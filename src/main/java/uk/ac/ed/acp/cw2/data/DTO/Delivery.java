package uk.ac.ed.acp.cw2.data.DTO;

import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Getter@Setter
public class Delivery {
    private int deliveryId;
    private List<Position> flightPath;
    //flightPath as a [] of LngLat entries which are your moves.
    // You start with the service point, do your first delivery,
    // continue and terminate at the service point. Each flightPath
    // is from the logical start (previous delivery or service point)
    // until the next delivery (or service point).

    //A delivery is indicated by having 2 identical records in
    // the flightPath (so no move, just a hover).
}
