package uk.ac.ed.acp.cw2.data.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NextPositionRequest {

    @NotNull(message = "Start position cannot be null")
    @Valid
    private Position start;

    @NotNull(message = "Angle cannot be null")
    private Double angle;  //

    // Default constructor
    public NextPositionRequest() {}

    // Getters and setters for start
    public Position getPosition() {
        return start;
    }

    public void setStart(Position start) {
        this.start = start;
    }

    // Getters and setters for angle
    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }

    // Validation: angle must be multiple of 22.5 between 0 and 360
    @AssertTrue(message = "Angle must be a multiple of 22.5 between 0.0 and 360.0")
    public boolean isAngleValid() {
        if (angle == null) return false; // safety check
        if (angle < 0.0 || angle > 360.0) return false;

        double remainder = angle % 22.5;
        return remainder < 0.00015 || Math.abs(remainder - 22.5) < 0.00015;
    }
}
