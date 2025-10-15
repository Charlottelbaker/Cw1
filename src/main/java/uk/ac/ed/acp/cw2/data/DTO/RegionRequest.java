package uk.ac.ed.acp.cw2.data.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class RegionRequest {
    @NotNull @Valid private Position position;
    @NotNull @Valid private Region region;

    public RegionRequest() {}

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }
}
