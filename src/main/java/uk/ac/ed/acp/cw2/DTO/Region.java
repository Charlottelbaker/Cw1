package uk.ac.ed.acp.cw2.DTO;

import java.util.List;

public class Region {
    private String name;
    private List<Position> vertices;

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

    public boolean isValid() {
        if (vertices == null || vertices.size() < 4) {
            // Need at least 4 points because the last one must repeat the first
            // and polygon needs at least 3 points
            return false;
        }

        Position first = vertices.getFirst();
        Position last = vertices.getLast();

        if (Math.abs(first.getLat() - last.getLat()) > 1e-9 ||
                Math.abs(first.getLng() - last.getLng()) > 1e-9) {
            return false;}
    return true;
    }


    public Boolean isIn(Position dronePos) {
        if (!isValid()) {return null;}
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