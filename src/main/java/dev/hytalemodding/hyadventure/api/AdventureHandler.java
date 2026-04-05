package dev.hytalemodding.hyadventure.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.hytalemodding.api.HttpApiServer;
import dev.hytalemodding.api.JsonUtil;
import dev.hytalemodding.hyadventure.services.*;

import java.io.IOException;
import java.util.Map;

public class AdventureHandler implements HttpHandler {

    private final ObjectiveService objectives;
    private final ReputationService reputation;
    private final ShopService shops;
    private final FarmingService farming;
    private final MemoriesService memories;
    private final CameraService camera;
    private final TeleporterService teleporters;
    private final NPCObjectiveService npcObjectives;
    private final StashService stash;
    private final QuestAuthoringService authoring;
    private final QuestRegistrationService registration;

    public AdventureHandler(ObjectiveService objectives, ReputationService reputation,
                            ShopService shops, FarmingService farming, MemoriesService memories,
                            CameraService camera, TeleporterService teleporters,
                            NPCObjectiveService npcObjectives, StashService stash,
                            QuestAuthoringService authoring, QuestRegistrationService registration) {
        this.objectives = objectives;
        this.reputation = reputation;
        this.shops = shops;
        this.farming = farming;
        this.memories = memories;
        this.camera = camera;
        this.teleporters = teleporters;
        this.npcObjectives = npcObjectives;
        this.stash = stash;
        this.authoring = authoring;
        this.registration = registration;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String sub = path.length() > "/api/adventure".length()
                ? path.substring("/api/adventure".length()) : "";

        try {
            if (sub.startsWith("/objectives")) handleObjectives(exchange, sub.substring("/objectives".length()));
            else if (sub.startsWith("/reputation")) handleReputation(exchange, sub.substring("/reputation".length()));
            else if (sub.startsWith("/shops")) handleShops(exchange, sub.substring("/shops".length()));
            else if (sub.startsWith("/farming")) handleFarming(exchange, sub.substring("/farming".length()));
            else if (sub.startsWith("/memories")) handleMemories(exchange, sub.substring("/memories".length()));
            else if (sub.startsWith("/camera")) handleCamera(exchange, sub.substring("/camera".length()));
            else if (sub.startsWith("/teleporters")) handleTeleporters(exchange, sub.substring("/teleporters".length()));
            else if (sub.startsWith("/npcs/roles")) handleNpcRoles(exchange);
            else if (sub.startsWith("/npcobjectives")) handleNPCObjectives(exchange, sub.substring("/npcobjectives".length()));
            else if (sub.startsWith("/stash")) handleStash(exchange);
            else if (sub.startsWith("/authoring")) handleAuthoring(exchange, sub.substring("/authoring".length()));
            else HttpApiServer.sendJson(exchange, 404, JsonUtil.errorJson("Unknown: " + sub));
        } catch (Exception e) {
            HttpApiServer.sendJson(exchange, 500, JsonUtil.errorJson(e.getMessage()));
        }
    }

    private void handleObjectives(HttpExchange ex, String sub) throws IOException {
        if (sub.startsWith("/lines/") && sub.length() > 7) {
            if (!HttpApiServer.requireGet(ex)) return;
            var detail = objectives.getObjectiveLineDetail(sub.substring(7));
            HttpApiServer.sendJson(ex, detail != null ? 200 : 404,
                    detail != null ? JsonUtil.toJson(detail) : JsonUtil.errorJson("Not found"));
        } else if (sub.equals("/lines") || sub.equals("/lines/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(objectives.listObjectiveLines()));
        } else if (sub.equals("/markers") || sub.equals("/markers/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(objectives.listLocationMarkers()));
        } else if (sub.equals("/markers/world") || sub.startsWith("/markers/world?")) {
            if (!HttpApiServer.requireGet(ex)) return;
            var params = HttpApiServer.parseQuery(ex.getRequestURI().getQuery());
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(
                    objectives.listLocationMarkerEntities(params.getOrDefault("world", "default"))));
        } else if (sub.equals("/active") || sub.equals("/active/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(objectives.listActiveObjectives()));
        } else if (sub.startsWith("/active/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            var detail = objectives.getActiveObjectiveDetail(sub.substring(8));
            HttpApiServer.sendJson(ex, detail != null ? 200 : 404,
                    detail != null ? JsonUtil.toJson(detail) : JsonUtil.errorJson("Not found"));
        } else if (sub.startsWith("/history/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(objectives.listObjectiveHistory(sub.substring(9))));
        } else if (sub.equals("/start")) {
            if (!HttpApiServer.requirePost(ex)) return;
            var body = HttpApiServer.parseJsonBody(ex);
            var result = objectives.startObjective(body.get("objectiveId"),
                    body.getOrDefault("playerUuids", ""), body.getOrDefault("worldId", "default"));
            sendResult(ex, result);
        } else if (sub.equals("/cancel")) {
            if (!HttpApiServer.requirePost(ex)) return;
            var body = HttpApiServer.parseJsonBody(ex);
            var result = objectives.cancelObjective(body.get("objectiveUuid"));
            sendResult(ex, result);
        } else if (sub.equals("/complete")) {
            if (!HttpApiServer.requirePost(ex)) return;
            var body = HttpApiServer.parseJsonBody(ex);
            var result = objectives.completeObjective(body.get("objectiveUuid"));
            sendResult(ex, result);
        } else if (sub.startsWith("/") && sub.length() > 1) {
            if (!HttpApiServer.requireGet(ex)) return;
            var detail = objectives.getObjectiveAssetDetail(sub.substring(1));
            HttpApiServer.sendJson(ex, detail != null ? 200 : 404,
                    detail != null ? JsonUtil.toJson(detail) : JsonUtil.errorJson("Not found"));
        } else {
            if (!HttpApiServer.requireGet(ex)) return;
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(objectives.listObjectiveAssets()));
        }
    }

    private void handleReputation(HttpExchange ex, String sub) throws IOException {
        if (sub.equals("/groups") || sub.equals("/groups/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(reputation.listGroups()));
        } else if (sub.startsWith("/groups/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            var detail = reputation.getGroupDetail(sub.substring(8));
            HttpApiServer.sendJson(ex, detail != null ? 200 : 404,
                    detail != null ? JsonUtil.toJson(detail) : JsonUtil.errorJson("Not found"));
        } else if (sub.startsWith("/ranks/") && sub.length() > 7) {
            if (!HttpApiServer.requireGet(ex)) return;
            var detail = reputation.getRankDetail(sub.substring(7));
            HttpApiServer.sendJson(ex, detail != null ? 200 : 404,
                    detail != null ? JsonUtil.toJson(detail) : JsonUtil.errorJson("Not found"));
        } else if (sub.equals("/ranks") || sub.equals("/ranks/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(reputation.listRanks()));
        } else if (sub.equals("/change")) {
            if (!HttpApiServer.requirePost(ex)) return;
            var body = HttpApiServer.parseJsonBody(ex);
            var result = reputation.changeReputation(body.getOrDefault("worldId", "default"),
                    body.get("groupId"), Integer.parseInt(body.getOrDefault("delta", "0")));
            sendResult(ex, result);
        } else {
            if (!HttpApiServer.requireGet(ex)) return;
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(reputation.listGroups()));
        }
    }

    private void handleShops(HttpExchange ex, String sub) throws IOException {
        if (!HttpApiServer.requireGet(ex)) return;
        if (sub.equals("/barter") || sub.equals("/barter/")) {
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(shops.listBarterShops()));
        } else if (sub.startsWith("/barter/")) {
            var detail = shops.getBarterShopDetail(sub.substring(8));
            HttpApiServer.sendJson(ex, detail != null ? 200 : 404,
                    detail != null ? JsonUtil.toJson(detail) : JsonUtil.errorJson("Not found"));
        } else if (sub.startsWith("/") && sub.length() > 1) {
            var detail = shops.getShopDetail(sub.substring(1));
            HttpApiServer.sendJson(ex, detail != null ? 200 : 404,
                    detail != null ? JsonUtil.toJson(detail) : JsonUtil.errorJson("Not found"));
        } else {
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(shops.listShops()));
        }
    }

    private void handleFarming(HttpExchange ex, String sub) throws IOException {
        if (!HttpApiServer.requireGet(ex)) return;
        if (sub.equals("/coops") || sub.equals("/coops/")) {
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(farming.listCoops()));
        } else if (sub.startsWith("/coops/")) {
            var detail = farming.getCoopDetail(sub.substring(7));
            HttpApiServer.sendJson(ex, detail != null ? 200 : 404,
                    detail != null ? JsonUtil.toJson(detail) : JsonUtil.errorJson("Not found"));
        } else {
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(farming.listCoops()));
        }
    }

    private void handleMemories(HttpExchange ex, String sub) throws IOException {
        switch (sub) {
            case "", "/" -> {
                if (!HttpApiServer.requireGet(ex)) return;
                HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(memories.listAllMemories()));
            }
            case "/flat" -> {
                if (!HttpApiServer.requireGet(ex)) return;
                HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(memories.listAllMemoriesFlat()));
            }
            case "/recorded" -> {
                if (!HttpApiServer.requireGet(ex)) return;
                HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(memories.listRecordedMemories()));
            }
            case "/level" -> {
                if (!HttpApiServer.requireGet(ex)) return;
                HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(Map.of("level", memories.getMemoriesLevel())));
            }
            case "/recordAll" -> {
                if (!HttpApiServer.requirePost(ex)) return;
                sendResult(ex, memories.recordAll());
            }
            case "/clear" -> {
                if (!HttpApiServer.requirePost(ex)) return;
                sendResult(ex, memories.clearAll());
            }
            case "/setCount" -> {
                if (!HttpApiServer.requirePost(ex)) return;
                var body = HttpApiServer.parseJsonBody(ex);
                sendResult(ex, memories.setCount(Integer.parseInt(body.getOrDefault("count", "0"))));
            }
            default -> {
                if (sub.startsWith("/") && sub.length() > 1) {
                    if (!HttpApiServer.requireGet(ex)) return;
                    var detail = memories.getMemoryDetail(sub.substring(1));
                    HttpApiServer.sendJson(ex, detail != null ? 200 : 404,
                            detail != null ? JsonUtil.toJson(detail) : JsonUtil.errorJson("Not found"));
                } else {
                    HttpApiServer.sendJson(ex, 404, JsonUtil.errorJson("Unknown: " + sub));
                }
            }
        }
    }

    private void handleCamera(HttpExchange ex, String sub) throws IOException {
        if (!HttpApiServer.requireGet(ex)) return;
        if (sub.equals("/shakes") || sub.equals("/shakes/")) {
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(camera.listCameraShakes()));
        } else if (sub.startsWith("/shakes/")) {
            var detail = camera.getCameraShakeDetail(sub.substring(8));
            HttpApiServer.sendJson(ex, detail != null ? 200 : 404,
                    detail != null ? JsonUtil.toJson(detail) : JsonUtil.errorJson("Not found"));
        } else {
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(camera.listCameraShakes()));
        }
    }

    private void handleTeleporters(HttpExchange ex, String sub) throws IOException {
        if (!HttpApiServer.requireGet(ex)) return;
        var params = HttpApiServer.parseQuery(ex.getRequestURI().getQuery());
        String worldId = params.getOrDefault("world", "default");
        HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(teleporters.listTeleporters(worldId)));
    }

    private void handleNpcRoles(HttpExchange ex) throws IOException {
        if (!HttpApiServer.requireGet(ex)) return;
        HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(npcObjectives.listAvailableNpcRoles()));
    }

    private void handleNPCObjectives(HttpExchange ex, String sub) throws IOException {
        if (sub.equals("/kills") || sub.equals("/kills/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            var params = HttpApiServer.parseQuery(ex.getRequestURI().getQuery());
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(
                    npcObjectives.listActiveNPCObjectives(params.getOrDefault("world", "default"))));
        } else if (sub.equals("/assets/kill") || sub.equals("/assets/kill/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(npcObjectives.listKillTaskAssets()));
        } else if (sub.equals("/assets/killspawnmarker") || sub.equals("/assets/killspawnmarker/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(npcObjectives.listKillSpawnMarkerTaskAssets()));
        } else if (sub.equals("/assets/bounty") || sub.equals("/assets/bounty/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(npcObjectives.listBountyTaskAssets()));
        } else if (sub.equals("/start")) {
            if (!HttpApiServer.requirePost(ex)) return;
            var body = HttpApiServer.parseJsonBody(ex);
            sendResult(ex, npcObjectives.startNPCObjective(
                    body.get("entityUuid"), body.get("objectiveId"), body.getOrDefault("worldId", "default")));
        } else {
            if (!HttpApiServer.requireGet(ex)) return;
            var params = HttpApiServer.parseQuery(ex.getRequestURI().getQuery());
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(
                    npcObjectives.listActiveNPCObjectives(params.getOrDefault("world", "default"))));
        }
    }

    private void handleStash(HttpExchange ex) throws IOException {
        if (!HttpApiServer.requireGet(ex)) return;
        HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(stash.getStashInfo()));
    }

    private void handleAuthoring(HttpExchange ex, String sub) throws IOException {
        // ── Quest Lines ──
        if (sub.equals("/lines") || sub.equals("/lines/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(authoring.listQuestLines()));
        } else if (sub.equals("/lines/create")) {
            if (!HttpApiServer.requirePost(ex)) return;
            sendResult(ex, authoring.createQuestLine(HttpApiServer.parseJsonBody(ex)));
        } else if (sub.matches("/lines/[^/]+/update")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String id = extractSegment(sub, "/lines/", "/update");
            sendResult(ex, authoring.updateQuestLine(id, HttpApiServer.parseJsonBody(ex)));
        } else if (sub.matches("/lines/[^/]+/delete")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String id = extractSegment(sub, "/lines/", "/delete");
            sendResult(ex, authoring.deleteQuestLine(id));
        } else if (sub.matches("/lines/[^/]+/addObjective")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String id = extractSegment(sub, "/lines/", "/addObjective");
            var body = HttpApiServer.parseJsonBody(ex);
            int pos = -1;
            try { pos = Integer.parseInt(body.getOrDefault("position", "-1")); } catch (NumberFormatException ignored) {}
            sendResult(ex, authoring.addObjectiveToLine(id, body.get("objectiveId"), pos));
        } else if (sub.matches("/lines/[^/]+/removeObjective")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String id = extractSegment(sub, "/lines/", "/removeObjective");
            var body = HttpApiServer.parseJsonBody(ex);
            sendResult(ex, authoring.removeObjectiveFromLine(id, body.get("objectiveId")));
        } else if (sub.matches("/lines/[^/]+/addBranch")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String id = extractSegment(sub, "/lines/", "/addBranch");
            var body = HttpApiServer.parseJsonBody(ex);
            sendResult(ex, authoring.addBranch(id, body.get("nextLineId")));
        } else if (sub.matches("/lines/[^/]+/removeBranch")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String id = extractSegment(sub, "/lines/", "/removeBranch");
            var body = HttpApiServer.parseJsonBody(ex);
            sendResult(ex, authoring.removeBranch(id, body.get("nextLineId")));
        } else if (sub.startsWith("/lines/") && sub.length() > 7 && !sub.substring(7).contains("/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            var detail = authoring.getQuestLineDetail(sub.substring(7));
            HttpApiServer.sendJson(ex, detail != null ? 200 : 404,
                    detail != null ? JsonUtil.toJson(detail) : JsonUtil.errorJson("Not found"));
        }
        // ── Objectives ──
        else if (sub.equals("/objectives") || sub.equals("/objectives/")) {
            if (!HttpApiServer.requireGet(ex)) return;
            HttpApiServer.sendJson(ex, 200, JsonUtil.toJson(authoring.listObjectives()));
        } else if (sub.equals("/objectives/create")) {
            if (!HttpApiServer.requirePost(ex)) return;
            var result = authoring.createObjective(HttpApiServer.parseJsonBody(ex));
            if (Boolean.TRUE.equals(result.get("success"))) {
                var obj = authoring.findObjective((String) result.get("entityId"));
                if (obj != null) registration.registerObjective(obj);
            }
            sendResult(ex, result);
        } else if (sub.matches("/objectives/[^/]+/update")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String id = extractSegment(sub, "/objectives/", "/update");
            var result = authoring.updateObjective(id, HttpApiServer.parseJsonBody(ex));
            if (Boolean.TRUE.equals(result.get("success"))) {
                var obj = authoring.findObjective(id);
                if (obj != null) registration.registerObjective(obj);
            }
            sendResult(ex, result);
        } else if (sub.matches("/objectives/[^/]+/delete")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String id = extractSegment(sub, "/objectives/", "/delete");
            sendResult(ex, authoring.deleteObjective(id));
        }
        // ── Task Sets ──
        else if (sub.matches("/objectives/[^/]+/tasksets/add")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String id = extractSegment(sub, "/objectives/", "/tasksets/add");
            var result = authoring.addTaskSet(id, HttpApiServer.parseJsonBody(ex));
            if (Boolean.TRUE.equals(result.get("success"))) reregisterObjective(id);
            sendResult(ex, result);
        } else if (sub.matches("/objectives/[^/]+/tasksets/\\d+/delete")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String[] parts = sub.split("/");
            String objId = parts[2]; // /objectives/{id}/tasksets/{idx}/delete
            int idx = Integer.parseInt(parts[4]);
            var result = authoring.removeTaskSet(objId, idx);
            if (Boolean.TRUE.equals(result.get("success"))) reregisterObjective(objId);
            sendResult(ex, result);
        }
        // ── Tasks ──
        else if (sub.matches("/objectives/[^/]+/tasksets/\\d+/tasks/add")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String[] parts = sub.split("/");
            String objId = parts[2];
            int tsIdx = Integer.parseInt(parts[4]);
            var result = authoring.addTask(objId, tsIdx, HttpApiServer.parseJsonBody(ex));
            if (Boolean.TRUE.equals(result.get("success"))) reregisterObjective(objId);
            sendResult(ex, result);
        } else if (sub.matches("/objectives/[^/]+/tasksets/\\d+/tasks/\\d+/delete")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String[] parts = sub.split("/");
            String objId = parts[2];
            int tsIdx = Integer.parseInt(parts[4]);
            int tIdx = Integer.parseInt(parts[6]);
            var result = authoring.removeTask(objId, tsIdx, tIdx);
            if (Boolean.TRUE.equals(result.get("success"))) reregisterObjective(objId);
            sendResult(ex, result);
        }
        // ── Completions ──
        else if (sub.matches("/objectives/[^/]+/completions/add")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String id = extractSegment(sub, "/objectives/", "/completions/add");
            var result = authoring.addCompletion(id, HttpApiServer.parseJsonBody(ex));
            if (Boolean.TRUE.equals(result.get("success"))) reregisterObjective(id);
            sendResult(ex, result);
        } else if (sub.matches("/objectives/[^/]+/completions/\\d+/delete")) {
            if (!HttpApiServer.requirePost(ex)) return;
            String[] parts = sub.split("/");
            String objId = parts[2];
            int idx = Integer.parseInt(parts[4]);
            var result = authoring.removeCompletion(objId, idx);
            if (Boolean.TRUE.equals(result.get("success"))) reregisterObjective(objId);
            sendResult(ex, result);
        }
        // ── Register all ──
        else if (sub.equals("/register")) {
            if (!HttpApiServer.requirePost(ex)) return;
            registration.registerAllOnStartup();
            sendResult(ex, Map.of("success", true, "message", "All authored assets re-registered"));
        }
        else {
            HttpApiServer.sendJson(ex, 404, JsonUtil.errorJson("Unknown authoring route: " + sub));
        }
    }

    private void reregisterObjective(String id) {
        var obj = authoring.findObjective(id);
        if (obj != null) registration.registerObjective(obj);
    }

    private static String extractSegment(String path, String prefix, String suffix) {
        int start = path.indexOf(prefix) + prefix.length();
        int end = path.indexOf(suffix, start);
        return end > start ? path.substring(start, end) : "";
    }

    private void sendResult(HttpExchange ex, Map<String, Object> result) throws IOException {
        int status = Boolean.TRUE.equals(result.get("success")) ? 200 : 400;
        HttpApiServer.sendJson(ex, status, JsonUtil.toJson(result));
    }
}
