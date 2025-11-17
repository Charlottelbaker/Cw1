package uk.ac.ed.acp.cw2.data.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class Capabilities {
    private boolean cooling;
    private boolean heating;
    private double capacity;
    private double maxMoves;
    private double costPerMove;
    private double costInitial;
    private double costFinal;
    //Total Cost for flight = Constants for take-off and return + moves on path * cost / move
    //                         costInitial  +  costFinal  +  (moves on path * costPerMove)
}
