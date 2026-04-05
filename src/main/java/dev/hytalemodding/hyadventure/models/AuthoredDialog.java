package dev.hytalemodding.hyadventure.models;

import java.util.ArrayList;
import java.util.List;

public class AuthoredDialog {
    private String id = "";
    private String entityNameKey = "";
    private String dialogKey = "";
    private String entityNameText = "";
    private String dialogText = "";
    private int sequence = 0;
    private List<AuthoredDialogStep> steps = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEntityNameKey() { return entityNameKey; }
    public void setEntityNameKey(String v) { this.entityNameKey = v; }
    public String getDialogKey() { return dialogKey; }
    public void setDialogKey(String v) { this.dialogKey = v; }
    public String getEntityNameText() { return entityNameText; }
    public void setEntityNameText(String v) { this.entityNameText = v; }
    public String getDialogText() { return dialogText; }
    public void setDialogText(String v) { this.dialogText = v; }
    public int getSequence() { return sequence; }
    public void setSequence(int v) { this.sequence = v; }
    public List<AuthoredDialogStep> getSteps() { return steps; }
    public void setSteps(List<AuthoredDialogStep> v) { this.steps = v; }
}
