package uk.ac.ed.acp.cw2.DTO;

public class Position {
    private double lat;
    private double lng;

    // Constructor
    public Position() {}

    // Getters and setters
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public boolean isValid() {
        // Edinburgh is around lat ~56N and lng ~3W
        return (lat > 0 && lat >= 55 && lat <= 57) &&
                (lng < 0 && lng >= -4 && lng <= -2);
    }
}
