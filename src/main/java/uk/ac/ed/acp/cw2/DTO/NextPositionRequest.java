package uk.ac.ed.acp.cw2.DTO;

public class NextPositionRequest {
    private Position start;
    private double angle;  // degrees

    public NextPositionRequest() {}

    public Position getPosition() { return start; }
    public void setPosition(Position start) { this.start = start; }

    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }
}

