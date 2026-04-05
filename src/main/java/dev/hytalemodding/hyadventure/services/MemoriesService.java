package dev.hytalemodding.hyadventure.services;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.builtin.adventure.memories.memories.Memory;

import java.util.*;

public class MemoriesService {

    public Map<String, Object> listAllMemories() {
        try {
            Map<String, Set<Memory>> all = MemoriesPlugin.get().getAllMemories();
            Map<String, Object> result = new LinkedHashMap<>();
            for (var e : all.entrySet()) {
                List<String> ids = new ArrayList<>();
                for (Memory m : e.getValue()) ids.add(m.getId());
                result.put(e.getKey(), ids);
            }
            return result;
        } catch (Exception e) { return Map.of(); }
    }

    public List<Map<String, Object>> listAllMemoriesFlat() {
        try {
            Map<String, Set<Memory>> all = MemoriesPlugin.get().getAllMemories();
            Set<Memory> recorded = MemoriesPlugin.get().getRecordedMemories();
            Set<String> recordedIds = new HashSet<>();
            if (recorded != null) { for (Memory m : recorded) recordedIds.add(m.getId()); }

            List<Map<String, Object>> result = new ArrayList<>();
            for (var e : all.entrySet()) {
                String provider = e.getKey();
                for (Memory m : e.getValue()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", m.getId());
                    item.put("label", m.getId() + (recordedIds.contains(m.getId()) ? " [recorded]" : ""));
                    item.put("provider", provider);
                    item.put("isRecorded", recordedIds.contains(m.getId()));
                    result.add(item);
                }
            }
            return result;
        } catch (Exception e) { return List.of(); }
    }

    public Map<String, Object> getMemoryDetail(String memoryId) {
        try {
            Map<String, Set<Memory>> all = MemoriesPlugin.get().getAllMemories();
            Set<Memory> recorded = MemoriesPlugin.get().getRecordedMemories();
            Set<String> recordedIds = new HashSet<>();
            if (recorded != null) { for (Memory m : recorded) recordedIds.add(m.getId()); }

            for (var e : all.entrySet()) {
                for (Memory m : e.getValue()) {
                    if (m.getId().equals(memoryId)) {
                        Map<String, Object> result = new LinkedHashMap<>();
                        result.put("id", m.getId());
                        result.put("provider", e.getKey());
                        result.put("isRecorded", recordedIds.contains(m.getId()));
                        return result;
                    }
                }
            }
            return null;
        } catch (Exception e) { return null; }
    }

    public List<String> listRecordedMemories() {
        try {
            Set<Memory> recorded = MemoriesPlugin.get().getRecordedMemories();
            List<String> ids = new ArrayList<>();
            for (Memory m : recorded) ids.add(m.getId());
            return ids;
        } catch (Exception e) { return List.of(); }
    }

    public int getMemoriesLevel() {
        return 0; // requires GameplayConfig param — not easily accessible
    }

    public Map<String, Object> recordAll() {
        try {
            MemoriesPlugin.get().recordAllMemories();
            return Map.of("success", true);
        } catch (Exception e) { return Map.of("success", false, "error", e.getMessage()); }
    }

    public Map<String, Object> clearAll() {
        try {
            MemoriesPlugin.get().clearRecordedMemories();
            return Map.of("success", true);
        } catch (Exception e) { return Map.of("success", false, "error", e.getMessage()); }
    }

    public Map<String, Object> setCount(int count) {
        try {
            int result = MemoriesPlugin.get().setRecordedMemoriesCount(count);
            return Map.of("success", true, "count", result);
        } catch (Exception e) { return Map.of("success", false, "error", e.getMessage()); }
    }
}
