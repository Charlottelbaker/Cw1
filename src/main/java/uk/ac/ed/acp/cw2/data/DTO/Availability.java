package uk.ac.ed.acp.cw2.data.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
public class Availability {
    private DayOfWeek dayOfWeek;
    private LocalTime from;
    private LocalTime until;
}
