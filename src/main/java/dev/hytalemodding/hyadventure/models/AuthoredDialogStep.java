package dev.hytalemodding.hyadventure.models;

import java.util.ArrayList;
import java.util.List;

public class AuthoredDialogStep {
    private String id = "";
    private String speakerNameText = "";
    private String dialogText = "";
    private String nextStepId = "";
    private List<AuthoredDialogChoice> choices = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSpeakerNameText() { return speakerNameText; }
    public void setSpeakerNameText(String v) { this.speakerNameText = v; }
    public String getDialogText() { return dialogText; }
    public void setDialogText(String v) { this.dialogText = v; }
    public String getNextStepId() { return nextStepId; }
    public void setNextStepId(String v) { this.nextStepId = v; }
    public List<AuthoredDialogChoice> getChoices() { return choices; }
    public void setChoices(List<AuthoredDialogChoice> v) { this.choices = v; }
}
