package uk.ac.ed.acp.cw2.data.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter@Getter
public class DroneCandidate {
    private Drone drone;
    private AvailableDrone availableDrone;
    private ServicePoint homeSP;

    public DroneCandidate(Drone drone, AvailableDrone ad, ServicePoint sp) {
        this.drone = drone;
        this.availableDrone = ad;
        this.homeSP = sp;
    }

}
