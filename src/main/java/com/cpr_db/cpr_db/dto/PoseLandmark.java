package com.cpr_db.cpr_db.dto;

public class PoseLandmark {

    private double x;
    private double y;
    private double z;
    private double visibility;

    public PoseLandmark() {
    }

    public PoseLandmark(double x, double y, double z, double visibility) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.visibility = visibility;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getVisibility() {
        return visibility;
    }

    public void setVisibility(double visibility) {
        this.visibility = visibility;
    }
}
