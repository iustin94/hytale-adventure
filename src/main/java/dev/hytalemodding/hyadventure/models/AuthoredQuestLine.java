package dev.hytalemodding.hyadventure.models;

import java.util.ArrayList;
import java.util.List;

public class AuthoredQuestLine {
    private String id = "";
    private String category = "custom";
    private List<String> objectiveIds = new ArrayList<>();
    private List<String> nextQuestLineIds = new ArrayList<>();
    private String titleKey = "";
    private String descriptionKey = "";
    private String questGiverNpcId = "";

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String v) { this.category = v; }
    public List<String> getObjectiveIds() { return objectiveIds; }
    public void setObjectiveIds(List<String> v) { this.objectiveIds = v; }
    public List<String> getNextQuestLineIds() { return nextQuestLineIds; }
    public void setNextQuestLineIds(List<String> v) { this.nextQuestLineIds = v; }
    public String getTitleKey() { return titleKey; }
    public void setTitleKey(String v) { this.titleKey = v; }
    public String getDescriptionKey() { return descriptionKey; }
    public void setDescriptionKey(String v) { this.descriptionKey = v; }
    public String getQuestGiverNpcId() { return questGiverNpcId; }
    public void setQuestGiverNpcId(String v) { this.questGiverNpcId = v; }
}
