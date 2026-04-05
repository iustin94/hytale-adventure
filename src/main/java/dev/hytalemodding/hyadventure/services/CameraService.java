package dev.hytalemodding.hyadventure.services;

import com.hypixel.hytale.builtin.adventure.camera.asset.camerashake.CameraShake;

import java.util.*;

public class CameraService {

    public List<Map<String, Object>> listCameraShakes() {
        try {
            var map = CameraShake.getAssetMap();
            if (map == null) return List.of();
            List<Map<String, Object>> r = new ArrayList<>();
            for (var e : map.getAssetMap().entrySet()) {
                CameraShake s = (CameraShake) e.getValue();
                r.add(Map.of("id", s.getId(), "label", s.getId()));
            }
            return r;
        } catch (Exception e) { return List.of(); }
    }

    public Map<String, Object> getCameraShakeDetail(String id) {
        try {
            var map = CameraShake.getAssetMap();
            if (map == null) return null;
            Object v = map.getAssetMap().get(id);
            if (!(v instanceof CameraShake s)) return null;
            // CameraShake only exposes getId() — config fields are not public
            return Map.of("id", s.getId());
        } catch (Exception e) { return null; }
    }
}
