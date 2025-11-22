package uk.ac.ed.acp.cw2.data.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter@Getter
public class Cluster {
    private List<MedDispatchRec> requests;
    private Position servicePoint;
    private Drone drone;

    public Cluster(List<MedDispatchRec> requests, Position servicePoint, Drone drone) {
        this.requests = requests;
        this.servicePoint = servicePoint;
        this.drone = drone;
    }

}