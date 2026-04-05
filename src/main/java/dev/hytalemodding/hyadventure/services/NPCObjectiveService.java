package dev.hytalemodding.hyadventure.services;

import com.hypixel.hytale.builtin.adventure.npcobjectives.NPCObjectivesPlugin;
import com.hypixel.hytale.builtin.adventure.npcobjectives.assets.BountyObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.npcobjectives.assets.KillObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.npcobjectives.assets.KillSpawnMarkerObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.npcobjectives.resources.KillTrackerResource;
import com.hypixel.hytale.builtin.adventure.npcobjectives.transaction.KillTaskTransaction;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.ObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.TaskSet;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.hypixel.hytale.server.npc.NPCPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class NPCObjectiveService {

    // ── Available NPC roles ──────────────────────────────────────────────────

    public List<Map<String, Object>> listAvailableNpcRoles() {
        try {
            List<String> roleNames = NPCPlugin.get().getRoleTemplateNames(false);
            List<Map<String, Object>> result = new ArrayList<>();
            for (String name : roleNames) {
                result.add(Map.of("id", name, "label", name));
            }
            result.sort(Comparator.comparing(m -> m.get("label").toString()));
            return result;
        } catch (Exception e) { return List.of(); }
    }

    // ── Active NPC objectives ─────���──────────────────────────────────────────

    public List<Map<String, Object>> listActiveNPCObjectives(String worldId) {
        try {
            World world = Universe.get().getWorld(worldId);
            if (world == null) return List.of();

            CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
            world.execute(() -> {
                try {
                    Store<EntityStore> store = world.getEntityStore().getStore();
                    var resourceType = NPCObjectivesPlugin.get().getKillTrackerResourceType();
                    KillTrackerResource tracker = store.getResource(resourceType);

                    List<Map<String, Object>> result = new ArrayList<>();
                    if (tracker != null) {
                        for (KillTaskTransaction task : tracker.getKillTasks()) {
                            Map<String, Object> m = new LinkedHashMap<>();
                            String objId = task.getObjective() != null ? task.getObjective().getObjectiveId() : "";
                            String objUuid = task.getObjective() != null ? task.getObjective().getObjectiveUUID().toString() : "";
                            m.put("id", objUuid);
                            m.put("label", objId);
                            m.put("objectiveId", objId);
                            m.put("objectiveUUID", objUuid);
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

    // ── Kill task assets ─────────────────────────���───────────────────────────

    public List<Map<String, Object>> listKillTaskAssets() {
        try {
            var objMap = ObjectiveAsset.getAssetMap();
            if (objMap == null) return List.of();

            List<Map<String, Object>> result = new ArrayList<>();
            for (var e : objMap.getAssetMap().entrySet()) {
                ObjectiveAsset a = (ObjectiveAsset) e.getValue();
                if (a.getTaskSets() == null) continue;
                for (TaskSet ts : a.getTaskSets()) {
                    if (ts.getTasks() == null) continue;
                    for (ObjectiveTaskAsset task : ts.getTasks()) {
                        if (task instanceof KillObjectiveTaskAsset kill) {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("objectiveId", a.getId());
                            m.put("npcGroupId", kill.getNpcGroupId());
                            m.put("count", kill.getCount());
                            result.add(m);
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) { return List.of(); }
    }

    public List<Map<String, Object>> listKillSpawnMarkerTaskAssets() {
        try {
            var objMap = ObjectiveAsset.getAssetMap();
            if (objMap == null) return List.of();

            List<Map<String, Object>> result = new ArrayList<>();
            for (var e : objMap.getAssetMap().entrySet()) {
                ObjectiveAsset a = (ObjectiveAsset) e.getValue();
                if (a.getTaskSets() == null) continue;
                for (TaskSet ts : a.getTaskSets()) {
                    if (ts.getTasks() == null) continue;
                    for (ObjectiveTaskAsset task : ts.getTasks()) {
                        if (task instanceof KillSpawnMarkerObjectiveTaskAsset ksm) {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("objectiveId", a.getId());
                            m.put("npcGroupId", ksm.getNpcGroupId());
                            m.put("count", ksm.getCount());
                            m.put("spawnMarkerIds", ksm.getSpawnMarkerIds() != null ? Arrays.asList(ksm.getSpawnMarkerIds()) : List.of());
                            m.put("radius", ksm.getRadius());
                            result.add(m);
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) { return List.of(); }
    }

    public List<Map<String, Object>> listBountyTaskAssets() {
        try {
            var objMap = ObjectiveAsset.getAssetMap();
            if (objMap == null) return List.of();

            List<Map<String, Object>> result = new ArrayList<>();
            for (var e : objMap.getAssetMap().entrySet()) {
                ObjectiveAsset a = (ObjectiveAsset) e.getValue();
                if (a.getTaskSets() == null) continue;
                for (TaskSet ts : a.getTaskSets()) {
                    if (ts.getTasks() == null) continue;
                    for (ObjectiveTaskAsset task : ts.getTasks()) {
                        if (task instanceof BountyObjectiveTaskAsset bounty) {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("objectiveId", a.getId());
                            m.put("npcId", bounty.getNpcId());
                            result.add(m);
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) { return List.of(); }
    }

    // ── Mutations ────────────────────────────────────────────────────────────

    public Map<String, Object> startNPCObjective(String entityUuidStr, String objectiveId, String worldId) {
        if (entityUuidStr == null || objectiveId == null)
            return Map.of("success", false, "error", "entityUuid and objectiveId required");
        try {
            World world = Universe.get().getWorld(worldId);
            if (world == null) return Map.of("success", false, "error", "World not found");

            UUID entityUuid = UUID.fromString(entityUuidStr);
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

            world.execute(() -> {
                try {
                    Store<EntityStore> store = world.getEntityStore().getStore();
                    Ref<EntityStore> entityRef = world.getEntityRef(entityUuid);
                    if (entityRef == null) {
                        future.complete(Map.of("success", false, "error", "Entity not found"));
                        return;
                    }
                    NPCObjectivesPlugin.startObjective(entityRef, objectiveId, store);
                    future.complete(Map.of("success", true));
                } catch (Exception e) {
                    future.complete(Map.of("success", false, "error", e.getMessage()));
                }
            });

            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) { return Map.of("success", false, "error", e.getMessage()); }
    }

    public boolean hasTask(String playerUuidStr, String entityUuidStr, String taskId) {
        try {
            return NPCObjectivesPlugin.hasTask(
                    UUID.fromString(playerUuidStr), UUID.fromString(entityUuidStr), taskId);
        } catch (Exception e) { return false; }
    }
}
