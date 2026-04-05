package dev.hytalemodding.hyadventure.models;

public class AuthoredCompletion {
    private CompletionType type = CompletionType.GIVE_ITEMS;
    private String dropListId = "";
    private String reputationGroupId = "";
    private int reputationAmount = 1;

    public CompletionType getType() { return type; }
    public void setType(CompletionType type) { this.type = type; }
    public String getDropListId() { return dropListId; }
    public void setDropListId(String v) { this.dropListId = v; }
    public String getReputationGroupId() { return reputationGroupId; }
    public void setReputationGroupId(String v) { this.reputationGroupId = v; }
    public int getReputationAmount() { return reputationAmount; }
    public void setReputationAmount(int v) { this.reputationAmount = v; }
}
