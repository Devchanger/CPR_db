package com.cpr_db.cpr_db.dto;

public class AngleAnalysis {

    private double leftArmAngle;
    private double rightArmAngle;
    private boolean isArmsVertical;

    public AngleAnalysis() {
    }

    public AngleAnalysis(double leftArmAngle, double rightArmAngle, boolean isArmsVertical) {
        this.leftArmAngle = leftArmAngle;
        this.rightArmAngle = rightArmAngle;
        this.isArmsVertical = isArmsVertical;
    }

    public double getLeftArmAngle() {
        return leftArmAngle;
    }

    public void setLeftArmAngle(double leftArmAngle) {
        this.leftArmAngle = leftArmAngle;
    }

    public double getRightArmAngle() {
        return rightArmAngle;
    }

    public void setRightArmAngle(double rightArmAngle) {
        this.rightArmAngle = rightArmAngle;
    }

    public boolean getIsArmsVertical() {
        return isArmsVertical;
    }

    public void setIsArmsVertical(boolean isArmsVertical) {
        this.isArmsVertical = isArmsVertical;
    }
}
