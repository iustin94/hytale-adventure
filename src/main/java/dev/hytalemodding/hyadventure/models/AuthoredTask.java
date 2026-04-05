package dev.hytalemodding.hyadventure.models;

import java.util.ArrayList;
import java.util.List;

public class AuthoredTask {
    private TaskType type = TaskType.GATHER;
    private int count = 1;
    private String descriptionId = "";

    // Gather, UseBlock, Craft
    private String blockTagOrItemId = "";

    // UseEntity
    private String taskId = "";
    private String animationIdToPlay = "";
    private String dialogEntityNameKey = "";
    private String dialogKey = "";

    // ReachLocation
    private String targetLocationId = "";

    // Kill, KillSpawnMarker
    private String npcGroupId = "";

    // KillSpawnMarker
    private List<String> spawnMarkerIds = new ArrayList<>();
    private float radius = 1.0f;

    // Bounty
    private String npcId = "";

    public TaskType getType() { return type; }
    public void setType(TaskType type) { this.type = type; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public String getDescriptionId() { return descriptionId; }
    public void setDescriptionId(String descriptionId) { this.descriptionId = descriptionId; }
    public String getBlockTagOrItemId() { return blockTagOrItemId; }
    public void setBlockTagOrItemId(String v) { this.blockTagOrItemId = v; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getAnimationIdToPlay() { return animationIdToPlay; }
    public void setAnimationIdToPlay(String v) { this.animationIdToPlay = v; }
    public String getDialogEntityNameKey() { return dialogEntityNameKey; }
    public void setDialogEntityNameKey(String v) { this.dialogEntityNameKey = v; }
    public String getDialogKey() { return dialogKey; }
    public void setDialogKey(String v) { this.dialogKey = v; }
    public String getTargetLocationId() { return targetLocationId; }
    public void setTargetLocationId(String v) { this.targetLocationId = v; }
    public String getNpcGroupId() { return npcGroupId; }
    public void setNpcGroupId(String v) { this.npcGroupId = v; }
    public List<String> getSpawnMarkerIds() { return spawnMarkerIds; }
    public void setSpawnMarkerIds(List<String> v) { this.spawnMarkerIds = v; }
    public float getRadius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }
    public String getNpcId() { return npcId; }
    public void setNpcId(String npcId) { this.npcId = npcId; }
}
