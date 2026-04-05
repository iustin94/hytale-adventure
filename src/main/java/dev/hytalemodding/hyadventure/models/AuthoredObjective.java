package dev.hytalemodding.hyadventure.models;

import java.util.ArrayList;
import java.util.List;

public class AuthoredObjective {
    private String id = "";
    private String category = "custom";
    private String titleKey = "";
    private String descriptionKey = "";
    private boolean removeOnItemDrop = false;
    private List<AuthoredTaskSet> taskSets = new ArrayList<>();
    private List<AuthoredCompletion> completions = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String v) { this.category = v; }
    public String getTitleKey() { return titleKey; }
    public void setTitleKey(String v) { this.titleKey = v; }
    public String getDescriptionKey() { return descriptionKey; }
    public void setDescriptionKey(String v) { this.descriptionKey = v; }
    public boolean isRemoveOnItemDrop() { return removeOnItemDrop; }
    public void setRemoveOnItemDrop(boolean v) { this.removeOnItemDrop = v; }
    public List<AuthoredTaskSet> getTaskSets() { return taskSets; }
    public void setTaskSets(List<AuthoredTaskSet> v) { this.taskSets = v; }
    public List<AuthoredCompletion> getCompletions() { return completions; }
    public void setCompletions(List<AuthoredCompletion> v) { this.completions = v; }
}
