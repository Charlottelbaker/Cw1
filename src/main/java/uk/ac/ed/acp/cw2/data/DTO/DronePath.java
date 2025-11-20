package uk.ac.ed.acp.cw2.data.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter@Getter
public class DronePath {
    private int droneId;
    private List<Delivery> deliveries;
}
