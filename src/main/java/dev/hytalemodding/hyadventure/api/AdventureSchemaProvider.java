package dev.hytalemodding.hyadventure.api;

import dev.hytalemodding.api.DashboardSchemaProvider;
import dev.hytalemodding.hyadventure.HyAdventurePlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AdventureSchemaProvider implements DashboardSchemaProvider {

    private static final String[] QUEST_CATEGORIES = {"main_quest", "side_quest", "daily", "custom"};
    private static final String[] TASK_TYPES = {
            "GATHER", "CRAFT", "USE_BLOCK", "USE_ENTITY",
            "REACH_LOCATION", "KILL", "KILL_SPAWN_MARKER", "BOUNTY"
    };
    private static final String[] COMPLETION_TYPES = {"GIVE_ITEMS", "CLEAR_OBJECTIVE_ITEMS", "REPUTATION"};
    private static final String[] NPC_ASSIGNMENT_TYPES = {"QUEST_GIVER", "INTERACT", "KILL_TARGET", "BOUNTY_TARGET"};

    private final HyAdventurePlugin plugin;

    public AdventureSchemaProvider(@Nonnull HyAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    @Nonnull @Override public String getPluginId() { return "hyadventure"; }
    @Nonnull @Override public String getPluginName() { return "HyAdventure"; }
    @Nonnull @Override public String getVersion() { return "1.0.0"; }
    @Nonnull @Override public String getEntityType() { return "adventureEntity"; }
    @Nonnull @Override public String getEntityLabel() { return "Adventure Data"; }

    @Nonnull @Override
    public Map<String, Object> buildSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("pluginId", getPluginId());
        schema.put("pluginName", getPluginName());
        schema.put("version", getVersion());
        schema.put("entityType", getEntityType());
        schema.put("entityLabel", getEntityLabel());

        // Dynamic enum values from authored data (must be before field groups that reference them)
        String[] lineIds = plugin.getAuthoringService().getData().getQuestLines().stream()
                .map(l -> l.getId()).toArray(String[]::new);
        String[] objectiveIds = plugin.getAuthoringService().getData().getObjectives().stream()
                .map(o -> o.getId()).toArray(String[]::new);
        String[] npcRoles = plugin.getNpcObjectiveService().listAvailableNpcRoles().stream()
                .map(m -> m.get("id").toString()).toArray(String[]::new);
        String[] npcAssignmentIds = plugin.getAuthoringService().getData().getNpcAssignments().stream()
                .map(n -> n.getId()).toArray(String[]::new);
        String[] dialogIds = plugin.getAuthoringService().getData().getDialogs().stream()
                .map(d -> d.getId()).toArray(String[]::new);
        String[] locationIds = plugin.getAuthoringService().getData().getLocations().stream()
                .map(l -> l.getId()).toArray(String[]::new);

        // Field groups per submodule
        schema.put("groups", List.of(
                fieldGroup("objectiveDetail", "Objective Detail", 0, List.of(
                        field("id", "ID", "string", true),
                        field("category", "Category", "string", true),
                        field("titleKey", "Title Key", "string", true),
                        field("descriptionKey", "Description Key", "string", true),
                        field("taskSetCount", "Task Sets", "int", true),
                        field("completionCount", "Completions", "int", true),
                        field("removeOnItemDrop", "Remove On Drop", "bool", true)
                )),
                fieldGroup("activeObjectiveDetail", "Active Objective", 1, List.of(
                        field("objectiveId", "Objective ID", "string", true),
                        field("completed", "Completed", "bool", true),
                        field("taskSetIndex", "Current Phase", "int", true),
                        field("description", "Description", "string", true),
                        field("playerCount", "Players", "int", true)
                )),
                fieldGroup("reputationGroupDetail", "Reputation Group", 2, List.of(
                        field("id", "ID", "string", true),
                        field("npcGroups", "NPC Groups", "stringList", true),
                        field("initialReputationValue", "Initial Value", "int", true)
                )),
                fieldGroup("reputationRankDetail", "Reputation Rank", 3, List.of(
                        field("id", "ID", "string", true),
                        field("minValue", "Min Value", "int", true),
                        field("maxValue", "Max Value", "int", true),
                        field("attitude", "Attitude", "string", true)
                )),
                fieldGroup("shopDetail", "Shop", 4, List.of(
                        field("id", "ID", "string", true),
                        field("elementCount", "Elements", "int", true)
                )),
                fieldGroup("barterShopDetail", "Barter Shop", 5, List.of(
                        field("id", "ID", "string", true),
                        field("displayNameKey", "Display Name", "string", true),
                        field("restockHour", "Restock Hour", "int", true),
                        field("tradeCount", "Trades", "int", true)
                )),
                fieldGroup("farmingDetail", "Farming Coop", 6, List.of(
                        field("id", "ID", "string", true),
                        field("maxResidents", "Max Residents", "int", true),
                        field("wildCaptureRadius", "Capture Radius", "float", true),
                        field("captureWildNPCsInRange", "Auto Capture", "bool", true)
                )),
                fieldGroup("memoryDetail", "Memory", 7, List.of(
                        field("id", "ID", "string", true),
                        field("provider", "Provider", "string", true),
                        field("isRecorded", "Recorded", "bool", true)
                )),
                fieldGroup("cameraDetail", "Camera Shake", 8, List.of(
                        field("id", "ID", "string", true)
                )),
                fieldGroup("teleporterDetail", "Teleporter", 9, List.of(
                        field("warp", "Warp", "string", true),
                        field("ownedWarp", "Owned Warp", "string", true),
                        field("customName", "Custom Name", "bool", true),
                        field("worldUuid", "World", "string", true),
                        field("x", "X", "float", true),
                        field("y", "Y", "float", true),
                        field("z", "Z", "float", true)
                )),
                fieldGroup("npcObjectiveDetail", "NPC Objective", 10, List.of(
                        field("objectiveId", "Objective ID", "string", true),
                        field("objectiveUUID", "UUID", "string", true)
                )),
                fieldGroup("stashDetail", "Stash", 11, List.of(
                        field("description", "Description", "string", true),
                        field("clearContainerDropList", "Clear Drop List", "bool", true)
                )),
                fieldGroup("authoredLineDetail", "Quest Line", 12, List.of(
                        field("line_id", "ID", "string", true),
                        field("line_category", "Category", "string", true),
                        field("line_titleKey", "Title Key", "string"),
                        field("line_descriptionKey", "Description Key", "string"),
                        field("line_objectiveIds", "Objectives (ordered)", "stringList", true),
                        field("line_nextQuestLineIds", "Branches (follow-ups)", "stringList", true),
                        field("line_questGiverNpcId", "Quest Giver NPC", "string", true)
                )),
                fieldGroup("authoredObjDetail", "Authored Objective", 13, List.of(
                        field("aobj_id", "ID", "string", true),
                        field("aobj_category", "Category", "string", true),
                        field("aobj_titleKey", "Title Key", "string"),
                        field("aobj_descriptionKey", "Description Key", "string"),
                        field("aobj_removeOnItemDrop", "Remove On Drop", "bool"),
                        field("aobj_taskSetCount", "Phases", "int", true),
                        field("aobj_completionCount", "Rewards", "int", true)
                )),
                fieldGroup("npcAssignmentDetail", "NPC Assignment", 14, List.of(
                        field("npc_id", "ID", "string", true),
                        enumField("npc_role", "NPC Role", npcRoles),
                        enumField("npc_type", "Assignment Type", NPC_ASSIGNMENT_TYPES),
                        field("npc_dialogId", "Dialog", "string", true),
                        field("npc_locationId", "Location Filter", "string", true)
                )),
                fieldGroup("dialogDetail", "Dialog", 15, List.of(
                        field("dlg_id", "ID", "string", true),
                        field("dlg_entityNameText", "NPC Display Name", "string"),
                        field("dlg_dialogText", "Dialog Text", "string"),
                        field("dlg_entityNameKey", "Name Key", "string", true),
                        field("dlg_dialogKey", "Dialog Key", "string", true),
                        intField("dlg_sequence", "Sequence", 0, 99)
                )),
                fieldGroup("locationDetail", "Location", 16, List.of(
                        field("loc_id", "ID", "string", true),
                        field("loc_label", "Label", "string"),
                        field("loc_x", "X", "float", true),
                        field("loc_y", "Y", "float", true),
                        field("loc_z", "Z", "float", true),
                        field("loc_radius", "Radius", "float")
                ))
        ));

        // Sub-modules for client tab rendering
        schema.put("subModules", List.of(
                subModule("objectives", "Objectives", "objective", "Objectives"),
                subModule("reputation", "Reputation", "reputationGroup", "Reputation Groups"),
                subModule("shops", "Shops", "shop", "Shops"),
                subModule("farming", "Farming", "coop", "Farming Coops"),
                subModule("memories", "Memories", "memory", "Memories"),
                subModule("camera", "Camera", "cameraShake", "Camera Shakes"),
                subModule("teleporters", "Teleporters", "teleporter", "Teleporters"),
                subModule("npcobjectives", "NPC Quests", "npcObjective", "NPC Objectives"),
                subModule("stash", "Stash", "stash", "Stash"),
                subModule("authoring", "Quest Editor", "quest", "Authored Quests")
        ));

        // Mutation actions (tagged by subModule for client tab filtering)
        schema.put("actions", List.of(
                paramAction("startObjective", "Start Objective", false, "objectives", List.of(
                        field("objectiveId", "Objective ID", "string"),
                        field("playerUuids", "Player UUIDs (comma-sep)", "string"),
                        field("worldId", "World", "string")
                )),
                paramAction("cancelObjective", "Cancel Objective", false, "objectives", List.of(
                        field("objectiveUuid", "Objective UUID", "string")
                )),
                paramAction("completeObjective", "Complete Objective", false, "objectives", List.of(
                        field("objectiveUuid", "Objective UUID", "string")
                )),
                paramAction("changeReputation", "Change Reputation", false, "reputation", List.of(
                        field("worldId", "World", "string"),
                        field("groupId", "Reputation Group", "string"),
                        field("delta", "Delta", "int")
                )),
                paramAction("recordAllMemories", "Record All Memories", false, "memories", List.of()),
                paramAction("clearMemories", "Clear Memories", false, "memories", List.of()),
                paramAction("setMemoryCount", "Set Memory Count", false, "memories", List.of(
                        field("count", "Count", "int")
                )),
                paramAction("startNPCObjective", "Start NPC Objective", false, "npcobjectives", List.of(
                        field("entityUuid", "Entity UUID", "string"),
                        field("objectiveId", "Objective ID", "string"),
                        field("worldId", "World", "string")
                )),
                // ── Quest Editor actions (dynamic enums from authored data) ──
                paramAction("createFullQuest", "Create Quest", false, "authoring", List.of(
                        requiredField("name", "Quest Name", "string"),
                        enumField("category", "Category", QUEST_CATEGORIES),
                        enumField("taskType", "First Task Type", TASK_TYPES),
                        field("taskTarget", "Task Target (item/npc/location)", "string"),
                        intField("taskCount", "Count", 1, 999)
                )),
                paramAction("createQuestLine", "Create Quest Line", false, "authoring", List.of(

                        enumField("category", "Category", QUEST_CATEGORIES),
                        field("titleKey", "Title Key", "string"),
                        field("descriptionKey", "Description Key", "string")
                )),
                paramAction("createObjective", "Create Objective", false, "authoring", List.of(

                        enumField("category", "Category", QUEST_CATEGORIES),
                        field("titleKey", "Title Key", "string"),
                        field("descriptionKey", "Description Key", "string"),
                        field("removeOnItemDrop", "Remove On Drop", "bool")
                )),
                paramAction("addObjectiveToLine", "Link Objective to Line", false, "authoring", List.of(
                        enumField("questLineId", "Quest Line", lineIds),
                        enumField("objectiveId", "Objective", objectiveIds),
                        intField("position", "Position (0-based)", 0, 50)
                )),
                paramAction("addBranch", "Add Branch (Follow-up Line)", false, "authoring", List.of(
                        enumField("questLineId", "Source Quest Line", lineIds),
                        enumField("nextLineId", "Follow-up Quest Line", lineIds)
                )),
                paramAction("addTaskSet", "Add Phase (Task Set)", false, "authoring", List.of(
                        enumField("objectiveId", "Objective", objectiveIds),
                        field("descriptionId", "Phase Description", "string")
                )),
                paramAction("addTask", "Add Task to Phase", false, "authoring", List.of(
                        enumField("objectiveId", "Objective", objectiveIds),
                        intField("taskSetIndex", "Phase Index", 0, 50),
                        enumField("type", "Task Type", TASK_TYPES),
                        intField("count", "Required Count", 1, 999),
                        field("blockTagOrItemId", "Block/Item ID (Gather, Craft, UseBlock)", "string"),
                        field("taskId", "Task ID (UseEntity)", "string"),
                        field("animationIdToPlay", "Animation (UseEntity)", "string"),
                        field("dialogEntityNameKey", "Dialog NPC Name Key (UseEntity)", "string"),
                        field("dialogKey", "Dialog Text Key (UseEntity)", "string"),
                        field("targetLocationId", "Location ID (ReachLocation)", "string"),
                        field("npcGroupId", "NPC Group (Kill, KillSpawnMarker)", "string"),
                        field("npcId", "NPC ID (Bounty)", "string"),
                        field("spawnMarkerIds", "Spawn Markers (comma-sep, KillSpawnMarker)", "string"),
                        field("radius", "Radius (KillSpawnMarker)", "float")
                )),
                paramAction("addCompletion", "Add Reward", false, "authoring", List.of(
                        enumField("objectiveId", "Objective", objectiveIds),
                        enumField("type", "Reward Type", COMPLETION_TYPES),
                        field("dropListId", "Drop List ID (GiveItems)", "string"),
                        field("reputationGroupId", "Reputation Group (Reputation)", "string"),
                        intField("reputationAmount", "Reputation Amount", -1000, 1000)
                )),
                paramAction("deleteQuestLine", "Delete Quest Line", false, "authoring", List.of(
                        enumField("questLineId", "Quest Line", lineIds)
                )),
                paramAction("deleteObjective", "Delete Objective", false, "authoring", List.of(
                        enumField("objectiveId", "Objective", objectiveIds)
                )),
                // NPC Assignment actions
                paramAction("createNpcAssignment", "Create NPC Assignment", false, "authoring", List.of(

                        enumField("assignmentType", "Assignment Type", NPC_ASSIGNMENT_TYPES)
                )),
                paramAction("deleteNpcAssignment", "Delete NPC Assignment", false, "authoring", List.of(
                        enumField("npcAssignmentId", "NPC Assignment", npcAssignmentIds)
                )),
                paramAction("setQuestGiver", "Set Quest Giver", false, "authoring", List.of(
                        enumField("questLineId", "Quest Line", lineIds),
                        enumField("npcAssignmentId", "NPC Assignment", npcAssignmentIds)
                )),
                paramAction("linkDialogToNpc", "Link Dialog to NPC", false, "authoring", List.of(
                        enumField("npcAssignmentId", "NPC Assignment", npcAssignmentIds),
                        enumField("dialogId", "Dialog", dialogIds)
                )),
                // Dialog actions
                paramAction("createDialog", "Create Dialog", false, "authoring", List.of(

                        field("entityNameKey", "NPC Name Key", "string"),
                        field("dialogKey", "Dialog Text Key", "string"),
                        intField("sequence", "Sequence", 0, 99)
                )),
                paramAction("deleteDialog", "Delete Dialog", false, "authoring", List.of(
                        enumField("dialogId", "Dialog", dialogIds)
                )),
                // Location actions
                paramAction("createLocation", "Create Location", false, "authoring", List.of(

                        field("label", "Label", "string"),
                        field("x", "X", "float"),
                        field("y", "Y", "float"),
                        field("z", "Z", "float"),
                        field("radius", "Radius", "float")
                )),
                paramAction("deleteLocation", "Delete Location", false, "authoring", List.of(
                        enumField("locationId", "Location", locationIds)
                )),
                paramAction("rebuild", "Rebuild All", false, "authoring", List.of()),
                paramAction("generateTemplates", "Generate Quest Templates", false, "authoring", List.of()),
                paramAction("registerAll", "Re-register All Assets", false, "authoring", List.of())
        ));

        // Graph hints for visual node editor
        schema.put("graphHints", Map.of(
                "nodeTypes", List.of(
                        Map.of(
                                "groupId", "authoredLineDetail",
                                "entityPrefix", "auth-line",
                                "label", "Quest Line",
                                "headerColor", "#3A6BC5",
                                "ports", List.of(
                                        Map.of("portId", "flow_in", "fieldId", "", "label", "From",
                                                "direction", "input", "portType", "questflow", "color", "#E8943A", "multiLink", true),
                                        Map.of("portId", "objectives_out", "fieldId", "line_objectiveIds", "label", "Objectives",
                                                "direction", "output", "portType", "objective_link", "color", "#4ACC7A", "multiLink", true),
                                        Map.of("portId", "flow_out", "fieldId", "line_nextQuestLineIds", "label", "Next Lines",
                                                "direction", "output", "portType", "questflow", "color", "#E8943A", "multiLink", true),
                                        Map.of("portId", "questgiver_out", "fieldId", "line_questGiverNpcId", "label", "Quest Giver",
                                                "direction", "output", "portType", "npc_questgiver", "color", "#C54B8C", "multiLink", false)
                                )
                        ),
                        Map.of(
                                "groupId", "authoredObjDetail",
                                "entityPrefix", "auth-obj",
                                "label", "Objective",
                                "headerColor", "#2FA85A",
                                "ports", List.of(
                                        Map.of("portId", "obj_in", "fieldId", "", "label", "From Line",
                                                "direction", "input", "portType", "objective_link", "color", "#4ACC7A", "multiLink", true),
                                        Map.of("portId", "npc_interact_out", "fieldId", "", "label", "NPC Interact",
                                                "direction", "output", "portType", "npc_interact", "color", "#C54B8C", "multiLink", false),
                                        Map.of("portId", "reach_loc_out", "fieldId", "", "label", "Location",
                                                "direction", "output", "portType", "location_link", "color", "#5B8DD9", "multiLink", false)
                                )
                        ),
                        Map.of(
                                "groupId", "npcAssignmentDetail",
                                "entityPrefix", "npc-assign",
                                "label", "NPC",
                                "headerColor", "#C54B8C",
                                "ports", List.of(
                                        Map.of("portId", "npc_giver_in", "fieldId", "", "label", "Quest Giver",
                                                "direction", "input", "portType", "npc_questgiver", "color", "#C54B8C", "multiLink", true),
                                        Map.of("portId", "npc_interact_in", "fieldId", "", "label", "Interact",
                                                "direction", "input", "portType", "npc_interact", "color", "#C54B8C", "multiLink", true),
                                        Map.of("portId", "dialog_out", "fieldId", "npc_dialogId", "label", "Dialog",
                                                "direction", "output", "portType", "dialog_link", "color", "#D4A843", "multiLink", false),
                                        Map.of("portId", "loc_filter_out", "fieldId", "npc_locationId", "label", "Location",
                                                "direction", "output", "portType", "location_link", "color", "#5B8DD9", "multiLink", false)
                                )
                        ),
                        Map.of(
                                "groupId", "dialogDetail",
                                "entityPrefix", "dlg",
                                "label", "Dialog",
                                "headerColor", "#D4A843",
                                "ports", List.of(
                                        Map.of("portId", "dlg_in", "fieldId", "", "label", "From NPC",
                                                "direction", "input", "portType", "dialog_link", "color", "#D4A843", "multiLink", true)
                                )
                        ),
                        Map.of(
                                "groupId", "locationDetail",
                                "entityPrefix", "loc",
                                "label", "Location",
                                "headerColor", "#5B8DD9",
                                "ports", List.of(
                                        Map.of("portId", "loc_in", "fieldId", "", "label", "Target",
                                                "direction", "input", "portType", "location_link", "color", "#5B8DD9", "multiLink", true)
                                )
                        )
                ),
                "connectionRules", List.of(
                        Map.of("outputType", "questflow", "inputType", "questflow"),
                        Map.of("outputType", "objective_link", "inputType", "objective_link"),
                        Map.of("outputType", "npc_questgiver", "inputType", "npc_questgiver"),
                        Map.of("outputType", "npc_interact", "inputType", "npc_interact"),
                        Map.of("outputType", "dialog_link", "inputType", "dialog_link"),
                        Map.of("outputType", "location_link", "inputType", "location_link")
                )
        ));

        return schema;
    }

    @Nonnull @Override
    public List<Map<String, Object>> listEntities() {
        List<Map<String, Object>> result = new ArrayList<>();

        addFromService(result, "objectives", "obj", () -> plugin.getObjectiveService().listObjectiveAssets());
        addFromService(result, "objectives", "obj-active", () -> plugin.getObjectiveService().listActiveObjectives());
        addFromService(result, "reputation", "rep-group", () -> plugin.getReputationService().listGroups());
        addFromService(result, "reputation", "rep-rank", () -> plugin.getReputationService().listRanks());
        addFromService(result, "shops", "shop", () -> plugin.getShopService().listShops());
        addFromService(result, "shops", "barter", () -> plugin.getShopService().listBarterShops());
        addFromService(result, "farming", "farm", () -> plugin.getFarmingService().listCoops());
        addFromService(result, "memories", "mem", () -> plugin.getMemoriesService().listAllMemoriesFlat());
        addFromService(result, "camera", "cam", () -> plugin.getCameraService().listCameraShakes());
        addFromService(result, "teleporters", "tp", () -> plugin.getTeleporterService().listTeleporters("default"));
        addFromService(result, "npcobjectives", "npc", () -> plugin.getNpcObjectiveService().listActiveNPCObjectives("default"));
        addFromService(result, "stash", "stash", () -> List.of(Map.of("id", "info", "label", "Stash System")));
        addFromService(result, "authoring", "auth-line", () -> plugin.getAuthoringService().listQuestLines());
        addFromService(result, "authoring", "auth-obj", () -> plugin.getAuthoringService().listObjectives());
        addFromService(result, "authoring", "npc-assign", () -> plugin.getAuthoringService().listNpcAssignments());
        addFromService(result, "authoring", "dlg", () -> plugin.getAuthoringService().listDialogs());
        addFromService(result, "authoring", "loc", () -> plugin.getAuthoringService().listAuthoredLocations());

        return result;
    }

    @Nonnull @Override
    public Map<String, Object> getEntityValues(@Nonnull String entityId) {
        int sep = entityId.indexOf(':');
        if (sep < 0) return Map.of();

        String prefix = entityId.substring(0, sep);
        String rawId = entityId.substring(sep + 1);

        Map<String, Object> detail = switch (prefix) {
            case "obj" -> plugin.getObjectiveService().getObjectiveAssetDetail(rawId);
            case "obj-active" -> plugin.getObjectiveService().getActiveObjectiveDetail(rawId);
            case "rep-group" -> plugin.getReputationService().getGroupDetail(rawId);
            case "rep-rank" -> plugin.getReputationService().getRankDetail(rawId);
            case "shop" -> plugin.getShopService().getShopDetail(rawId);
            case "barter" -> plugin.getShopService().getBarterShopDetail(rawId);
            case "farm" -> plugin.getFarmingService().getCoopDetail(rawId);
            case "mem" -> plugin.getMemoriesService().getMemoryDetail(rawId);
            case "cam" -> plugin.getCameraService().getCameraShakeDetail(rawId);
            case "tp" -> plugin.getTeleporterService().getTeleporterDetail(rawId);
            case "npc" -> null;
            case "stash" -> plugin.getStashService().getStashInfo();
            case "auth-line" -> plugin.getAuthoringService().getQuestLineDetail(rawId);
            case "auth-obj" -> plugin.getAuthoringService().getObjectiveDetail(rawId);
            case "npc-assign" -> plugin.getAuthoringService().getNpcAssignmentDetail(rawId);
            case "dlg" -> plugin.getAuthoringService().getDialogDetail(rawId);
            case "loc" -> plugin.getAuthoringService().getAuthoredLocationDetail(rawId);
            default -> null;
        };

        if (detail == null) return Map.of();
        return wrapValues(entityId, detail);
    }

    @Nonnull @Override
    public List<String> updateEntity(@Nonnull String entityId, @Nonnull Map<String, String> values) {
        int sep = entityId.indexOf(':');
        if (sep < 0) return List.of("Invalid entity ID");
        String prefix = entityId.substring(0, sep);
        String rawId = entityId.substring(sep + 1);

        return switch (prefix) {
            case "npc-assign" -> updateNpcAssignment(rawId, values);
            case "dlg" -> updateDialog(rawId, values);
            case "auth-line" -> updateQuestLine(rawId, values);
            case "loc" -> updateLocation(rawId, values);
            default -> List.of("This entity type is read-only.");
        };
    }

    private List<String> updateNpcAssignment(String id, Map<String, String> values) {
        var npc = plugin.getAuthoringService().findNpcAssignment(id);
        if (npc == null) return List.of("NPC assignment not found");
        if (values.containsKey("npc_role")) npc.setNpcRole(values.get("npc_role"));
        if (values.containsKey("npc_type")) {
            try { npc.setAssignmentType(dev.hytalemodding.hyadventure.models.NpcAssignmentType.valueOf(values.get("npc_type"))); }
            catch (IllegalArgumentException ignored) {}
        }
        if (values.containsKey("npc_dialogId")) npc.setDialogId(values.get("npc_dialogId"));
        if (values.containsKey("npc_locationId")) npc.setLocationId(values.get("npc_locationId"));
        plugin.getAuthoringService().save();
        return List.of();
    }

    private List<String> updateDialog(String id, Map<String, String> values) {
        var dlg = plugin.getAuthoringService().findDialog(id);
        if (dlg == null) return List.of("Dialog not found");
        if (values.containsKey("dlg_entityNameText")) dlg.setEntityNameText(values.get("dlg_entityNameText"));
        if (values.containsKey("dlg_dialogText")) dlg.setDialogText(values.get("dlg_dialogText"));
        if (values.containsKey("dlg_entityNameKey")) dlg.setEntityNameKey(values.get("dlg_entityNameKey"));
        if (values.containsKey("dlg_dialogKey")) dlg.setDialogKey(values.get("dlg_dialogKey"));
        if (values.containsKey("dlg_sequence")) {
            try { dlg.setSequence(Integer.parseInt(values.get("dlg_sequence"))); }
            catch (NumberFormatException ignored) {}
        }
        plugin.getAuthoringService().save();
        return List.of();
    }

    private List<String> updateQuestLine(String id, Map<String, String> values) {
        var line = plugin.getAuthoringService().findLine(id);
        if (line == null) return List.of("Quest line not found");
        if (values.containsKey("line_titleKey")) line.setTitleKey(values.get("line_titleKey"));
        if (values.containsKey("line_descriptionKey")) line.setDescriptionKey(values.get("line_descriptionKey"));
        if (values.containsKey("line_questGiverNpcId")) line.setQuestGiverNpcId(values.get("line_questGiverNpcId"));
        plugin.getAuthoringService().save();
        return List.of();
    }

    private List<String> updateLocation(String id, Map<String, String> values) {
        var loc = plugin.getAuthoringService().findLocation(id);
        if (loc == null) return List.of("Location not found");
        if (values.containsKey("loc_label")) loc.setLabel(values.get("loc_label"));
        if (values.containsKey("loc_radius")) {
            try { loc.setRadius(Float.parseFloat(values.get("loc_radius"))); }
            catch (NumberFormatException ignored) {}
        }
        plugin.getAuthoringService().save();
        return List.of();
    }

    @Nonnull @Override
    public Map<String, Object> executeAction(@Nonnull String actionId, @Nullable String entityId,
                                              @Nonnull Map<String, String> params) {
        return switch (actionId) {
            case "startObjective" -> plugin.getObjectiveService().startObjective(
                    params.get("objectiveId"), params.getOrDefault("playerUuids", ""),
                    params.getOrDefault("worldId", "default"));
            case "cancelObjective" -> plugin.getObjectiveService().cancelObjective(
                    params.get("objectiveUuid"));
            case "completeObjective" -> plugin.getObjectiveService().completeObjective(
                    params.get("objectiveUuid"));
            case "changeReputation" -> plugin.getReputationService().changeReputation(
                    params.getOrDefault("worldId", "default"), params.get("groupId"),
                    Integer.parseInt(params.getOrDefault("delta", "0")));
            case "recordAllMemories" -> plugin.getMemoriesService().recordAll();
            case "clearMemories" -> plugin.getMemoriesService().clearAll();
            case "setMemoryCount" -> plugin.getMemoriesService().setCount(
                    Integer.parseInt(params.getOrDefault("count", "0")));
            case "startNPCObjective" -> plugin.getNpcObjectiveService().startNPCObjective(
                    params.get("entityUuid"), params.get("objectiveId"),
                    params.getOrDefault("worldId", "default"));
            // Quest Editor actions
            case "createFullQuest" -> plugin.getAuthoringService().createFullQuest(params);
            case "createQuestLine" -> {
                var result = plugin.getAuthoringService().createQuestLine(params);
                if (Boolean.TRUE.equals(result.get("success"))) {
                    var line = plugin.getAuthoringService().findLine(params.get("id"));
                    if (line != null) plugin.getRegistrationService().registerQuestLine(line);
                }
                yield result;
            }
            case "createObjective" -> {
                var result = plugin.getAuthoringService().createObjective(params);
                if (Boolean.TRUE.equals(result.get("success"))) {
                    var obj = plugin.getAuthoringService().findObjective(params.get("id"));
                    if (obj != null) plugin.getRegistrationService().registerObjective(obj);
                }
                yield result;
            }
            case "deleteQuestLine" -> plugin.getAuthoringService().deleteQuestLine(params.get("questLineId"));
            case "deleteObjective" -> plugin.getAuthoringService().deleteObjective(params.get("objectiveId"));
            case "addObjectiveToLine" -> {
                int pos = -1;
                try { pos = Integer.parseInt(params.getOrDefault("position", "-1")); } catch (NumberFormatException ignored) {}
                var result = plugin.getAuthoringService().addObjectiveToLine(
                        params.get("questLineId"), params.get("objectiveId"), pos);
                if (Boolean.TRUE.equals(result.get("success"))) {
                    var line = plugin.getAuthoringService().findLine(params.get("questLineId"));
                    if (line != null) plugin.getRegistrationService().registerQuestLine(line);
                }
                yield result;
            }
            case "addBranch" -> {
                var result = plugin.getAuthoringService().addBranch(params.get("questLineId"), params.get("nextLineId"));
                if (Boolean.TRUE.equals(result.get("success"))) {
                    var line = plugin.getAuthoringService().findLine(params.get("questLineId"));
                    if (line != null) plugin.getRegistrationService().registerQuestLine(line);
                }
                yield result;
            }
            case "addTaskSet" -> {
                var result = plugin.getAuthoringService().addTaskSet(params.get("objectiveId"), params);
                if (Boolean.TRUE.equals(result.get("success"))) {
                    var obj = plugin.getAuthoringService().findObjective(params.get("objectiveId"));
                    if (obj != null) plugin.getRegistrationService().registerObjective(obj);
                }
                yield result;
            }
            case "addTask" -> {
                int tsIdx = 0;
                try { tsIdx = Integer.parseInt(params.getOrDefault("taskSetIndex", "0")); } catch (NumberFormatException ignored) {}
                var result = plugin.getAuthoringService().addTask(params.get("objectiveId"), tsIdx, params);
                if (Boolean.TRUE.equals(result.get("success"))) {
                    var obj = plugin.getAuthoringService().findObjective(params.get("objectiveId"));
                    if (obj != null) plugin.getRegistrationService().registerObjective(obj);
                }
                yield result;
            }
            case "addCompletion" -> {
                var result = plugin.getAuthoringService().addCompletion(params.get("objectiveId"), params);
                if (Boolean.TRUE.equals(result.get("success"))) {
                    var obj = plugin.getAuthoringService().findObjective(params.get("objectiveId"));
                    if (obj != null) plugin.getRegistrationService().registerObjective(obj);
                }
                yield result;
            }
            case "createNpcAssignment" -> plugin.getAuthoringService().createNpcAssignment(params);
            case "deleteNpcAssignment" -> plugin.getAuthoringService().deleteNpcAssignment(params.get("npcAssignmentId"));
            case "setQuestGiver" -> plugin.getAuthoringService().setQuestGiver(params.get("questLineId"), params.get("npcAssignmentId"));
            case "linkDialogToNpc" -> plugin.getAuthoringService().linkDialogToNpc(params.get("npcAssignmentId"), params.get("dialogId"));
            case "createDialog" -> plugin.getAuthoringService().createDialog(params);
            case "deleteDialog" -> plugin.getAuthoringService().deleteDialog(params.get("dialogId"));
            case "createLocation" -> plugin.getAuthoringService().createLocation(params);
            case "deleteLocation" -> plugin.getAuthoringService().deleteLocation(params.get("locationId"));
            case "generateTemplates" -> plugin.getTemplateService().generateAllTemplates();
            case "generateTemplate" -> plugin.getTemplateService().generateSingleTemplate(params.getOrDefault("templateId", ""));
            case "registerAll" -> {
                plugin.getRegistrationService().registerAllOnStartup();
                yield Map.of("success", (Object) true, "message", (Object) "All authored assets re-registered");
            }
            case "rebuild" -> {
                // Full rebuild: clear stale data, fix invalid tasks, re-register, regenerate translations
                int cleared = clearStaleObjectives();
                int fixed = fixInvalidTasks();
                plugin.getAuthoringService().save();
                plugin.getRegistrationService().registerAllOnStartup();
                plugin.getTranslationService().regenerate();
                yield Map.of("success", (Object) true, "message",
                        (Object) ("Rebuild complete. Cleared " + cleared + " stale objective(s), fixed " + fixed + " invalid task(s), re-registered all assets."));
            }
            default -> Map.of("success", false, "errors", List.of("Unknown action: " + actionId));
        };
    }

    private int fixInvalidTasks() {
        int fixed = 0;
        for (var obj : plugin.getAuthoringService().getData().getObjectives()) {
            for (var ts : obj.getTaskSets()) {
                for (var task : ts.getTasks()) {
                    // REACH_LOCATION with empty targetLocationId is invalid
                    if (task.getType() == dev.hytalemodding.hyadventure.models.TaskType.REACH_LOCATION
                            && (task.getTargetLocationId() == null || task.getTargetLocationId().isEmpty())) {
                        task.setType(dev.hytalemodding.hyadventure.models.TaskType.USE_ENTITY);
                        task.setTaskId(obj.getId() + "_interact");
                        fixed++;
                    }
                }
            }
            // Ensure at least one task set exists
            if (obj.getTaskSets().isEmpty()) {
                var ts = new dev.hytalemodding.hyadventure.models.AuthoredTaskSet();
                ts.setDescriptionId(obj.getId() + "_phase_0");
                var task = new dev.hytalemodding.hyadventure.models.AuthoredTask();
                task.setType(dev.hytalemodding.hyadventure.models.TaskType.USE_ENTITY);
                task.setTaskId(obj.getId() + "_interact");
                task.setCount(1);
                task.setDescriptionId(obj.getId() + "_task_0");
                ts.getTasks().add(task);
                obj.getTaskSets().add(ts);
                fixed++;
            }
        }
        return fixed;
    }

    private int clearStaleObjectives() {
        try {
            java.io.File objDir = new java.io.File("universe/objectives");
            if (!objDir.exists()) return 0;
            java.io.File[] files = objDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files == null) return 0;
            int count = 0;
            for (java.io.File f : files) {
                f.delete();
                count++;
            }
            return count;
        } catch (Exception e) { return 0; }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static Map<String, Object> subModule(String id, String label, String entityType, String entityLabel) {
        return Map.of("id", id, "label", label, "entityType", entityType, "entityLabel", entityLabel);
    }

    private static Map<String, Object> paramAction(String id, String label, boolean requiresEntity,
                                                     String subModule, List<Map<String, Object>> fields) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("id", id); action.put("label", label); action.put("requiresEntity", requiresEntity);
        if (subModule != null) action.put("subModule", subModule);
        if (fields.isEmpty()) {
            action.put("groups", List.of());
        } else {
            action.put("groups", List.of(Map.of("id", "params", "label", "Parameters", "order", 0, "fields", fields)));
        }
        return action;
    }

    private static void addFromService(List<Map<String, Object>> result, String subModule, String prefix,
                                        java.util.function.Supplier<List<Map<String, Object>>> supplier) {
        try {
            for (var item : supplier.get()) {
                Map<String, Object> entry = new LinkedHashMap<>(item);
                String rawId = Objects.toString(entry.get("id"), "");
                entry.put("id", prefix + ":" + rawId);
                entry.put("group", subModule);
                if (!entry.containsKey("label")) entry.put("label", rawId);
                result.add(entry);
            }
        } catch (Exception ignored) {}
    }

    private static Map<String, Object> wrapValues(String entityId, Map<String, Object> detail) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entityId", entityId);
        result.put("entityLabel", detail.getOrDefault("id", entityId).toString());
        result.put("values", detail);
        return result;
    }

    private static Map<String, Object> fieldGroup(String id, String label, int order, List<Map<String, Object>> fields) {
        Map<String, Object> group = new LinkedHashMap<>();
        group.put("id", id); group.put("label", label); group.put("order", order); group.put("fields", fields);
        return group;
    }

    private static Map<String, Object> field(String id, String label, String type) {
        return Map.of("id", id, "label", label, "type", type);
    }

    private static Map<String, Object> field(String id, String label, String type, boolean readOnly) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("id", id); f.put("label", label); f.put("type", type);
        if (readOnly) f.put("readOnly", true);
        return f;
    }

    private static Map<String, Object> enumField(String id, String label, String[] values) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("id", id); f.put("label", label); f.put("type", "enum");
        f.put("enumValues", List.of(values));
        return f;
    }

    private static Map<String, Object> requiredField(String id, String label, String type) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("id", id); f.put("label", label); f.put("type", type);
        f.put("required", true);
        return f;
    }

    private static Map<String, Object> intField(String id, String label, int min, int max) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("id", id); f.put("label", label); f.put("type", "int");
        f.put("min", min); f.put("max", max);
        return f;
    }
}
