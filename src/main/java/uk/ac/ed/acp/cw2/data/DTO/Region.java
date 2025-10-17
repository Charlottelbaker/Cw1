package uk.ac.ed.acp.cw2.data.DTO;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class Region {
    @NotNull private String name;
    @NotNull private List<Position> vertices;

    public Region() {
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<Position> getVertices() {
        return vertices;
    }

    public void setVertices(List<Position> vertices) {
        this.vertices = vertices;
    }

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


    public Boolean isIn(Position dronePos) {
        int num_intersections = 0;
        for (int i = 0; i < vertices.size(); i++) {
            Position v1 = vertices.get(i);
            Position v2 = vertices.get((i + 1) % vertices.size());
            if (
                    ((v1.getLat() > dronePos.getLat()) != (v2.getLat() > dronePos.getLat())) &&
                            (dronePos.getLng() <
                                    ((v2.getLng() - v1.getLng()) *
                                            (dronePos.getLat() - v1.getLat()) /
                                            (v2.getLat() - v1.getLat()) + v1.getLng()))
            ) {
                num_intersections += 1;
            }
        }
        return num_intersections % 2 == 1;

    }

}