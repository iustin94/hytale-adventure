package dev.hytalemodding.hyadventure.services;

import com.hypixel.hytale.builtin.adventure.npcobjectives.NPCObjectivesPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketWatcher;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.hyadventure.models.*;
import dev.hytalemodding.hyadventure.pages.QuestDialogPage;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intercepts NPC interactions (F-key) and starts quest objectives
 * for NPCs that have been assigned as quest givers via their entity UUID.
 * No role file modification needed — works with any NPC regardless of role.
 */
public class QuestInteractionListener implements PacketWatcher {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final QuestAuthoringService authoring;
    private final ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 1000;

    public QuestInteractionListener(@Nonnull QuestAuthoringService authoring) {
        this.authoring = authoring;
    }

    public void register() {
        PacketAdapters.registerInbound(this);
        LOGGER.atInfo().log("[HyAdventure] Quest interaction listener registered");
    }

    @Override
    public void accept(PacketHandler packetHandler, Packet packet) {
        if (!(packet instanceof SyncInteractionChains interactionChains)) return;

        try {
            if (packetHandler.getAuth() == null) return;

            UUID playerUuid = packetHandler.getAuth().getUuid();
            PlayerRef playerRef = Universe.get().getPlayer(playerUuid);
            if (playerRef == null || !playerRef.isValid()) return;

            World world = Universe.get().getWorld(playerRef.getWorldUuid());
            if (world == null) return;

            world.execute(() -> {
                for (SyncInteractionChain chain : interactionChains.updates) {
                    handleInteraction(playerRef, chain, world);
                }
            });
        } catch (Exception e) {
            LOGGER.atWarning().log("[HyAdventure] Error handling interaction: " + e.getMessage());
        }
    }

    private void handleInteraction(@Nonnull PlayerRef playerRef, @Nonnull SyncInteractionChain chain, @Nonnull World world) {
        // Only handle F-key (Use) interactions
        if (chain.interactionType != InteractionType.Use) return;
        if (chain.data == null) return;

        // Cooldown check
        UUID playerUuid = playerRef.getUuid();
        long now = System.currentTimeMillis();
        Long lastInteraction = cooldowns.get(playerUuid);
        if (lastInteraction != null && now - lastInteraction < COOLDOWN_MS) return;
        cooldowns.put(playerUuid, now);

        // Resolve NPC entity and UUID
        Store<EntityStore> store = playerRef.getReference().getStore();
        Ref<EntityStore> npcRef = store.getExternalData().getRefFromNetworkId(chain.data.entityId);
        if (npcRef == null) return;

        UUIDComponent uuidComp = npcRef.getStore().getComponent(npcRef, UUIDComponent.getComponentType());
        if (uuidComp == null || uuidComp.getUuid() == null) return;

        String npcUuid = uuidComp.getUuid().toString();

        // Look up quest dialog for this NPC
        AuthoredDialog dialog = findDialogForNpc(npcUuid);
        if (dialog == null) return;

        // Open the quest dialog page for this player
        try {
            Player playerComp = store.getComponent(playerRef.getReference(), Player.getComponentType());
            if (playerComp != null) {
                playerComp.getPageManager().openCustomPage(
                        playerRef.getReference(), store,
                        new QuestDialogPage(playerRef, dialog, 0));
                LOGGER.atInfo().log("[HyAdventure] Opened quest dialog for player " + playerUuid + " via NPC " + npcUuid);
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("[HyAdventure] Failed to open dialog: " + e.getMessage());

            // Fallback: try starting objective directly
            String objectiveId = findQuestObjectiveForNpc(npcUuid);
            if (objectiveId != null) {
                try { NPCObjectivesPlugin.startObjective(playerRef.getReference(), objectiveId, store); }
                catch (Exception ex) { LOGGER.atWarning().log("[HyAdventure] Fallback objective start failed: " + ex.getMessage()); }
            }
        }
    }

    /**
     * Finds the dialog assigned to this NPC UUID.
     */
    private AuthoredDialog findDialogForNpc(String npcUuid) {
        for (AuthoredNpcAssignment npc : authoring.getData().getNpcAssignments()) {
            if (!npcUuid.equals(npc.getNpcEntityUuid())) continue;
            if (npc.getDialogId().isEmpty()) continue;
            return authoring.findDialog(npc.getDialogId());
        }
        return null;
    }

    /**
     * Finds the first objective ID assigned to this NPC UUID as a quest giver.
     */
    private String findQuestObjectiveForNpc(String npcUuid) {
        for (AuthoredNpcAssignment npc : authoring.getData().getNpcAssignments()) {
            if (npc.getAssignmentType() != NpcAssignmentType.QUEST_GIVER) continue;
            if (!npcUuid.equals(npc.getNpcEntityUuid())) continue;

            // Find which quest line references this NPC assignment
            for (AuthoredQuestLine line : authoring.getData().getQuestLines()) {
                if (!npc.getId().equals(line.getQuestGiverNpcId())) continue;
                if (line.getObjectiveIds().isEmpty()) continue;
                return line.getObjectiveIds().get(0);
            }
        }
        return null;
    }
}
