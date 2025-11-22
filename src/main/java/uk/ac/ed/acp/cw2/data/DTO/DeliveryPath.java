package uk.ac.ed.acp.cw2.data.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter@Getter
public class DeliveryPath {
    private double totalCost;
    private double totalMoves;
    private List<DronePath> dronePaths;
}
