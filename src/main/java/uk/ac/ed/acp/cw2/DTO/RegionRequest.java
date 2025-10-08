package uk.ac.ed.acp.cw2.DTO;

public class RegionRequest {
    private Position position;
    private Region region;

    public RegionRequest() {}

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }
}
