package dev.hytalemodding.hyadventure.models;

import java.util.ArrayList;
import java.util.List;

public class AuthoredTaskSet {
    private String descriptionId = "";
    private List<AuthoredTask> tasks = new ArrayList<>();

    public String getDescriptionId() { return descriptionId; }
    public void setDescriptionId(String v) { this.descriptionId = v; }
    public List<AuthoredTask> getTasks() { return tasks; }
    public void setTasks(List<AuthoredTask> tasks) { this.tasks = tasks; }
}
