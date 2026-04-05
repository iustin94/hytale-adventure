package dev.hytalemodding.hyadventure.services;

import java.util.*;

public class StashService {

    public Map<String, Object> getStashInfo() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "info");
        m.put("description", "Stash system — populates block containers with item drops");
        m.put("clearContainerDropList", false);
        m.put("note", "Requires block-level state access — use in-game /stash interaction");
        return m;
    }
}
