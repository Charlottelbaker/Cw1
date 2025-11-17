package uk.ac.ed.acp.cw2.data.DTO;


import lombok.Getter;
import lombok.Setter;

@Setter@Getter
public class ServicePoint {
    private int id;
    private String name;
    private Position position;
}
