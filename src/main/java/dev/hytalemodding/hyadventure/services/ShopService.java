package dev.hytalemodding.hyadventure.services;

import com.hypixel.hytale.builtin.adventure.shop.ShopAsset;
import com.hypixel.hytale.builtin.adventure.shop.barter.BarterShopAsset;
import com.hypixel.hytale.builtin.adventure.shop.barter.BarterTrade;

import java.util.*;

public class ShopService {

    public List<Map<String, Object>> listShops() {
        try {
            var map = ShopAsset.getAssetMap();
            if (map == null) return List.of();
            List<Map<String, Object>> r = new ArrayList<>();
            for (var e : map.getAssetMap().entrySet()) {
                ShopAsset a = (ShopAsset) e.getValue();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", a.getId());
                m.put("label", a.getId());
                m.put("elementCount", a.getElements() != null ? a.getElements().length : 0);
                r.add(m);
            }
            return r;
        } catch (Exception e) { return List.of(); }
    }

    public Map<String, Object> getShopDetail(String id) {
        try {
            var map = ShopAsset.getAssetMap();
            if (map == null) return null;
            Object v = map.getAssetMap().get(id);
            if (!(v instanceof ShopAsset a)) return null;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            int elemCount = a.getElements() != null ? a.getElements().length : 0;
            m.put("elementCount", elemCount);

            List<Map<String, Object>> elements = new ArrayList<>();
            if (a.getElements() != null) {
                for (var elem : a.getElements()) {
                    elements.add(Map.of("element", elem.toString()));
                }
            }
            m.put("elements", elements);
            return m;
        } catch (Exception e) { return null; }
    }

    public List<Map<String, Object>> listBarterShops() {
        try {
            var map = BarterShopAsset.getAssetMap();
            if (map == null) return List.of();
            List<Map<String, Object>> r = new ArrayList<>();
            for (var e : map.getAssetMap().entrySet()) {
                BarterShopAsset a = (BarterShopAsset) e.getValue();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", a.getId());
                m.put("label", "Barter: " + (a.getDisplayNameKey() != null ? a.getDisplayNameKey() : a.getId()));
                m.put("displayNameKey", a.getDisplayNameKey());
                m.put("restockHour", a.getRestockHour());
                m.put("tradeCount", a.getTrades() != null ? a.getTrades().length : 0);
                r.add(m);
            }
            return r;
        } catch (Exception e) { return List.of(); }
    }

    public Map<String, Object> getBarterShopDetail(String id) {
        try {
            var map = BarterShopAsset.getAssetMap();
            if (map == null) return null;
            Object v = map.getAssetMap().get(id);
            if (!(v instanceof BarterShopAsset a)) return null;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("displayNameKey", a.getDisplayNameKey());
            m.put("restockHour", a.getRestockHour());

            List<Map<String, Object>> trades = new ArrayList<>();
            if (a.getTrades() != null) {
                for (BarterTrade t : a.getTrades()) {
                    Map<String, Object> tm = new LinkedHashMap<>();
                    tm.put("maxStock", t.getMaxStock());
                    tm.put("output", t.getOutput() != null ? t.getOutput().toString() : "");
                    List<String> inputs = new ArrayList<>();
                    if (t.getInput() != null) {
                        for (var input : t.getInput()) inputs.add(input != null ? input.toString() : "");
                    }
                    tm.put("inputs", inputs);
                    tm.put("inputCount", inputs.size());
                    trades.add(tm);
                }
            }
            m.put("trades", trades);
            m.put("tradeCount", trades.size());
            return m;
        } catch (Exception e) { return null; }
    }
}
