package uk.ac.ed.acp.cw2.data.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Requirements {
    private Double capacity;
    private boolean cooling;
    private boolean heating;
    private Double maxCost;

    public Requirements() {}

    public Requirements(Double capacity) {
        this.capacity = capacity;
    }

    public Requirements(Double capacity, boolean cooling, boolean heating, Double maxCost) {
        this.capacity = capacity;
        this.cooling = cooling;
        this.heating = heating;
        this.maxCost = maxCost;
    }

}
