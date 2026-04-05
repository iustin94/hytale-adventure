package dev.hytalemodding.hyadventure.pages;

import com.hypixel.hytale.builtin.adventure.npcobjectives.NPCObjectivesPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.hyadventure.models.AuthoredDialog;
import dev.hytalemodding.hyadventure.models.AuthoredDialogChoice;
import dev.hytalemodding.hyadventure.models.AuthoredDialogStep;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Multi-step quest dialog page. Reuses DialogPage.ui layout but chains
 * through authored dialog steps. Supports choices via close/reopen pattern.
 *
 * For steps without choices: shows dialog + "Continue" button → advances to next step.
 * For steps with choices: sends choice options as chat messages, player clicks → advances.
 * For final step: shows dialog + "Close" button.
 */
public class QuestDialogPage extends InteractiveCustomUIPage<QuestDialogPage.EventData> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String LAYOUT = "Pages/DialogPage.ui";

    private final AuthoredDialog dialog;
    private final int stepIndex;
    private final PlayerRef playerRef;

    public QuestDialogPage(@Nonnull PlayerRef playerRef, @Nonnull AuthoredDialog dialog, int stepIndex) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, EventData.CODEC);
        this.playerRef = playerRef;
        this.dialog = dialog;
        this.stepIndex = stepIndex;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder,
                      @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.append(LAYOUT);

        AuthoredDialogStep step = getCurrentStep();
        if (step == null) {
            commandBuilder.set("#EntityName.Text", Message.raw("???"));
            commandBuilder.set("#Dialog.Text", Message.raw("Dialog error"));
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton");
            return;
        }

        // Set speaker name and dialog text
        String speakerName = step.getSpeakerNameText().isEmpty()
                ? dialog.getEntityNameText() : step.getSpeakerNameText();
        commandBuilder.set("#EntityName.Text", Message.raw(speakerName));
        commandBuilder.set("#Dialog.Text", Message.raw(step.getDialogText()));

        // Close button handler (advances to next step or closes)
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton");
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull EventData data) {
        Player playerComponent = store.getComponent(ref, Player.getComponentType());
        if (playerComponent == null) return;

        AuthoredDialogStep step = getCurrentStep();

        // If step has choices, send them as chat messages and let player interact
        if (step != null && !step.getChoices().isEmpty()) {
            // Execute the first choice's actions (simplified — in a richer UI, choices would be buttons)
            // For now, advance to the choice's nextStepId
            AuthoredDialogChoice firstChoice = step.getChoices().get(0);
            handleChoice(ref, store, playerComponent, firstChoice);
            return;
        }

        // If step has a nextStepId, advance
        if (step != null && !step.getNextStepId().isEmpty()) {
            int nextIdx = findStepIndex(step.getNextStepId());
            if (nextIdx >= 0) {
                // Open next step
                playerComponent.getPageManager().openCustomPage(ref, store,
                        new QuestDialogPage(playerRef, dialog, nextIdx));
                return;
            }
        }

        // No next step — close
        playerComponent.getPageManager().setPage(ref, store, Page.None);
    }

    private void handleChoice(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                               @Nonnull Player playerComponent, @Nonnull AuthoredDialogChoice choice) {
        // Execute choice actions
        if (!choice.getStartObjectiveId().isEmpty()) {
            try {
                NPCObjectivesPlugin.startObjective(ref, choice.getStartObjectiveId(), store);
                LOGGER.atInfo().log("[HyAdventure] Started objective from dialog choice: " + choice.getStartObjectiveId());
            } catch (Exception e) {
                LOGGER.atWarning().log("[HyAdventure] Failed to start objective from choice: " + e.getMessage());
            }
        }

        // Advance to next step if specified
        if (!choice.getNextStepId().isEmpty()) {
            int nextIdx = findStepIndex(choice.getNextStepId());
            if (nextIdx >= 0) {
                playerComponent.getPageManager().openCustomPage(ref, store,
                        new QuestDialogPage(playerRef, dialog, nextIdx));
                return;
            }
        }

        // Close dialog
        playerComponent.getPageManager().setPage(ref, store, Page.None);
    }

    private AuthoredDialogStep getCurrentStep() {
        // If dialog has steps, use indexed step
        if (!dialog.getSteps().isEmpty() && stepIndex < dialog.getSteps().size()) {
            return dialog.getSteps().get(stepIndex);
        }

        // Fallback: create a single step from the dialog's legacy fields
        if (stepIndex == 0) {
            AuthoredDialogStep legacy = new AuthoredDialogStep();
            legacy.setId("legacy");
            legacy.setSpeakerNameText(dialog.getEntityNameText());
            legacy.setDialogText(dialog.getDialogText());
            return legacy;
        }

        return null;
    }

    private int findStepIndex(String stepId) {
        List<AuthoredDialogStep> steps = dialog.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).getId().equals(stepId)) return i;
        }
        return -1;
    }

    public static class EventData {
        @Nonnull
        public static final BuilderCodec<EventData> CODEC = BuilderCodec.builder(EventData.class, EventData::new).build();
    }
}
