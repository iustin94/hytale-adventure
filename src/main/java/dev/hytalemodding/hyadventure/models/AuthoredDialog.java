package dev.hytalemodding.hyadventure.models;

public class AuthoredDialog {
    private String id = "";
    private String entityNameKey = "";
    private String dialogKey = "";
    private String entityNameText = "";
    private String dialogText = "";
    private int sequence = 0;

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
}
