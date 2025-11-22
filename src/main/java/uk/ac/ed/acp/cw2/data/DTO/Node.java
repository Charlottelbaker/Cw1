package uk.ac.ed.acp.cw2.data.DTO;

class Node {
    Position pos;
    double g, h, f;
    Node parent;

    Node(Position p) {
        this.pos = p;
        this.g = Double.POSITIVE_INFINITY;
    }
}



