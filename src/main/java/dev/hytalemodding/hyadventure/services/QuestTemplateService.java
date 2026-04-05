package dev.hytalemodding.hyadventure.services;

import dev.hytalemodding.hyadventure.models.*;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Generates pre-built quest templates showcasing different task types
 * and reward structures supported by the adventure system.
 */
public class QuestTemplateService {

    private final QuestAuthoringService authoring;

    public QuestTemplateService(@Nonnull QuestAuthoringService authoring) {
        this.authoring = authoring;
    }

    public Map<String, Object> generateAllTemplates() {
        List<String> created = new ArrayList<>();

        created.add(createGatherQuest());
        created.add(createCraftQuest());
        created.add(createKillQuest());
        created.add(createExploreQuest());
        created.add(createEscortQuest());
        created.add(createBountyQuest());
        created.add(createMultiPhaseQuest());
        created.add(createBranchingQuestLine());

        authoring.save();
        return Map.of("success", true, "message",
                "Created " + created.size() + " quest templates: " + String.join(", ", created));
    }

    public Map<String, Object> generateSingleTemplate(String templateId) {
        if (templateId == null || templateId.isEmpty())
            return Map.of("success", false, "error", "templateId required");

        String created = switch (templateId) {
            case "template_gather" -> createGatherQuest();
            case "template_craft" -> createCraftQuest();
            case "template_kill" -> createKillQuest();
            case "template_explore" -> createExploreQuest();
            case "template_talk" -> createEscortQuest();
            case "template_bounty" -> createBountyQuest();
            case "template_multiphase" -> createMultiPhaseQuest();
            case "template_branching" -> createBranchingQuestLine();
            default -> null;
        };

        if (created == null)
            return Map.of("success", false, "error", "Unknown template: " + templateId);

        authoring.save();
        return Map.of("success", true, "entityId", templateId, "message", "Created template: " + created);
    }

    // ─── Template: Gather Quest ──────────────────────────────────────────────

    private String createGatherQuest() {
        String id = "template_gather";
        if (authoring.findLine(id) != null) return id + " (exists)";

        AuthoredObjective obj = objective(id + "_obj",
                "Gather 5 Iron Ore for the blacksmith.",
                task(TaskType.GATHER, 5, t -> t.setBlockTagOrItemId("Iron_Ore")),
                completion(CompletionType.GIVE_ITEMS, c -> c.setDropListId("blacksmith_reward")));

        AuthoredQuestLine line = questLine(id, "side_quest",
                "The Blacksmith's Request",
                "The blacksmith needs iron ore to forge new tools.",
                obj);

        dialog(id + "_npc_dialog",
                "Blacksmith",
                "I'm running low on iron. Could you bring me 5 pieces of Iron Ore from the mines?");

        npcAssignment(id + "_npc", "Blacksmith", NpcAssignmentType.QUEST_GIVER, id + "_npc_dialog");
        line.setQuestGiverNpcId(id + "_npc");
        return id;
    }

    // ─── Template: Craft Quest ───────────────────────────────────────────────

    private String createCraftQuest() {
        String id = "template_craft";
        if (authoring.findLine(id) != null) return id + " (exists)";

        AuthoredObjective obj = objective(id + "_obj",
                "Craft 3 Wooden Planks at a workbench.",
                task(TaskType.CRAFT, 3, t -> t.setBlockTagOrItemId("Plank_Wood")),
                completion(CompletionType.REPUTATION, c -> {
                    c.setReputationGroupId("village");
                    c.setReputationAmount(10);
                }));

        questLine(id, "daily",
                "Carpentry Practice",
                "Practice your carpentry by crafting wooden planks.",
                obj);
        return id;
    }

    // ─── Template: Kill Quest ────────────────────────────────────────────────

    private String createKillQuest() {
        String id = "template_kill";
        if (authoring.findLine(id) != null) return id + " (exists)";

        AuthoredObjective obj = objective(id + "_obj",
                "Defeat 10 skeletons threatening the village.",
                task(TaskType.KILL, 10, t -> t.setNpcGroupId("Skeleton")),
                completion(CompletionType.GIVE_ITEMS, c -> c.setDropListId("warrior_reward")));

        AuthoredQuestLine line = questLine(id, "side_quest",
                "Skeleton Menace",
                "Skeletons have been spotted near the village. Eliminate them!",
                obj);

        dialog(id + "_npc_dialog",
                "Village Guard",
                "We've seen skeletons lurking near the outskirts. Can you take care of them?");

        npcAssignment(id + "_npc", "Guard", NpcAssignmentType.QUEST_GIVER, id + "_npc_dialog");
        line.setQuestGiverNpcId(id + "_npc");
        return id;
    }

    // ─── Template: Explore / Reach Location ──────────────────────────────────

    private String createExploreQuest() {
        String id = "template_explore";
        if (authoring.findLine(id) != null) return id + " (exists)";

        // Create a location for the destination
        AuthoredLocation loc = new AuthoredLocation();
        loc.setId(id + "_destination");
        loc.setLabel("Ancient Ruins");
        loc.setX(200);
        loc.setY(64);
        loc.setZ(300);
        loc.setRadius(10f);
        authoring.getData().getLocations().add(loc);

        AuthoredObjective obj = objective(id + "_obj",
                "Find the Ancient Ruins to the east.",
                task(TaskType.REACH_LOCATION, 1, t -> t.setTargetLocationId(id + "_destination")),
                completion(CompletionType.REPUTATION, c -> {
                    c.setReputationGroupId("explorers_guild");
                    c.setReputationAmount(25);
                }));

        questLine(id, "side_quest",
                "The Lost Ruins",
                "Legends speak of ancient ruins hidden in the eastern hills.",
                obj);
        return id;
    }

    // ─── Template: Escort / Talk to NPC ──────────────────────────────────────

    private String createEscortQuest() {
        String id = "template_talk";
        if (authoring.findLine(id) != null) return id + " (exists)";

        AuthoredObjective obj = objective(id + "_obj",
                "Speak with the herbalist about the strange plants.",
                task(TaskType.USE_ENTITY, 1, t -> {
                    t.setTaskId(id + "_interact");
                    t.setDialogEntityNameKey(id + "_herbalist_name");
                    t.setDialogKey(id + "_herbalist_dialog");
                }),
                completion(CompletionType.GIVE_ITEMS, c -> c.setDropListId("herb_bundle")));

        AuthoredQuestLine line = questLine(id, "main_quest",
                "The Herbalist's Knowledge",
                "The village elder wants you to consult the herbalist.",
                obj);

        dialog(id + "_start_dialog",
                "Village Elder",
                "Strange plants have appeared near the river. Please speak with our herbalist about them.");

        dialog(id + "_herbalist_dialog_node",
                "Herbalist",
                "Ah yes, these are Moon Blossoms. They only grow when the magic in the land is strong. Take these samples.");

        npcAssignment(id + "_elder_npc", "Elder", NpcAssignmentType.QUEST_GIVER, id + "_start_dialog");
        npcAssignment(id + "_herbalist_npc", "Herbalist", NpcAssignmentType.INTERACT, id + "_herbalist_dialog_node");
        line.setQuestGiverNpcId(id + "_elder_npc");
        return id;
    }

    // ─── Template: Bounty Quest ──────────────────────────────────────────────

    private String createBountyQuest() {
        String id = "template_bounty";
        if (authoring.findLine(id) != null) return id + " (exists)";

        AuthoredObjective obj = objective(id + "_obj",
                "Hunt down the bandit leader.",
                task(TaskType.BOUNTY, 1, t -> t.setNpcId("Bandit_Leader")),
                completion(CompletionType.GIVE_ITEMS, c -> c.setDropListId("bounty_gold")));

        AuthoredQuestLine line = questLine(id, "side_quest",
                "Wanted: Bandit Leader",
                "A bounty has been placed on the bandit leader terrorizing travelers.",
                obj);

        dialog(id + "_npc_dialog",
                "Sheriff",
                "There's a bounty on the bandit leader. Bring them to justice and the reward is yours.");

        npcAssignment(id + "_npc", "Sheriff", NpcAssignmentType.QUEST_GIVER, id + "_npc_dialog");
        line.setQuestGiverNpcId(id + "_npc");
        return id;
    }

    // ─── Template: Multi-Phase Quest ─────────────────────────────────────────

    private String createMultiPhaseQuest() {
        String id = "template_multiphase";
        if (authoring.findLine(id) != null) return id + " (exists)";

        AuthoredObjective obj = new AuthoredObjective();
        obj.setId(id + "_obj");
        obj.setCategory("main_quest");
        obj.setTitleKey(id + "_obj_title");
        obj.setDescriptionKey(id + "_obj_desc");

        // Phase 1: Gather materials
        AuthoredTaskSet phase1 = new AuthoredTaskSet();
        phase1.setDescriptionId(id + "_phase1");
        AuthoredTask gatherTask = new AuthoredTask();
        gatherTask.setType(TaskType.GATHER);
        gatherTask.setCount(10);
        gatherTask.setBlockTagOrItemId("Stone");
        gatherTask.setDescriptionId(id + "_gather_stone");
        phase1.getTasks().add(gatherTask);
        obj.getTaskSets().add(phase1);

        // Phase 2: Craft the item
        AuthoredTaskSet phase2 = new AuthoredTaskSet();
        phase2.setDescriptionId(id + "_phase2");
        AuthoredTask craftTask = new AuthoredTask();
        craftTask.setType(TaskType.CRAFT);
        craftTask.setCount(1);
        craftTask.setBlockTagOrItemId("Stone_Pickaxe");
        craftTask.setDescriptionId(id + "_craft_pickaxe");
        phase2.getTasks().add(craftTask);
        obj.getTaskSets().add(phase2);

        // Phase 3: Talk to NPC to deliver
        AuthoredTaskSet phase3 = new AuthoredTaskSet();
        phase3.setDescriptionId(id + "_phase3");
        AuthoredTask talkTask = new AuthoredTask();
        talkTask.setType(TaskType.USE_ENTITY);
        talkTask.setCount(1);
        talkTask.setTaskId(id + "_deliver");
        talkTask.setDescriptionId(id + "_deliver_pickaxe");
        phase3.getTasks().add(talkTask);
        obj.getTaskSets().add(phase3);

        // Reward
        AuthoredCompletion reward = new AuthoredCompletion();
        reward.setType(CompletionType.GIVE_ITEMS);
        reward.setDropListId("miner_reward");
        obj.getCompletions().add(reward);

        authoring.getData().getObjectives().add(obj);

        AuthoredQuestLine line = questLine(id, "main_quest",
                "The Miner's Errand",
                "Gather stone, craft a pickaxe, and deliver it to the mining foreman.",
                null); // objective already added
        line.getObjectiveIds().add(id + "_obj");

        dialog(id + "_npc_dialog",
                "Mining Foreman",
                "We need a new stone pickaxe. Gather some stone, craft it, and bring it to me.");

        npcAssignment(id + "_npc", "Miner", NpcAssignmentType.QUEST_GIVER, id + "_npc_dialog");
        line.setQuestGiverNpcId(id + "_npc");
        return id;
    }

    // ─── Template: Branching Quest Line ──────────────────────────────────────

    private String createBranchingQuestLine() {
        String id = "template_branching";
        if (authoring.findLine(id) != null) return id + " (exists)";

        // Main quest line
        AuthoredObjective mainObj = objective(id + "_main_obj",
                "Investigate the disturbance in the forest.",
                task(TaskType.USE_ENTITY, 1, t -> {
                    t.setTaskId(id + "_investigate");
                    t.setDescriptionId(id + "_investigate_task");
                }),
                null);

        AuthoredQuestLine mainLine = questLine(id, "main_quest",
                "The Forest Mystery",
                "Strange things are happening in the forest. Investigate!",
                mainObj);

        // Branch A: Combat path
        AuthoredObjective combatObj = objective(id + "_combat_obj",
                "Defeat the corrupted treants.",
                task(TaskType.KILL, 5, t -> t.setNpcGroupId("Treant")),
                completion(CompletionType.GIVE_ITEMS, c -> c.setDropListId("warrior_reward")));

        AuthoredQuestLine combatLine = questLine(id + "_combat", "main_quest",
                "Path of the Warrior",
                "Fight the corruption head-on by defeating the treants.",
                combatObj);

        // Branch B: Peaceful path
        AuthoredObjective peaceObj = objective(id + "_peace_obj",
                "Gather 5 Purification Herbs to cleanse the forest.",
                task(TaskType.GATHER, 5, t -> t.setBlockTagOrItemId("Purification_Herb")),
                completion(CompletionType.REPUTATION, c -> {
                    c.setReputationGroupId("druids");
                    c.setReputationAmount(50);
                }));

        AuthoredQuestLine peaceLine = questLine(id + "_peace", "main_quest",
                "Path of the Druid",
                "Cleanse the corruption through nature's remedy.",
                peaceObj);

        // Link branches
        mainLine.getNextQuestLineIds().add(id + "_combat");
        mainLine.getNextQuestLineIds().add(id + "_peace");

        dialog(id + "_npc_dialog",
                "Forest Warden",
                "The forest is sick. You must choose: fight the corruption, or seek a natural cure.");

        npcAssignment(id + "_npc", "Warden", NpcAssignmentType.QUEST_GIVER, id + "_npc_dialog");
        mainLine.setQuestGiverNpcId(id + "_npc");
        return id;
    }

    // ─── Builder helpers ─────────────────────────────────────────────────────

    private AuthoredObjective objective(String id, String desc, AuthoredTask task, AuthoredCompletion completion) {
        AuthoredObjective obj = new AuthoredObjective();
        obj.setId(id);
        obj.setCategory("custom");
        obj.setTitleKey(id + "_title");
        obj.setDescriptionKey(id + "_desc");

        AuthoredTaskSet ts = new AuthoredTaskSet();
        ts.setDescriptionId(id + "_phase");
        ts.getTasks().add(task);
        obj.getTaskSets().add(ts);

        if (completion != null) obj.getCompletions().add(completion);
        authoring.getData().getObjectives().add(obj);
        return obj;
    }

    private AuthoredTask task(TaskType type, int count, java.util.function.Consumer<AuthoredTask> configure) {
        AuthoredTask t = new AuthoredTask();
        t.setType(type);
        t.setCount(count);
        t.setDescriptionId("task_" + type.name().toLowerCase());
        configure.accept(t);
        return t;
    }

    private AuthoredCompletion completion(CompletionType type, java.util.function.Consumer<AuthoredCompletion> configure) {
        AuthoredCompletion c = new AuthoredCompletion();
        c.setType(type);
        configure.accept(c);
        return c;
    }

    private AuthoredQuestLine questLine(String id, String category, String title, String desc, AuthoredObjective obj) {
        AuthoredQuestLine line = new AuthoredQuestLine();
        line.setId(id);
        line.setCategory(category);
        line.setTitleKey(id + "_title");
        line.setDescriptionKey(id + "_desc");
        if (obj != null) line.getObjectiveIds().add(obj.getId());
        authoring.getData().getQuestLines().add(line);

        // Store display text for translations
        dialog(id + "_title_text", title, desc);
        return line;
    }

    private void dialog(String id, String nameText, String dialogText) {
        if (authoring.findDialog(id) != null) return;
        AuthoredDialog dlg = new AuthoredDialog();
        dlg.setId(id);
        dlg.setEntityNameKey(id + "_name");
        dlg.setDialogKey(id + "_text");
        dlg.setEntityNameText(nameText);
        dlg.setDialogText(dialogText);
        authoring.getData().getDialogs().add(dlg);
    }

    private void npcAssignment(String id, String role, NpcAssignmentType type, String dialogId) {
        if (authoring.findNpcAssignment(id) != null) return;
        AuthoredNpcAssignment npc = new AuthoredNpcAssignment();
        npc.setId(id);
        npc.setNpcRole(role);
        npc.setAssignmentType(type);
        npc.setDialogId(dialogId);
        authoring.getData().getNpcAssignments().add(npc);
    }
}
