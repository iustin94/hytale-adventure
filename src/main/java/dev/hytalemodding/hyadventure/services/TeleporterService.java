package dev.hytalemodding.hyadventure.services;

import com.hypixel.hytale.builtin.adventure.teleporter.TeleporterPlugin;
import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TeleporterService {

    public List<Map<String, Object>> listTeleporters(String worldId) {
        try {
            World world = Universe.get().getWorld(worldId);
            if (world == null) return List.of();

            var compType = TeleporterPlugin.get().getTeleporterComponentType();
            CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();

            world.execute(() -> {
                try {
                    List<Map<String, Object>> result = new ArrayList<>();
                    Store<ChunkStore> store = world.getChunkStore().getStore();
                    int index = 0;

                    store.forEachChunk((chunk, cmd) -> {
                        for (int i = 0; i < chunk.size(); i++) {
                            Teleporter tp = chunk.getComponent(i, compType);
                            if (tp == null || !tp.isValid()) continue;

                            Map<String, Object> m = new LinkedHashMap<>();
                            String warp = tp.getWarp() != null ? tp.getWarp() : "";
                            m.put("id", warp.isEmpty() ? "teleporter_" + result.size() : warp);
                            m.put("label", warp.isEmpty() ? "Unnamed Teleporter" : warp);
                            m.put("warp", warp);
                            m.put("ownedWarp", tp.getOwnedWarp() != null ? tp.getOwnedWarp() : "");
                            m.put("customName", tp.isCustomName());
                            m.put("worldUuid", tp.getWorldUuid() != null ? tp.getWorldUuid().toString() : "");
                            if (tp.getTransform() != null && tp.getTransform().getPosition() != null) {
                                var pos = tp.getTransform().getPosition();
                                m.put("x", pos.getX());
                                m.put("y", pos.getY());
                                m.put("z", pos.getZ());
                            }
                            result.add(m);
                        }
                    });

                    future.complete(result);
                } catch (Exception e) {
                    future.complete(List.of());
                }
            });

            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) { return List.of(); }
    }

    public Map<String, Object> getTeleporterDetail(String warpName) {
        try {
            List<Map<String, Object>> all = listTeleporters("default");
            for (Map<String, Object> tp : all) {
                if (warpName.equals(tp.get("id")) || warpName.equals(tp.get("warp"))) {
                    return tp;
                }
            }
            return null;
        } catch (Exception e) { return null; }
    }
}
