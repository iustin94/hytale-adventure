package dev.hytalemodding.hyadventure.services;

import com.hypixel.hytale.builtin.adventure.farming.config.FarmingCoopAsset;

import java.util.*;

public class FarmingService {

    public List<Map<String, Object>> listCoops() {
        try {
            var map = FarmingCoopAsset.getAssetMap();
            if (map == null) return List.of();
            List<Map<String, Object>> r = new ArrayList<>();
            for (var e : map.getAssetMap().entrySet()) {
                FarmingCoopAsset a = (FarmingCoopAsset) e.getValue();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", a.getId());
                m.put("label", a.getId() + " (max: " + a.getMaxResidents() + ")");
                m.put("maxResidents", a.getMaxResidents());
                m.put("wildCaptureRadius", a.getWildCaptureRadius());
                m.put("captureWildNPCsInRange", a.getCaptureWildNPCsInRange());
                r.add(m);
            }
            return r;
        } catch (Exception e) { return List.of(); }
    }

    public Map<String, Object> getCoopDetail(String id) {
        try {
            var map = FarmingCoopAsset.getAssetMap();
            if (map == null) return null;
            Object v = map.getAssetMap().get(id);
            if (!(v instanceof FarmingCoopAsset a)) return null;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("maxResidents", a.getMaxResidents());
            m.put("wildCaptureRadius", a.getWildCaptureRadius());
            m.put("captureWildNPCsInRange", a.getCaptureWildNPCsInRange());
            m.put("produceDrops", a.getProduceDrops() != null ? a.getProduceDrops().toString() : "");
            return m;
        } catch (Exception e) { return null; }
    }
}
