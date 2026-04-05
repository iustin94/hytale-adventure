package dev.hytalemodding.hyadventure.services;

import com.hypixel.hytale.builtin.adventure.npcobjectives.assets.BountyObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.npcobjectives.assets.KillObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.npcobjectives.assets.KillSpawnMarkerObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveLineAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.ClearObjectiveItemsCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.GiveItemsCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.ObjectiveCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.*;
import com.hypixel.hytale.builtin.adventure.objectivereputation.assets.ReputationCompletionAsset;
import com.hypixel.hytale.logger.HytaleLogger;
import dev.hytalemodding.hyadventure.models.*;
import dev.hytalemodding.hyadventure.util.ConfigManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class QuestRegistrationService {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String PACK_KEY = "HyAdventure:Authored";

    private final ConfigManager configManager;

    public QuestRegistrationService(@Nonnull ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void registerAllOnStartup() {
        AuthoredQuestData data = configManager.getData();
        int objCount = 0, lineCount = 0;

        for (AuthoredObjective obj : data.getObjectives()) {
            try {
                registerObjective(obj);
                objCount++;
            } catch (Exception e) {
                LOGGER.atWarning().log("[HyAdventure] Failed to register objective " + obj.getId() + ": " + e.getMessage());
            }
        }

        for (AuthoredQuestLine line : data.getQuestLines()) {
            try {
                registerQuestLine(line);
                lineCount++;
            } catch (Exception e) {
                LOGGER.atWarning().log("[HyAdventure] Failed to register quest line " + line.getId() + ": " + e.getMessage());
            }
        }

        LOGGER.atInfo().log("[HyAdventure] Registered " + objCount + " authored objectives, " + lineCount + " quest lines.");
    }

    public void registerQuestLine(AuthoredQuestLine line) {
        ObjectiveLineAsset asset = new ObjectiveLineAsset(
                line.getId(),
                line.getCategory(),
                line.getObjectiveIds().toArray(new String[0]),
                line.getTitleKey(),
                line.getDescriptionKey(),
                line.getNextQuestLineIds().toArray(new String[0])
        );
        ObjectiveLineAsset.getAssetStore().loadAssets(PACK_KEY, List.of(asset));
    }

    public void registerObjective(AuthoredObjective obj) {
        TaskSet[] taskSets = buildTaskSets(obj);
        ObjectiveCompletionAsset[] completions = buildCompletions(obj);

        ObjectiveAsset asset = new ObjectiveAsset(
                obj.getId(),
                obj.getCategory(),
                taskSets,
                completions,
                obj.getTitleKey(),
                obj.getDescriptionKey(),
                obj.isRemoveOnItemDrop()
        );
        ObjectiveAsset.getAssetStore().loadAssets(PACK_KEY, List.of(asset));
    }

    // ── Task set conversion ──────────────────────────────────────────────────

    private TaskSet[] buildTaskSets(AuthoredObjective obj) {
        List<TaskSet> result = new ArrayList<>();
        for (AuthoredTaskSet ats : obj.getTaskSets()) {
            ObjectiveTaskAsset[] tasks = buildTasks(ats);
            result.add(new TaskSet(ats.getDescriptionId(), tasks));
        }
        return result.toArray(new TaskSet[0]);
    }

    private ObjectiveTaskAsset[] buildTasks(AuthoredTaskSet ats) {
        List<ObjectiveTaskAsset> result = new ArrayList<>();
        for (AuthoredTask at : ats.getTasks()) {
            ObjectiveTaskAsset task = buildTask(at);
            if (task != null) result.add(task);
        }
        return result.toArray(new ObjectiveTaskAsset[0]);
    }

    private ObjectiveTaskAsset buildTask(AuthoredTask at) {
        String desc = at.getDescriptionId();
        int count = at.getCount();

        return switch (at.getType()) {
            case GATHER -> new GatherObjectiveTaskAsset(desc, null, null, count,
                    new BlockTagOrItemIdField(null, at.getBlockTagOrItemId()));
            case CRAFT -> new CraftObjectiveTaskAsset(desc, null, null, count, at.getBlockTagOrItemId());
            case USE_BLOCK -> new UseBlockObjectiveTaskAsset(desc, null, null, count,
                    new BlockTagOrItemIdField(null, at.getBlockTagOrItemId()));
            case USE_ENTITY -> {
                UseEntityObjectiveTaskAsset.DialogOptions dialog = null;
                if (!at.getDialogEntityNameKey().isEmpty() && !at.getDialogKey().isEmpty()) {
                    dialog = new UseEntityObjectiveTaskAsset.DialogOptions(
                            at.getDialogEntityNameKey(), at.getDialogKey());
                }
                yield new UseEntityObjectiveTaskAsset(desc, null, null, count,
                        at.getTaskId(), at.getAnimationIdToPlay(), dialog);
            }
            case REACH_LOCATION -> new ReachLocationTaskAsset();
            case TREASURE_MAP -> new TreasureMapObjectiveTaskAsset(desc, null, null, null);
            case KILL -> new KillObjectiveTaskAsset(desc, null, null, count, at.getNpcGroupId());
            case KILL_SPAWN_MARKER -> new KillSpawnMarkerObjectiveTaskAsset(desc, null, null, count,
                    at.getNpcGroupId(), at.getSpawnMarkerIds().toArray(new String[0]), at.getRadius());
            case BOUNTY -> new BountyObjectiveTaskAsset(desc, null, null, at.getNpcId(), null);
        };
    }

    // ── Completion conversion ────────────────────────────────────────────────

    private ObjectiveCompletionAsset[] buildCompletions(AuthoredObjective obj) {
        List<ObjectiveCompletionAsset> result = new ArrayList<>();
        for (AuthoredCompletion ac : obj.getCompletions()) {
            ObjectiveCompletionAsset comp = buildCompletion(ac);
            if (comp != null) result.add(comp);
        }
        return result.toArray(new ObjectiveCompletionAsset[0]);
    }

    private ObjectiveCompletionAsset buildCompletion(AuthoredCompletion ac) {
        return switch (ac.getType()) {
            case GIVE_ITEMS -> new GiveItemsCompletionAsset(ac.getDropListId());
            case CLEAR_OBJECTIVE_ITEMS -> null; // protected constructor — handled by server internally
            case REPUTATION -> new ReputationCompletionAsset(ac.getReputationGroupId(), ac.getReputationAmount());
        };
    }
}
