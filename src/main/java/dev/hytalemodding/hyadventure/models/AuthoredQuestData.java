package dev.hytalemodding.hyadventure.models;

import java.util.ArrayList;
import java.util.List;

public class AuthoredQuestData {
    private List<AuthoredQuestLine> questLines = new ArrayList<>();
    private List<AuthoredObjective> objectives = new ArrayList<>();
    private List<AuthoredNpcAssignment> npcAssignments = new ArrayList<>();
    private List<AuthoredDialog> dialogs = new ArrayList<>();
    private List<AuthoredLocation> locations = new ArrayList<>();

    public List<AuthoredQuestLine> getQuestLines() { return questLines; }
    public void setQuestLines(List<AuthoredQuestLine> v) { this.questLines = v; }
    public List<AuthoredObjective> getObjectives() { return objectives; }
    public void setObjectives(List<AuthoredObjective> v) { this.objectives = v; }
    public List<AuthoredNpcAssignment> getNpcAssignments() { return npcAssignments; }
    public void setNpcAssignments(List<AuthoredNpcAssignment> v) { this.npcAssignments = v; }
    public List<AuthoredDialog> getDialogs() { return dialogs; }
    public void setDialogs(List<AuthoredDialog> v) { this.dialogs = v; }
    public List<AuthoredLocation> getLocations() { return locations; }
    public void setLocations(List<AuthoredLocation> v) { this.locations = v; }
}
