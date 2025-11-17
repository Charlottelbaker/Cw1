package uk.ac.ed.acp.cw2.data.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
public class MedDispatchRec {
    private int id;
    private LocalDate date;
    private LocalTime time;
    private Requirements requirements;
    private Position delivery;

    public MedDispatchRec() {}


    public MedDispatchRec(int id, Requirements requirements) {
        this.id = id;
        this.requirements = requirements;
    }

    public MedDispatchRec(int id, Requirements requirements,
                          LocalDate date, LocalTime time) {
        this.id = id;
        this.requirements = requirements;
        this.date = date;
        this.time = time;
    }
}
