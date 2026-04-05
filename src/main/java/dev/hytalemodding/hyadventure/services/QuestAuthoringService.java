package dev.hytalemodding.hyadventure.services;

import dev.hytalemodding.hyadventure.models.*;
import dev.hytalemodding.hyadventure.util.ConfigManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class QuestAuthoringService {

    private final ConfigManager configManager;

    public QuestAuthoringService(@Nonnull ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Nonnull public AuthoredQuestData getData() { return configManager.getData(); }
    public void reload() { configManager.load(); }
    public void save() {
        configManager.save();
        // Regenerate translations after any data change
        try {
            dev.hytalemodding.hyadventure.HyAdventurePlugin.get()
                    .getTranslationService().regenerate();
        } catch (Exception ignored) {}
    }

    // ── Quest Line CRUD ──────────────────────────────────────────────────────

    public List<Map<String, Object>> listQuestLines() {
        List<Map<String, Object>> r = new ArrayList<>();
        for (AuthoredQuestLine line : getData().getQuestLines()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", line.getId());
            m.put("label", "Line: " + line.getId());
            m.put("category", line.getCategory());
            m.put("objectiveCount", line.getObjectiveIds().size());
            m.put("branchCount", line.getNextQuestLineIds().size());
            r.add(m);
        }
        return r;
    }

    @Nullable
    public Map<String, Object> getQuestLineDetail(String id) {
        AuthoredQuestLine line = findLine(id);
        if (line == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("line_id", line.getId());
        m.put("line_category", line.getCategory());
        m.put("line_titleKey", line.getTitleKey());
        m.put("line_descriptionKey", line.getDescriptionKey());
        m.put("line_objectiveIds", line.getObjectiveIds());
        m.put("line_nextQuestLineIds", line.getNextQuestLineIds());
        m.put("line_questGiverNpcId", line.getQuestGiverNpcId());
        return m;
    }

    /**
     * Creates a complete valid quest in one step: quest line + objective + task + translations.
     */
    public Map<String, Object> createFullQuest(Map<String, String> params) {
        String name = params.getOrDefault("name", "");
        String id = params.getOrDefault("id", "");

        // Auto-generate ID from name if not provided
        if (id.isEmpty()) {
            if (name.isEmpty()) name = "New Quest";
            id = toSnakeCase(name) + "_" + (System.currentTimeMillis() % 100000);
        }
        if (findLine(id) != null) return error("Quest already exists: " + id);
        String category = params.getOrDefault("category", "custom");
        String taskTypeStr = params.getOrDefault("taskType", "USE_ENTITY");
        String taskTarget = params.getOrDefault("taskTarget", "");
        int taskCount = 1;
        try { taskCount = Integer.parseInt(params.getOrDefault("taskCount", "1")); }
        catch (NumberFormatException ignored) {}

        TaskType taskType;
        try { taskType = TaskType.valueOf(taskTypeStr); }
        catch (IllegalArgumentException e) { taskType = TaskType.USE_ENTITY; }

        // Build objective with configured task
        String objId = id + "_objective_0";
        AuthoredObjective obj = new AuthoredObjective();
        obj.setId(objId);
        obj.setCategory(category);
        obj.setTitleKey(objId + "_title");
        obj.setDescriptionKey(objId + "_desc");

        AuthoredTaskSet ts = new AuthoredTaskSet();
        ts.setDescriptionId(objId + "_phase_0");
        AuthoredTask task = new AuthoredTask();
        task.setType(taskType);
        task.setCount(taskCount);
        task.setDescriptionId(objId + "_task_0");

        // Set the target field based on task type
        switch (taskType) {
            case GATHER, CRAFT, USE_BLOCK -> task.setBlockTagOrItemId(taskTarget);
            case USE_ENTITY -> { task.setTaskId(taskTarget.isEmpty() ? id + "_interact" : taskTarget); }
            case REACH_LOCATION -> task.setTargetLocationId(taskTarget);
            case KILL, KILL_SPAWN_MARKER -> task.setNpcGroupId(taskTarget);
            case BOUNTY -> task.setNpcId(taskTarget);
            default -> {}
        }

        ts.getTasks().add(task);
        obj.getTaskSets().add(ts);
        getData().getObjectives().add(obj);

        // Build quest line
        AuthoredQuestLine line = new AuthoredQuestLine();
        line.setId(id);
        line.setCategory(category);
        line.setTitleKey(id + "_title");
        line.setDescriptionKey(id + "_desc");
        line.getObjectiveIds().add(objId);
        getData().getQuestLines().add(line);

        // Build dialog with display name
        String dlgId = id + "_dialog";
        AuthoredDialog dlg = new AuthoredDialog();
        dlg.setId(dlgId);
        dlg.setEntityNameKey(dlgId + "_name");
        dlg.setDialogKey(dlgId + "_text");
        dlg.setEntityNameText(name);
        dlg.setDialogText("");
        getData().getDialogs().add(dlg);

        configManager.save();

        // Register with engine
        try {
            dev.hytalemodding.hyadventure.HyAdventurePlugin.get()
                    .getRegistrationService().registerObjective(obj);
            dev.hytalemodding.hyadventure.HyAdventurePlugin.get()
                    .getRegistrationService().registerQuestLine(line);
        } catch (Exception ignored) {}

        return Map.of("success", true, "entityId", id,
                "message", "Quest '" + name + "' created with " + taskType + " task");
    }

    public Map<String, Object> createQuestLine(Map<String, String> params) {
        String id = params.getOrDefault("id", "");
        if (id.isEmpty()) id = "quest_line_" + (System.currentTimeMillis() % 100000);
        if (findLine(id) != null) return error("Quest line already exists: " + id);

        String category = params.getOrDefault("category", "custom");

        // Auto-create a default objective for this quest line
        String defaultObjId = id + "_objective_0";
        if (findObjective(defaultObjId) == null) {
            var objParams = new java.util.HashMap<String, String>();
            objParams.put("id", defaultObjId);
            objParams.put("category", category);
            objParams.put("titleKey", defaultObjId);
            createObjective(objParams);
        }

        AuthoredQuestLine line = new AuthoredQuestLine();
        line.setId(id);
        line.setCategory(category);
        line.setTitleKey(params.getOrDefault("titleKey", id));
        line.setDescriptionKey(params.getOrDefault("descriptionKey", id + "_desc"));
        line.getObjectiveIds().add(defaultObjId);
        getData().getQuestLines().add(line);
        configManager.save();

        // Register with engine
        try {
            dev.hytalemodding.hyadventure.HyAdventurePlugin.get()
                    .getRegistrationService().registerQuestLine(line);
        } catch (Exception ignored) {}

        return Map.of("success", true, "entityId", id, "message", "Quest line created with default objective");
    }

    public Map<String, Object> updateQuestLine(String id, Map<String, String> params) {
        AuthoredQuestLine line = findLine(id);
        if (line == null) return error("Quest line not found: " + id);
        if (params.containsKey("category")) line.setCategory(params.get("category"));
        if (params.containsKey("titleKey")) line.setTitleKey(params.get("titleKey"));
        if (params.containsKey("descriptionKey")) line.setDescriptionKey(params.get("descriptionKey"));
        configManager.save();
        return Map.of("success", true, "message", "Quest line updated");
    }

    public Map<String, Object> deleteQuestLine(String id) {
        boolean removed = getData().getQuestLines().removeIf(l -> l.getId().equals(id));
        if (!removed) return error("Quest line not found: " + id);
        configManager.save();
        return Map.of("success", true, "message", "Quest line deleted");
    }

    public Map<String, Object> addObjectiveToLine(String lineId, String objectiveId, int position) {
        AuthoredQuestLine line = findLine(lineId);
        if (line == null) return error("Quest line not found");
        if (position < 0 || position > line.getObjectiveIds().size()) position = line.getObjectiveIds().size();
        line.getObjectiveIds().add(position, objectiveId);
        configManager.save();
        return Map.of("success", true, "message", "Objective added at position " + position);
    }

    public Map<String, Object> removeObjectiveFromLine(String lineId, String objectiveId) {
        AuthoredQuestLine line = findLine(lineId);
        if (line == null) return error("Quest line not found");
        boolean removed = line.getObjectiveIds().remove(objectiveId);
        if (!removed) return error("Objective not in line");
        configManager.save();
        return Map.of("success", true, "message", "Objective removed");
    }

    public Map<String, Object> addBranch(String lineId, String nextLineId) {
        AuthoredQuestLine line = findLine(lineId);
        if (line == null) return error("Quest line not found");
        if (line.getNextQuestLineIds().contains(nextLineId)) return error("Branch already exists");
        line.getNextQuestLineIds().add(nextLineId);
        configManager.save();
        return Map.of("success", true, "message", "Branch added");
    }

    public Map<String, Object> removeBranch(String lineId, String nextLineId) {
        AuthoredQuestLine line = findLine(lineId);
        if (line == null) return error("Quest line not found");
        boolean removed = line.getNextQuestLineIds().remove(nextLineId);
        if (!removed) return error("Branch not found");
        configManager.save();
        return Map.of("success", true, "message", "Branch removed");
    }

    // ── Objective CRUD ───────────────────────────────────────────────────────

    public List<Map<String, Object>> listObjectives() {
        List<Map<String, Object>> r = new ArrayList<>();
        for (AuthoredObjective obj : getData().getObjectives()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", obj.getId());
            m.put("label", "Obj: " + obj.getId());
            m.put("category", obj.getCategory());
            m.put("taskSetCount", obj.getTaskSets().size());
            m.put("completionCount", obj.getCompletions().size());
            r.add(m);
        }
        return r;
    }

    @Nullable
    public Map<String, Object> getObjectiveDetail(String id) {
        AuthoredObjective obj = findObjective(id);
        if (obj == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("aobj_id", obj.getId());
        m.put("aobj_category", obj.getCategory());
        m.put("aobj_titleKey", obj.getTitleKey());
        m.put("aobj_descriptionKey", obj.getDescriptionKey());
        m.put("aobj_removeOnItemDrop", obj.isRemoveOnItemDrop());

        List<Map<String, Object>> tsList = new ArrayList<>();
        for (int i = 0; i < obj.getTaskSets().size(); i++) {
            AuthoredTaskSet ts = obj.getTaskSets().get(i);
            Map<String, Object> tsm = new LinkedHashMap<>();
            tsm.put("index", i);
            tsm.put("descriptionId", ts.getDescriptionId());
            List<Map<String, Object>> taskList = new ArrayList<>();
            for (int j = 0; j < ts.getTasks().size(); j++) {
                AuthoredTask t = ts.getTasks().get(j);
                Map<String, Object> tm = new LinkedHashMap<>();
                tm.put("index", j);
                tm.put("type", t.getType().name());
                tm.put("count", t.getCount());
                tm.put("descriptionId", t.getDescriptionId());
                tm.put("blockTagOrItemId", t.getBlockTagOrItemId());
                tm.put("taskId", t.getTaskId());
                tm.put("animationIdToPlay", t.getAnimationIdToPlay());
                tm.put("dialogEntityNameKey", t.getDialogEntityNameKey());
                tm.put("dialogKey", t.getDialogKey());
                tm.put("targetLocationId", t.getTargetLocationId());
                tm.put("npcGroupId", t.getNpcGroupId());
                tm.put("npcId", t.getNpcId());
                tm.put("spawnMarkerIds", t.getSpawnMarkerIds());
                tm.put("radius", t.getRadius());
                taskList.add(tm);
            }
            tsm.put("tasks", taskList);
            tsList.add(tsm);
        }
        m.put("aobj_taskSetCount", tsList.size());
        m.put("taskSets", tsList);

        List<Map<String, Object>> compList = new ArrayList<>();
        for (int i = 0; i < obj.getCompletions().size(); i++) {
            AuthoredCompletion c = obj.getCompletions().get(i);
            Map<String, Object> cm = new LinkedHashMap<>();
            cm.put("index", i);
            cm.put("type", c.getType().name());
            cm.put("dropListId", c.getDropListId());
            cm.put("reputationGroupId", c.getReputationGroupId());
            cm.put("reputationAmount", c.getReputationAmount());
            compList.add(cm);
        }
        m.put("aobj_completionCount", compList.size());
        m.put("completions", compList);
        return m;
    }

    public Map<String, Object> createObjective(Map<String, String> params) {
        String id = params.getOrDefault("id", "");
        if (id.isEmpty()) id = "objective_" + (System.currentTimeMillis() % 100000);
        if (findObjective(id) != null) return error("Objective already exists: " + id);

        AuthoredObjective obj = new AuthoredObjective();
        obj.setId(id);
        obj.setCategory(params.getOrDefault("category", "custom"));
        obj.setTitleKey(params.getOrDefault("titleKey", id));
        obj.setDescriptionKey(params.getOrDefault("descriptionKey", id + "_desc"));
        obj.setRemoveOnItemDrop(Boolean.parseBoolean(params.getOrDefault("removeOnItemDrop", "false")));

        // Initialize with a valid default task set + placeholder task
        // The engine requires at least one task set with one task to start an objective
        AuthoredTaskSet defaultTaskSet = new AuthoredTaskSet();
        defaultTaskSet.setDescriptionId(id + "_phase_0");
        AuthoredTask defaultTask = new AuthoredTask();
        defaultTask.setType(TaskType.USE_ENTITY);
        defaultTask.setCount(1);
        defaultTask.setDescriptionId(id + "_task_0");
        defaultTask.setTaskId(id + "_interact");
        defaultTaskSet.getTasks().add(defaultTask);
        obj.getTaskSets().add(defaultTaskSet);

        getData().getObjectives().add(obj);
        configManager.save();

        // Register with engine immediately so it's available for quest givers
        try {
            dev.hytalemodding.hyadventure.HyAdventurePlugin.get()
                    .getRegistrationService().registerObjective(obj);
        } catch (Exception ignored) {}

        return Map.of("success", true, "entityId", id, "message", "Objective created with default task");
    }

    public Map<String, Object> updateObjective(String id, Map<String, String> params) {
        AuthoredObjective obj = findObjective(id);
        if (obj == null) return error("Objective not found: " + id);
        if (params.containsKey("category")) obj.setCategory(params.get("category"));
        if (params.containsKey("titleKey")) obj.setTitleKey(params.get("titleKey"));
        if (params.containsKey("descriptionKey")) obj.setDescriptionKey(params.get("descriptionKey"));
        if (params.containsKey("removeOnItemDrop")) obj.setRemoveOnItemDrop(Boolean.parseBoolean(params.get("removeOnItemDrop")));
        configManager.save();
        return Map.of("success", true, "message", "Objective updated");
    }

    public Map<String, Object> deleteObjective(String id) {
        boolean removed = getData().getObjectives().removeIf(o -> o.getId().equals(id));
        if (!removed) return error("Objective not found: " + id);
        configManager.save();
        return Map.of("success", true, "message", "Objective deleted");
    }

    // ── TaskSet CRUD ─────────────────────────────────────────────────────────

    public Map<String, Object> addTaskSet(String objectiveId, Map<String, String> params) {
        AuthoredObjective obj = findObjective(objectiveId);
        if (obj == null) return error("Objective not found");
        AuthoredTaskSet ts = new AuthoredTaskSet();
        ts.setDescriptionId(params.getOrDefault("descriptionId", ""));
        obj.getTaskSets().add(ts);
        configManager.save();
        return Map.of("success", true, "message", "TaskSet added at index " + (obj.getTaskSets().size() - 1));
    }

    public Map<String, Object> removeTaskSet(String objectiveId, int index) {
        AuthoredObjective obj = findObjective(objectiveId);
        if (obj == null) return error("Objective not found");
        if (index < 0 || index >= obj.getTaskSets().size()) return error("Invalid task set index");
        obj.getTaskSets().remove(index);
        configManager.save();
        return Map.of("success", true, "message", "TaskSet removed");
    }

    // ── Task CRUD ────────────────────────────────────────────────────────────

    public Map<String, Object> addTask(String objectiveId, int taskSetIndex, Map<String, String> params) {
        AuthoredObjective obj = findObjective(objectiveId);
        if (obj == null) return error("Objective not found");
        if (taskSetIndex < 0 || taskSetIndex >= obj.getTaskSets().size()) return error("Invalid task set index");

        AuthoredTask task = new AuthoredTask();
        try { task.setType(TaskType.valueOf(params.getOrDefault("type", "GATHER"))); }
        catch (IllegalArgumentException e) { return error("Invalid task type"); }
        try { task.setCount(Integer.parseInt(params.getOrDefault("count", "1"))); }
        catch (NumberFormatException ignored) {}
        task.setDescriptionId(params.getOrDefault("descriptionId", ""));
        task.setBlockTagOrItemId(params.getOrDefault("blockTagOrItemId", ""));
        task.setTaskId(params.getOrDefault("taskId", ""));
        task.setAnimationIdToPlay(params.getOrDefault("animationIdToPlay", ""));
        task.setDialogEntityNameKey(params.getOrDefault("dialogEntityNameKey", ""));
        task.setDialogKey(params.getOrDefault("dialogKey", ""));
        task.setTargetLocationId(params.getOrDefault("targetLocationId", ""));
        task.setNpcGroupId(params.getOrDefault("npcGroupId", ""));
        task.setNpcId(params.getOrDefault("npcId", ""));
        try { task.setRadius(Float.parseFloat(params.getOrDefault("radius", "1.0"))); }
        catch (NumberFormatException ignored) {}
        String markers = params.getOrDefault("spawnMarkerIds", "");
        if (!markers.isEmpty()) task.setSpawnMarkerIds(List.of(markers.split(",")));

        obj.getTaskSets().get(taskSetIndex).getTasks().add(task);
        configManager.save();
        return Map.of("success", true, "message", "Task added");
    }

    public Map<String, Object> removeTask(String objectiveId, int taskSetIndex, int taskIndex) {
        AuthoredObjective obj = findObjective(objectiveId);
        if (obj == null) return error("Objective not found");
        if (taskSetIndex < 0 || taskSetIndex >= obj.getTaskSets().size()) return error("Invalid task set index");
        var tasks = obj.getTaskSets().get(taskSetIndex).getTasks();
        if (taskIndex < 0 || taskIndex >= tasks.size()) return error("Invalid task index");
        tasks.remove(taskIndex);
        configManager.save();
        return Map.of("success", true, "message", "Task removed");
    }

    // ── Completion CRUD ──────────────────────────────────────────────────────

    public Map<String, Object> addCompletion(String objectiveId, Map<String, String> params) {
        AuthoredObjective obj = findObjective(objectiveId);
        if (obj == null) return error("Objective not found");

        AuthoredCompletion comp = new AuthoredCompletion();
        try { comp.setType(CompletionType.valueOf(params.getOrDefault("type", "GIVE_ITEMS"))); }
        catch (IllegalArgumentException e) { return error("Invalid completion type"); }
        comp.setDropListId(params.getOrDefault("dropListId", ""));
        comp.setReputationGroupId(params.getOrDefault("reputationGroupId", ""));
        try { comp.setReputationAmount(Integer.parseInt(params.getOrDefault("reputationAmount", "1"))); }
        catch (NumberFormatException ignored) {}

        obj.getCompletions().add(comp);
        configManager.save();
        return Map.of("success", true, "message", "Completion added");
    }

    public Map<String, Object> removeCompletion(String objectiveId, int index) {
        AuthoredObjective obj = findObjective(objectiveId);
        if (obj == null) return error("Objective not found");
        if (index < 0 || index >= obj.getCompletions().size()) return error("Invalid completion index");
        obj.getCompletions().remove(index);
        configManager.save();
        return Map.of("success", true, "message", "Completion removed");
    }

    // ── NPC Assignment CRUD ────────────────────────────────────────────────

    public List<Map<String, Object>> listNpcAssignments() {
        List<Map<String, Object>> r = new ArrayList<>();
        for (AuthoredNpcAssignment npc : getData().getNpcAssignments()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", npc.getId());
            m.put("label", "NPC: " + npc.getNpcRole());
            r.add(m);
        }
        return r;
    }

    @Nullable
    public Map<String, Object> getNpcAssignmentDetail(String id) {
        AuthoredNpcAssignment npc = findNpcAssignment(id);
        if (npc == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("npc_id", npc.getId());
        m.put("npc_role", npc.getNpcRole());
        m.put("npc_type", npc.getAssignmentType().name());
        m.put("npc_dialogId", npc.getDialogId());
        m.put("npc_locationId", npc.getLocationId());
        m.put("npc_entityUuid", npc.getNpcEntityUuid());
        return m;
    }

    public Map<String, Object> createNpcAssignment(Map<String, String> params) {
        String id = params.getOrDefault("id", "");
        if (id.isEmpty()) id = "npc_" + (System.currentTimeMillis() % 100000);
        if (findNpcAssignment(id) != null) return error("NPC assignment already exists: " + id);

        String npcRole = params.getOrDefault("npcRole", "");

        // Auto-create a default dialog for this NPC
        String defaultDialogId = id + "_dialog";
        if (findDialog(defaultDialogId) == null) {
            var dlgParams = new java.util.HashMap<String, String>();
            dlgParams.put("id", defaultDialogId);
            dlgParams.put("entityNameKey", npcRole.isEmpty() ? id : npcRole);
            dlgParams.put("dialogKey", id + "_greeting");
            createDialog(dlgParams);
        }

        AuthoredNpcAssignment npc = new AuthoredNpcAssignment();
        npc.setId(id);
        npc.setNpcRole(npcRole);
        try { npc.setAssignmentType(NpcAssignmentType.valueOf(params.getOrDefault("assignmentType", "QUEST_GIVER"))); }
        catch (IllegalArgumentException e) { npc.setAssignmentType(NpcAssignmentType.QUEST_GIVER); }
        npc.setDialogId(defaultDialogId);
        getData().getNpcAssignments().add(npc);
        configManager.save();
        return Map.of("success", true, "entityId", id, "message", "NPC assignment created with default dialog");
    }

    public Map<String, Object> deleteNpcAssignment(String id) {
        boolean removed = getData().getNpcAssignments().removeIf(n -> n.getId().equals(id));
        if (!removed) return error("NPC assignment not found: " + id);
        configManager.save();
        return Map.of("success", true, "message", "NPC assignment deleted");
    }

    public Map<String, Object> setQuestGiver(String lineId, String npcAssignmentId) {
        AuthoredQuestLine line = findLine(lineId);
        if (line == null) return error("Quest line not found");

        AuthoredNpcAssignment npc = findNpcAssignment(npcAssignmentId);
        if (npc == null) return error("NPC assignment not found");

        if (line.getObjectiveIds().isEmpty()) {
            return error("Quest line has no objectives.");
        }

        // Register objective and quest line assets with the engine
        String objectiveId = line.getObjectiveIds().get(0);
        AuthoredObjective obj = findObjective(objectiveId);
        if (obj != null) {
            try {
                dev.hytalemodding.hyadventure.HyAdventurePlugin.get()
                        .getRegistrationService().registerObjective(obj);
                dev.hytalemodding.hyadventure.HyAdventurePlugin.get()
                        .getRegistrationService().registerQuestLine(line);
            } catch (Exception ignored) {}
        }

        line.setQuestGiverNpcId(npcAssignmentId);
        configManager.save();

        // UUID-based matching — the QuestInteractionListener handles quest triggering
        // at runtime via packet interception. No role file changes or NPC respawn needed.
        return Map.of("success", true, "message",
                "Quest giver assigned. NPC will offer the quest on next F-key interaction.");
    }

    public Map<String, Object> linkDialogToNpc(String npcAssignmentId, String dialogId) {
        AuthoredNpcAssignment npc = findNpcAssignment(npcAssignmentId);
        if (npc == null) return error("NPC assignment not found");
        npc.setDialogId(dialogId);
        configManager.save();
        return Map.of("success", true, "message", "Dialog linked to NPC");
    }

    @Nullable
    public AuthoredNpcAssignment findNpcAssignment(String id) {
        for (AuthoredNpcAssignment n : getData().getNpcAssignments())
            if (n.getId().equals(id)) return n;
        return null;
    }

    // ── Dialog CRUD ─────────────────────────────────────────────────────────

    public List<Map<String, Object>> listDialogs() {
        List<Map<String, Object>> r = new ArrayList<>();
        for (AuthoredDialog dlg : getData().getDialogs()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", dlg.getId());
            m.put("label", "Dialog: " + dlg.getId());
            r.add(m);
        }
        return r;
    }

    @Nullable
    public Map<String, Object> getDialogDetail(String id) {
        AuthoredDialog dlg = findDialog(id);
        if (dlg == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("dlg_id", dlg.getId());
        m.put("dlg_entityNameText", dlg.getEntityNameText());
        m.put("dlg_dialogText", dlg.getDialogText());
        m.put("dlg_entityNameKey", dlg.getEntityNameKey());
        m.put("dlg_dialogKey", dlg.getDialogKey());
        m.put("dlg_sequence", dlg.getSequence());
        return m;
    }

    public Map<String, Object> createDialog(Map<String, String> params) {
        String id = params.getOrDefault("id", "");
        if (id.isEmpty()) id = "dialog_" + (System.currentTimeMillis() % 100000);
        if (findDialog(id) != null) return error("Dialog already exists: " + id);

        AuthoredDialog dlg = new AuthoredDialog();
        dlg.setId(id);
        dlg.setEntityNameKey(params.getOrDefault("entityNameKey", id + "_name"));
        dlg.setDialogKey(params.getOrDefault("dialogKey", id + "_text"));
        dlg.setEntityNameText(params.getOrDefault("entityNameText", ""));
        dlg.setDialogText(params.getOrDefault("dialogText", ""));
        try { dlg.setSequence(Integer.parseInt(params.getOrDefault("sequence", "0"))); }
        catch (NumberFormatException ignored) {}
        getData().getDialogs().add(dlg);
        configManager.save();
        return Map.of("success", true, "entityId", id, "message", "Dialog created");
    }

    public Map<String, Object> deleteDialog(String id) {
        boolean removed = getData().getDialogs().removeIf(d -> d.getId().equals(id));
        if (!removed) return error("Dialog not found: " + id);
        configManager.save();
        return Map.of("success", true, "message", "Dialog deleted");
    }

    @Nullable
    public AuthoredDialog findDialog(String id) {
        for (AuthoredDialog d : getData().getDialogs())
            if (d.getId().equals(id)) return d;
        return null;
    }

    // ── Location CRUD ────────────────────────────────────────────────────────

    public List<Map<String, Object>> listAuthoredLocations() {
        List<Map<String, Object>> r = new ArrayList<>();
        for (AuthoredLocation loc : getData().getLocations()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", loc.getId());
            m.put("label", loc.getLabel().isEmpty() ? loc.getId() : loc.getLabel());
            m.put("x", (float) loc.getX());
            m.put("y", (float) loc.getY());
            m.put("z", (float) loc.getZ());
            r.add(m);
        }
        return r;
    }

    @Nullable
    public Map<String, Object> getAuthoredLocationDetail(String id) {
        AuthoredLocation loc = findLocation(id);
        if (loc == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("loc_id", loc.getId());
        m.put("loc_label", loc.getLabel());
        m.put("loc_x", loc.getX());
        m.put("loc_y", loc.getY());
        m.put("loc_z", loc.getZ());
        m.put("loc_radius", loc.getRadius());
        return m;
    }

    public Map<String, Object> createLocation(Map<String, String> params) {
        String id = params.getOrDefault("id", "");
        if (id.isEmpty()) id = "location_" + (System.currentTimeMillis() % 100000);
        if (findLocation(id) != null) return error("Location already exists: " + id);

        AuthoredLocation loc = new AuthoredLocation();
        loc.setId(id);
        loc.setLabel(params.getOrDefault("label", id));
        try { loc.setX(Double.parseDouble(params.getOrDefault("x", "0"))); } catch (NumberFormatException ignored) {}
        try { loc.setY(Double.parseDouble(params.getOrDefault("y", "64"))); } catch (NumberFormatException ignored) {}
        try { loc.setZ(Double.parseDouble(params.getOrDefault("z", "0"))); } catch (NumberFormatException ignored) {}
        try { loc.setRadius(Float.parseFloat(params.getOrDefault("radius", "5.0"))); } catch (NumberFormatException ignored) {}
        getData().getLocations().add(loc);
        configManager.save();
        return Map.of("success", true, "entityId", id, "message", "Location created");
    }

    public Map<String, Object> deleteLocation(String id) {
        boolean removed = getData().getLocations().removeIf(l -> l.getId().equals(id));
        if (!removed) return error("Location not found: " + id);
        configManager.save();
        return Map.of("success", true, "message", "Location deleted");
    }

    @Nullable
    public AuthoredLocation findLocation(String id) {
        for (AuthoredLocation l : getData().getLocations())
            if (l.getId().equals(id)) return l;
        return null;
    }

    // ── Lookup helpers ───────────────────────────────────────────────────────

    @Nullable
    public AuthoredQuestLine findLine(String id) {
        for (AuthoredQuestLine l : getData().getQuestLines())
            if (l.getId().equals(id)) return l;
        return null;
    }

    @Nullable
    public AuthoredObjective findObjective(String id) {
        for (AuthoredObjective o : getData().getObjectives())
            if (o.getId().equals(id)) return o;
        return null;
    }

    private static String toSnakeCase(String input) {
        return input.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
    }

    private static Map<String, Object> error(String msg) {
        return Map.of("success", false, "error", msg);
    }
}
