package uk.ac.ed.acp.cw2.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.data.DTO.Position;
import uk.ac.ed.acp.cw2.data.DTO.RestrictedArea;

import java.util.*;

@Service
public class AStarService {

    private static final double STEP = 0.00015;   // size of each drone move

    // 16 compass directions (22.5° apart)
    private static final List<double[]> DIRECTIONS = new ArrayList<>();

    static {
        for (int i = 0; i < 16; i++) {
            double angle = Math.toRadians(i * 22.5);
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);

            double length = Math.sqrt(dx*dx + dy*dy);
            dx = (dx / length) * STEP;
            dy = (dy / length) * STEP;

            DIRECTIONS.add(new double[]{dy, dx});   // dy = lat, dx = lng
        }
    }

    // A* Node wrapper
    private static class Node {
        Position pos;
        double g, h, f;
        Node parent;

        Node(Position p) {
            this.pos = p;
            this.g = Double.POSITIVE_INFINITY;
        }
    }

    // Main callable method
    public List<Position> findPath(Position start, Position goal, RestrictedArea[] noFlyZones) {
        return aStar(start, goal, noFlyZones);
    }

    /**
     * ----------------------
     *  A* IMPLEMENTATION
     * ----------------------
     */
    private List<Position> aStar(Position start, Position goal, RestrictedArea[] noFlyZones) {

        Node startNode = new Node(copy(start));
        Node goalNode = new Node(copy(goal));

        startNode.g = 0;
        startNode.h = heuristic(start, goal);
        startNode.f = startNode.h;

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        open.add(startNode);

        Map<String, Node> allNodes = new HashMap<>();
        allNodes.put(key(startNode.pos), startNode);

        Set<String> closed = new HashSet<>();

        while (!open.isEmpty()) {
           // System.out.println("open isnt empty");

            Node current = open.poll();
            String currKey = key(current.pos);

            if (closed.contains(currKey)) continue;
            closed.add(currKey);

            // reached goal
            if (heuristic(current.pos, goal) <= STEP) {
                return buildPath(current);
            }

            // expand 16 neighbours
            for (double[] dir : DIRECTIONS) {

                Position newPos = new Position();
                newPos.setLat(current.pos.getLat() + dir[0]);
                newPos.setLng(current.pos.getLng() + dir[1]);
                newPos = snapToGrid(newPos); // snap to grid here


                if (!isFlyable(newPos, List.of(noFlyZones))) continue;

                String nKey = key(newPos);
                Node neighbour = allNodes.getOrDefault(nKey, new Node(newPos));
                allNodes.putIfAbsent(nKey, neighbour);

                double tentativeG = current.g + STEP;

                if (tentativeG < neighbour.g) {
                    neighbour.parent = current;
                    neighbour.g = tentativeG;
                    neighbour.h = heuristic(newPos, goal);
                    neighbour.f = neighbour.g + neighbour.h;
                    open.add(neighbour);
                }
            }
        }

        return Collections.emptyList(); // no route possible
    }

    /**
     * -------------------------
     *   Utility Methods
     * -------------------------
     */

    private List<Position> buildPath(Node end) {
        LinkedList<Position> path = new LinkedList<>();
        Node cur = end;

        while (cur != null) {
            path.addFirst(cur.pos);
            cur = cur.parent;
        }

        return path;
    }

    private static double heuristic(Position a, Position b) {
        double dx = a.getLng() - b.getLng();
        double dy = a.getLat() - b.getLat();
        return Math.sqrt(dx*dx + dy*dy);
    }

    private Position snapToGrid(Position pos) {
        Position snapped = new Position();
        snapped.setLat(Math.round(pos.getLat() / STEP) * STEP);
        snapped.setLng(Math.round(pos.getLng() / STEP) * STEP);
        return snapped;
    }

    private static String key(Position p) {
        return Math.round(p.getLat() * 1e6) + "," +
                Math.round(p.getLng() * 1e6);
    }

    private static Position copy(Position p) {
        Position c = new Position();
        c.setLat(p.getLat());
        c.setLng(p.getLng());
        return c;
    }

    /**
     * ------------------------------
     *    No-Fly Zone Logic
     * ------------------------------
     */

    private boolean isFlyable(Position p, List<RestrictedArea> noFlyZones) {
        if (p.getLat() < -90 || p.getLat() > 90) return false;
        if (p.getLng() < -180 || p.getLng() > 180) return false;

        for (RestrictedArea area : noFlyZones) {
            if (pointInsidePolygon(p, area.getVertices())) return false;
        }
        return true;
    }

    // Ray-casting algorithm for point-in-polygon
    private boolean pointInsidePolygon(Position point, List<Position> vertices) {
        boolean result = false;
        int j = vertices.size() - 1;

        for (int i = 0; i < vertices.size(); i++) {
            Position vi = vertices.get(i);
            Position vj = vertices.get(j);

            boolean intersects =
                    (vi.getLng() > point.getLng()) != (vj.getLng() > point.getLng()) &&
                            (point.getLat() < (vj.getLat() - vi.getLat()) *
                                    (point.getLng() - vi.getLng()) /
                                    (vj.getLng() - vi.getLng()) + vi.getLat());

            if (intersects) result = !result;
            j = i;
        }
        return result;
    }






}
