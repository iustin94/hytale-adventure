package dev.hytalemodding.hyadventure.models;

public class AuthoredLocation {
    private String id = "";
    private String label = "";
    private double x;
    private double y;
    private double z;
    private float radius = 5.0f;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String v) { this.label = v; }
    public double getX() { return x; }
    public void setX(double v) { this.x = v; }
    public double getY() { return y; }
    public void setY(double v) { this.y = v; }
    public double getZ() { return z; }
    public void setZ(double v) { this.z = v; }
    public float getRadius() { return radius; }
    public void setRadius(float v) { this.radius = v; }
}
