package dev.hytalemodding.hyadventure.models;

public class AuthoredNpcAssignment {
    private String id = "";
    private String npcRole = "";
    private NpcAssignmentType assignmentType = NpcAssignmentType.QUEST_GIVER;
    private String dialogId = "";
    private String locationId = "";

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNpcRole() { return npcRole; }
    public void setNpcRole(String v) { this.npcRole = v; }
    public NpcAssignmentType getAssignmentType() { return assignmentType; }
    public void setAssignmentType(NpcAssignmentType v) { this.assignmentType = v; }
    public String getDialogId() { return dialogId; }
    public void setDialogId(String v) { this.dialogId = v; }
    public String getLocationId() { return locationId; }
    public void setLocationId(String v) { this.locationId = v; }
}
