package dev.hytalemodding.hyadventure.services;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.components.ObjectiveHistoryComponent;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveLineAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveLocationMarkerAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.ClearObjectiveItemsCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.GiveItemsCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.ObjectiveCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.*;
import com.hypixel.hytale.builtin.adventure.objectives.markers.reachlocation.ReachLocationMarker;
import com.hypixel.hytale.builtin.adventure.objectivereputation.assets.ReputationCompletionAsset;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ObjectiveService {

    // ── Asset queries ────────────────────────────────────────────────────────

    public List<Map<String, Object>> listObjectiveAssets() {
        try {
            var map = ObjectiveAsset.getAssetMap();
            if (map == null) return List.of();
            List<Map<String, Object>> r = new ArrayList<>();
            for (var e : map.getAssetMap().entrySet()) {
                ObjectiveAsset a = (ObjectiveAsset) e.getValue();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", a.getId());
                m.put("label", a.getId());
                m.put("category", a.getCategory());
                m.put("titleKey", a.getTitleKey());
                m.put("descriptionKey", a.getDescriptionKey());
                r.add(m);
            }
            return r;
        } catch (Exception e) { return List.of(); }
    }

    public Map<String, Object> getObjectiveAssetDetail(String id) {
        try {
            var map = ObjectiveAsset.getAssetMap();
            if (map == null) return null;
            Object v = map.getAssetMap().get(id);
            if (!(v instanceof ObjectiveAsset a)) return null;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("category", a.getCategory());
            m.put("titleKey", a.getTitleKey());
            m.put("descriptionKey", a.getDescriptionKey());
            m.put("removeOnItemDrop", a.isRemoveOnItemDrop());

            // Task sets with nested task details
            List<Map<String, Object>> taskSets = new ArrayList<>();
            if (a.getTaskSets() != null) {
                for (int i = 0; i < a.getTaskSets().length; i++) {
                    TaskSet ts = a.getTaskSets()[i];
                    Map<String, Object> tsMap = new LinkedHashMap<>();
                    tsMap.put("descriptionId", ts.getDescriptionId());
                    tsMap.put("tasks", serializeTasks(ts.getTasks()));
                    taskSets.add(tsMap);
                }
            }
            m.put("taskSets", taskSets);
            m.put("taskSetCount", taskSets.size());

            // Completions with type details
            List<Map<String, Object>> completions = new ArrayList<>();
            if (a.getCompletionHandlers() != null) {
                for (ObjectiveCompletionAsset comp : a.getCompletionHandlers()) {
                    completions.add(serializeCompletion(comp));
                }
            }
            m.put("completions", completions);
            m.put("completionCount", completions.size());

            return m;
        } catch (Exception e) { return null; }
    }

    public List<Map<String, Object>> listObjectiveLines() {
        try {
            var map = ObjectiveLineAsset.getAssetMap();
            if (map == null) return List.of();
            List<Map<String, Object>> r = new ArrayList<>();
            for (var e : map.getAssetMap().entrySet()) {
                ObjectiveLineAsset a = (ObjectiveLineAsset) e.getValue();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", a.getId());
                m.put("category", a.getCategory());
                m.put("objectiveIds", a.getObjectiveIds() != null ? Arrays.asList(a.getObjectiveIds()) : List.of());
                r.add(m);
            }
            return r;
        } catch (Exception e) { return List.of(); }
    }

    public Map<String, Object> getObjectiveLineDetail(String id) {
        try {
            var map = ObjectiveLineAsset.getAssetMap();
            if (map == null) return null;
            Object v = map.getAssetMap().get(id);
            if (!(v instanceof ObjectiveLineAsset a)) return null;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("category", a.getCategory());
            m.put("titleKey", a.getObjectiveTitleKey());
            m.put("descriptionKey", a.getObjectiveDescriptionKey());
            m.put("objectiveIds", a.getObjectiveIds() != null ? Arrays.asList(a.getObjectiveIds()) : List.of());
            m.put("nextObjectiveLineIds", a.getNextObjectiveLineIds() != null ? Arrays.asList(a.getNextObjectiveLineIds()) : List.of());
            return m;
        } catch (Exception e) { return null; }
    }

    public List<Map<String, Object>> listLocationMarkers() {
        try {
            var map = ObjectiveLocationMarkerAsset.getAssetMap();
            if (map == null) return List.of();
            List<Map<String, Object>> r = new ArrayList<>();
            for (var e : map.getAssetMap().entrySet()) {
                ObjectiveLocationMarkerAsset a = (ObjectiveLocationMarkerAsset) e.getValue();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", a.getId());
                m.put("label", a.getId());
                m.put("environmentIds", a.getEnvironmentIds() != null ? Arrays.asList(a.getEnvironmentIds()) : List.of());
                r.add(m);
            }
            return r;
        } catch (Exception e) { return List.of(); }
    }

    @javax.annotation.Nullable
    public Map<String, Object> getLocationMarkerDetail(String id) {
        try {
            var map = ObjectiveLocationMarkerAsset.getAssetMap();
            if (map == null) return null;
            for (var e : map.getAssetMap().entrySet()) {
                ObjectiveLocationMarkerAsset a = (ObjectiveLocationMarkerAsset) e.getValue();
                if (a.getId().equals(id)) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("loc_id", a.getId());
                    m.put("loc_environmentIds", a.getEnvironmentIds() != null ? Arrays.asList(a.getEnvironmentIds()) : List.of());
                    return m;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Scans world entities for ReachLocationMarker components and returns their positions.
     * Unlike listLocationMarkers() which reads from the asset registry, this returns
     * actual placed marker entities with world coordinates.
     */
    public List<Map<String, Object>> listLocationMarkerEntities(String worldId) {
        try {
            World world = Universe.get().getWorld(worldId);
            if (world == null) return List.of();

            CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
            world.execute(() -> {
                try {
                    Store<EntityStore> store = world.getEntityStore().getStore();
                    List<Map<String, Object>> result = new ArrayList<>();

                    store.forEachChunk(ReachLocationMarker.getComponentType(), (chunk, commandBuffer) -> {
                        for (int i = 0; i < chunk.size(); i++) {
                            ReachLocationMarker marker = chunk.getComponent(i, ReachLocationMarker.getComponentType());
                            TransformComponent transform = chunk.getComponent(i, TransformComponent.getComponentType());
                            if (marker == null || transform == null) continue;

                            Vector3d pos = transform.getPosition();
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("id", marker.getMarkerId());
                            m.put("label", marker.getMarkerId());
                            m.put("x", pos.getX());
                            m.put("y", pos.getY());
                            m.put("z", pos.getZ());
                            result.add(m);
                        }
                    });

                    future.complete(result);
                } catch (Exception e) {
                    future.complete(List.of());
                }
            });

            return future.get(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) { return List.of(); }
    }

    // ── Active objective queries ─────────────────────────────────────────────

    public List<Map<String, Object>> listActiveObjectives() {
        try {
            var ds = ObjectivePlugin.get().getObjectiveDataStore();
            if (ds == null) return List.of();
            List<Map<String, Object>> r = new ArrayList<>();
            for (Objective o : ds.getObjectiveCollection()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", o.getObjectiveUUID().toString());
                m.put("label", (o.isCompleted() ? "[DONE] " : "[ACTIVE] ") + o.getObjectiveId());
                m.put("objectiveId", o.getObjectiveId());
                m.put("completed", o.isCompleted());
                m.put("playerCount", o.getPlayerUUIDs().size());
                r.add(m);
            }
            return r;
        } catch (Exception e) { return List.of(); }
    }

    public Map<String, Object> getActiveObjectiveDetail(String uuid) {
        try {
            var ds = ObjectivePlugin.get().getObjectiveDataStore();
            if (ds == null) return null;
            var o = ds.getObjective(UUID.fromString(uuid));
            if (o == null) return null;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o.getObjectiveUUID().toString());
            m.put("objectiveId", o.getObjectiveId());
            m.put("completed", o.isCompleted());
            m.put("description", o.getCurrentDescription());
            m.put("taskSetIndex", o.getCurrentTaskSetIndex());
            m.put("playerCount", o.getPlayerUUIDs().size());
            m.put("players", o.getPlayerUUIDs().stream().map(UUID::toString).toList());
            m.put("activePlayers", o.getActivePlayerUUIDs().stream().map(UUID::toString).toList());
            m.put("worldUUID", o.getWorldUUID() != null ? o.getWorldUUID().toString() : "");
            return m;
        } catch (Exception e) { return null; }
    }

    public List<Map<String, Object>> listObjectiveHistory(String playerUuidStr) {
        try {
            UUID playerUuid = UUID.fromString(playerUuidStr);
            World world = Universe.get().getWorld("default");
            if (world == null) return List.of();

            CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
            world.execute(() -> {
                try {
                    var player = dev.hytalemodding.api.EntityLookup.findPlayerByUuid(playerUuidStr);
                    if (player == null) { future.complete(List.of()); return; }

                    Store<EntityStore> store = world.getEntityStore().getStore();
                    var compType = ObjectivePlugin.get().getObjectiveHistoryComponentType();
                    var playerRef = world.getEntityRef(playerUuid);
                    if (playerRef == null) { future.complete(List.of()); return; }

                    ObjectiveHistoryComponent hist = store.getComponent(playerRef, compType);
                    List<Map<String, Object>> result = new ArrayList<>();
                    if (hist != null) {
                        for (var e : hist.getObjectiveHistoryMap().entrySet()) {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("objectiveId", e.getKey());
                            m.put("data", e.getValue().toString());
                            result.add(m);
                        }
                    }
                    future.complete(result);
                } catch (Exception e) {
                    future.complete(List.of());
                }
            });
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) { return List.of(); }
    }

    // ── Mutations ────────────────────────────────────────────────────────────

    public Map<String, Object> startObjective(String objectiveId, String playerCsv, String worldId) {
        if (objectiveId == null || objectiveId.isEmpty()) return Map.of("success", false, "error", "objectiveId required");
        try {
            World world = Universe.get().getWorld(worldId);
            if (world == null) return Map.of("success", false, "error", "World not found");
            Set<UUID> players = new HashSet<>();
            for (String s : playerCsv.split(",")) { s = s.trim(); if (!s.isEmpty()) players.add(UUID.fromString(s)); }
            if (players.isEmpty()) return Map.of("success", false, "error", "No players");
            CompletableFuture<Map<String, Object>> f = new CompletableFuture<>();
            world.execute(() -> { try {
                Store<EntityStore> store = world.getEntityStore().getStore();
                UUID wUuid = UUID.nameUUIDFromBytes(worldId.getBytes());
                Objective o = ObjectivePlugin.get().startObjective(objectiveId, players, wUuid, wUuid, store);
                f.complete(o != null ? Map.of("success", true, "objectiveUUID", o.getObjectiveUUID().toString())
                        : Map.of("success", false, "error", "Start failed"));
            } catch (Exception e) { f.complete(Map.of("success", false, "error", e.getMessage())); } });
            return f.get(5, TimeUnit.SECONDS);
        } catch (Exception e) { return Map.of("success", false, "error", e.getMessage()); }
    }

    public Map<String, Object> cancelObjective(String uuidStr) {
        if (uuidStr == null) return Map.of("success", false, "error", "uuid required");
        try {
            UUID uuid = UUID.fromString(uuidStr);
            World world = Universe.get().getWorld("default");
            if (world == null) return Map.of("success", false, "error", "World not found");
            CompletableFuture<Map<String, Object>> f = new CompletableFuture<>();
            world.execute(() -> { try {
                ObjectivePlugin.get().cancelObjective(uuid, world.getEntityStore().getStore());
                f.complete(Map.of("success", true));
            } catch (Exception e) { f.complete(Map.of("success", false, "error", e.getMessage())); } });
            return f.get(5, TimeUnit.SECONDS);
        } catch (Exception e) { return Map.of("success", false, "error", e.getMessage()); }
    }

    public Map<String, Object> completeObjective(String uuidStr) {
        if (uuidStr == null) return Map.of("success", false, "error", "uuid required");
        try {
            UUID uuid = UUID.fromString(uuidStr);
            World world = Universe.get().getWorld("default");
            if (world == null) return Map.of("success", false, "error", "World not found");

            CompletableFuture<Map<String, Object>> f = new CompletableFuture<>();
            world.execute(() -> { try {
                var ds = ObjectivePlugin.get().getObjectiveDataStore();
                Objective o = ds != null ? ds.getObjective(uuid) : null;
                if (o == null) { f.complete(Map.of("success", false, "error", "Objective not found")); return; }
                o.complete(world.getEntityStore().getStore());
                f.complete(Map.of("success", true));
            } catch (Exception e) { f.complete(Map.of("success", false, "error", e.getMessage())); } });
            return f.get(5, TimeUnit.SECONDS);
        } catch (Exception e) { return Map.of("success", false, "error", e.getMessage()); }
    }

    // ── Serialization helpers ────────────────────────────────────────────────

    private List<Map<String, Object>> serializeTasks(ObjectiveTaskAsset[] tasks) {
        if (tasks == null) return List.of();
        List<Map<String, Object>> result = new ArrayList<>();
        for (ObjectiveTaskAsset task : tasks) {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("type", task.getClass().getSimpleName().replace("ObjectiveTaskAsset", "").replace("TaskAsset", ""));
            t.put("descriptionId", task.getDescriptionId());
            t.put("scope", task.getTaskScope().toString());

            if (task instanceof CountObjectiveTaskAsset countTask) {
                t.put("count", countTask.getCount());
            }
            if (task instanceof GatherObjectiveTaskAsset gather) {
                t.put("blockTagOrItemId", gather.getBlockTagOrItemIdField() != null ? gather.getBlockTagOrItemIdField().toString() : "");
            }
            if (task instanceof UseEntityObjectiveTaskAsset useEntity) {
                t.put("taskId", useEntity.getTaskId());
                t.put("animationIdToPlay", useEntity.getAnimationIdToPlay());
                if (useEntity.getDialogOptions() != null) {
                    t.put("dialogEntityNameKey", useEntity.getDialogOptions().getEntityNameKey());
                    t.put("dialogKey", useEntity.getDialogOptions().getDialogKey());
                }
            }
            if (task instanceof ReachLocationTaskAsset reach) {
                t.put("targetLocationId", reach.getTargetLocationId());
            }
            if (task instanceof TreasureMapObjectiveTaskAsset treasure) {
                if (treasure.getChestConfigs() != null) {
                    List<Map<String, Object>> chests = new ArrayList<>();
                    for (var cc : treasure.getChestConfigs()) {
                        chests.add(Map.of(
                                "minRadius", cc.getMinRadius(),
                                "maxRadius", cc.getMaxRadius(),
                                "droplistId", cc.getDroplistId() != null ? cc.getDroplistId() : ""
                        ));
                    }
                    t.put("chestConfigs", chests);
                }
            }
            if (task.getMapMarkers() != null) {
                List<List<Integer>> markers = new ArrayList<>();
                for (var pos : task.getMapMarkers()) markers.add(List.of(pos.getX(), pos.getY(), pos.getZ()));
                t.put("mapMarkers", markers);
            }
            if (task.getTaskConditions() != null && task.getTaskConditions().length > 0) {
                t.put("conditionCount", task.getTaskConditions().length);
            }
            result.add(t);
        }
        return result;
    }

    private Map<String, Object> serializeCompletion(ObjectiveCompletionAsset comp) {
        Map<String, Object> c = new LinkedHashMap<>();
        if (comp instanceof GiveItemsCompletionAsset give) {
            c.put("type", "GiveItems");
            c.put("dropListId", give.getDropListId());
        } else if (comp instanceof ClearObjectiveItemsCompletionAsset) {
            c.put("type", "ClearObjectiveItems");
        } else if (comp instanceof ReputationCompletionAsset rep) {
            c.put("type", "Reputation");
            c.put("groupId", rep.getReputationGroupId());
            c.put("amount", rep.getAmount());
        } else {
            c.put("type", comp.getClass().getSimpleName());
        }
        return c;
    }
}
