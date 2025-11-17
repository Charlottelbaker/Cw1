package uk.ac.ed.acp.cw2.data.DTO;


import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RestrictedArea {
        private int id;
        private List<Position> vertices;


    @AssertTrue(message = "Region must have at least 4 vertices, and the first and last must match")
    public boolean isVerticesValid() {
        if (vertices == null || vertices.size() < 4) {
            return false;
        }

        Position first = vertices.getFirst();
        Position last = vertices.getLast();

        // We only need ~0.00015 tolerance (same as your angle precision)
        double tolerance = 0.00015;
        boolean sameLat = Math.abs(first.getLat() - last.getLat()) < tolerance;
        boolean sameLng = Math.abs(first.getLng() - last.getLng()) < tolerance;

        return sameLat && sameLng;
    }
    }

