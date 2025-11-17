package uk.ac.ed.acp.cw2.data.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DroneForServicePoint {
    private int servicePointId;
    private List<AvailableDrone> drones;
}
