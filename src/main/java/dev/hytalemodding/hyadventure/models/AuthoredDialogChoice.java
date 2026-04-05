package dev.hytalemodding.hyadventure.models;

public class AuthoredDialogChoice {
    private String id = "";
    private String labelText = "";
    private String descriptionText = "";
    private String nextStepId = "";
    private String startObjectiveId = "";
    private String giveItemId = "";

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLabelText() { return labelText; }
    public void setLabelText(String v) { this.labelText = v; }
    public String getDescriptionText() { return descriptionText; }
    public void setDescriptionText(String v) { this.descriptionText = v; }
    public String getNextStepId() { return nextStepId; }
    public void setNextStepId(String v) { this.nextStepId = v; }
    public String getStartObjectiveId() { return startObjectiveId; }
    public void setStartObjectiveId(String v) { this.startObjectiveId = v; }
    public String getGiveItemId() { return giveItemId; }
    public void setGiveItemId(String v) { this.giveItemId = v; }
}
