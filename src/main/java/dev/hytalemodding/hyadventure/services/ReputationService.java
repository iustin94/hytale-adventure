package dev.hytalemodding.hyadventure.services;

import com.hypixel.hytale.builtin.adventure.reputation.ReputationPlugin;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationGroup;
import com.hypixel.hytale.builtin.adventure.reputation.assets.ReputationRank;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.*;

public class ReputationService {

    public List<Map<String, Object>> listGroups() {
        try {
            var map = ReputationGroup.getAssetMap();
            if (map == null) return List.of();
            List<Map<String, Object>> r = new ArrayList<>();
            for (var e : map.getAssetMap().entrySet()) {
                ReputationGroup g = (ReputationGroup) e.getValue();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", g.getId());
                m.put("label", g.getId());
                m.put("npcGroups", g.getNpcGroups() != null ? Arrays.asList(g.getNpcGroups()) : List.of());
                m.put("initialReputationValue", g.getInitialReputationValue());
                r.add(m);
            }
            return r;
        } catch (Exception e) { return List.of(); }
    }

    public Map<String, Object> getGroupDetail(String id) {
        try {
            var map = ReputationGroup.getAssetMap();
            if (map == null) return null;
            Object v = map.getAssetMap().get(id);
            if (!(v instanceof ReputationGroup g)) return null;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", g.getId());
            m.put("npcGroups", g.getNpcGroups() != null ? Arrays.asList(g.getNpcGroups()) : List.of());
            m.put("initialReputationValue", g.getInitialReputationValue());
            return m;
        } catch (Exception e) { return null; }
    }

    public List<Map<String, Object>> listRanks() {
        try {
            var map = ReputationRank.getAssetMap();
            if (map == null) return List.of();
            List<Map<String, Object>> r = new ArrayList<>();
            for (var e : map.getAssetMap().entrySet()) {
                ReputationRank rk = (ReputationRank) e.getValue();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rk.getId());
                m.put("label", "Rank: " + rk.getId() + " (" + (rk.getAttitude() != null ? rk.getAttitude().toString() : "?") + ")");
                m.put("minValue", rk.getMinValue());
                m.put("maxValue", rk.getMaxValue());
                m.put("attitude", rk.getAttitude() != null ? rk.getAttitude().toString() : "");
                r.add(m);
            }
            return r;
        } catch (Exception e) { return List.of(); }
    }

    public Map<String, Object> getRankDetail(String id) {
        try {
            var map = ReputationRank.getAssetMap();
            if (map == null) return null;
            Object v = map.getAssetMap().get(id);
            if (!(v instanceof ReputationRank rk)) return null;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", rk.getId());
            m.put("minValue", rk.getMinValue());
            m.put("maxValue", rk.getMaxValue());
            m.put("attitude", rk.getAttitude() != null ? rk.getAttitude().toString() : "");
            return m;
        } catch (Exception e) { return null; }
    }

    public Map<String, Object> changeReputation(String worldId, String groupId, int delta) {
        if (groupId == null) return Map.of("success", false, "error", "groupId required");
        try {
            World world = Universe.get().getWorld(worldId);
            if (world == null) return Map.of("success", false, "error", "World not found");
            int newVal = ReputationPlugin.get().changeReputation(world, groupId, delta);
            return Map.of("success", true, "newValue", newVal);
        } catch (Exception e) { return Map.of("success", false, "error", e.getMessage()); }
    }
}
