package uk.ac.ed.acp.cw2.data.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter@Setter
public class Drone {
    private String name;
    private String id;
    private Capabilities capability;

    public Drone() {}
}
