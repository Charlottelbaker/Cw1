package uk.ac.ed.acp.cw2.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.data.DTO.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class DroneService {
    private static final double STEP_SIZE = 0.00015;

    private final BasicService myService;
    private final DataService myDataService;
    private final AStarService myRouter;

    public DroneService(BasicService Service, DataService dataService, AStarService Astarservice) {
        this.myDataService = dataService;
        this.myService = Service;
        this.myRouter = Astarservice;
    }

    public List<String> findDronesWithCooling(boolean state) {
        Drone[] drones = myDataService.getAllDrones();
        List<String> result = new ArrayList<>();
        for (Drone d : drones) {
            if (d == null || d.getCapability() == null) {
                continue;
            }
            if (d.getCapability().isCooling() == state) {
                result.add(d.getId());
            }
        }
        return result;
    }

    public List<String> findDronesWithHeating(boolean state) {
        Drone[] drones = myDataService.getAllDrones();
        List<String> result = new ArrayList<>();
        for (Drone d : drones) {
            if (d == null || d.getCapability() == null) {
                continue;
            }
            if (d.getCapability().isHeating() == state) {
                result.add(d.getId());
            }
        }
        return result;
    }

    public Drone findDroneDetails(String id) {
        Drone[] drones = myDataService.getAllDrones();
        for (Drone d : drones) {
            if (d == null) {
                continue;
            }
            if (Objects.equals(d.getId(), id)) {
                return d;
            }
        }
        return null;
    }

    private boolean droneMatches(Drone d, String attribute, String operator, String value) {

        Capabilities c = d.getCapability();

        switch (attribute.toLowerCase()) {

            case "cooling":
                return compareBoolean(c.isCooling(), operator, value);

            case "heating":
                return compareBoolean(c.isHeating(), operator, value);

            case "capacity":
                return compareDouble(c.getCapacity(), operator, value);

            case "maxmoves":
                return compareDouble(c.getMaxMoves(), operator, value);

            case "costpermove":
                return compareDouble(c.getCostPerMove(), operator, value);

            case "costinitial":
                return compareDouble(c.getCostInitial(), operator, value);

            case "costfinal":
                return compareDouble(c.getCostFinal(), operator, value);

            default:
                return false;
        }
    }

    private boolean compareBoolean(boolean droneValue, String operator, String value) {
        boolean v = Boolean.parseBoolean(value);
        return "=".equals(operator) && droneValue == v;
    }

    private boolean compareDouble(double droneValue, String operator, String value) {
        double v = Double.parseDouble(value);
        switch (operator) {
            case "=":
                return droneValue == v;
            case "<":
                return droneValue < v;
            case ">":
                return droneValue > v;
            case "<=":
                return droneValue <= v;
            case ">=":
                return droneValue >= v;
            default:
                return false;
        }
    }


    public List<String> findDroneFromQuery(String attribute, String value) {
        Drone[] drones = myDataService.getAllDrones();
        List<String> result = new ArrayList<>();
        for (Drone d : drones) {
            if (droneMatches(d, attribute, "=", value)) {
                result.add(d.getId());
            }
        }
        return (result);
    }

    public List<String> queryDrones(List<QueryRequest> queries) {
        Drone[] drones = myDataService.getAllDrones();
        List<String> result = new ArrayList<>();
        for (Drone d : drones) {
            boolean matchesAll = true;
            for (QueryRequest q : queries) {
                if (!droneMatches(d, q.getAttribute(), q.getOperator(), q.getValue())) {
                    matchesAll = false;
                    break;
                }
            }
            if (matchesAll) {
                result.add(d.getId());
            }
        }
        return result;
    }

    public DronesForServicePoint getDronesAtServicePoint(int id, DronesForServicePoint[] allDronesForServicePoints) {
        for (DronesForServicePoint d : allDronesForServicePoints) {
            if (d.getServicePointId() == id) {
                return d;
            }
        }
        return null;
    }


    public List<DroneCandidate> getDronesForMedRec(List<MedDispatchRec> requests) {

        if (requests.isEmpty()) return Collections.emptyList();
        ServicePoint[] allServicePoints = myDataService.getServicePoints();

        // This will hold drones that pass capability, cost, cooling/heating, availability
        List<DroneCandidate> candidates = new ArrayList<>();

        for (ServicePoint sp : allServicePoints) {
            System.out.println(sp.getName() + " service point");
            List<AvailableDrone> dronesAtSP =
                    getDronesAtServicePoint(sp.getId(), myDataService.getDronesForServicePoints()).getDrones();
            for (AvailableDrone ad : dronesAtSP) {
                Drone drone = findDroneDetails(String.valueOf(ad.getId()));
                if (drone == null) {
                    System.err.println("ERROR: No drone found for AvailableDrone id=" + ad.getId());
                }
                if (canDroneHandleAllRequests(drone, ad, requests, sp)) {
                    candidates.add(new DroneCandidate(drone, ad, sp));
                    System.out.println(drone.getName() + sp.getName() + "\n");
                }
            }
        }

        // If no drone passed basic requirements:
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<DroneCandidate> result = new ArrayList<>();

        for (DroneCandidate cand : candidates) {
            ServicePoint bestSP = findBestServicePoint(requests);
            // Compute total route distance starting from the best SP
            double distance = routeDistanceStartingAt(bestSP.getLocation(), requests);
            double maxMoves = cand.getDrone().getCapability().getMaxMoves();
            double requiredMoves = distance / STEP_SIZE;
            if (requiredMoves <= maxMoves) {
                result.add(cand);
            }
        }
        return candidates;
    }


    public List<DroneCandidate> getDronesForMedRecOneTrip(List<MedDispatchRec> requests) {

        if (requests.isEmpty()) return Collections.emptyList();
        ServicePoint[] allServicePoints = myDataService.getServicePoints();

        // This will hold drones that pass capability, cost, cooling/heating, availability
        List<DroneCandidate> candidates = new ArrayList<>();

        for (ServicePoint sp : allServicePoints) {
            System.out.println(sp.getName() + " service point");
            List<AvailableDrone> dronesAtSP =
                    getDronesAtServicePoint(sp.getId(), myDataService.getDronesForServicePoints()).getDrones();
            for (AvailableDrone ad : dronesAtSP) {
                Drone drone = findDroneDetails(String.valueOf(ad.getId()));
                if (drone == null) {
                    System.err.println("ERROR: No drone found for AvailableDrone id=" + ad.getId());
                }
                if (canDroneHandleAllRequests(drone, ad, requests, sp)) {
                    candidates.add(new DroneCandidate(drone, ad, sp));
                    System.out.println(drone.getName() + sp.getName() + "\n");
                }
            }
        }

        // If no drone passed basic requirements:
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        return candidates;
    }


        private ServicePoint findBestServicePoint(List<MedDispatchRec> requests) {

            ServicePoint[] sps = myDataService.getServicePoints();
            ServicePoint best = null;
            double bestDist = Double.MAX_VALUE;

            for (ServicePoint sp : sps) {
                double total = routeDistanceStartingAt(sp.getLocation(), requests);
                if (total < bestDist) {
                    bestDist = total;
                    best = sp;
                }
            }
            return best;
        }

        private double routeDistanceStartingAt(Position start, List<MedDispatchRec> requests) {

            List<MedDispatchRec> remaining = new ArrayList<>(requests);
            Position current = start;
            double total = 0;

            while (!remaining.isEmpty()) {
                MedDispatchRec closest = null;
                double min = Double.MAX_VALUE;

                for (MedDispatchRec r : remaining) {
                    double d = myService.calculateDistance(current, r.getDelivery());
                    if (d < min) {
                        min = d;
                        closest = r;
                    }
                }

                total += min;
                current = closest.getDelivery();
                remaining.remove(closest);
            }

            return total;
        }

    private MedDispatchRec getClosest(List<MedDispatchRec> list, Position current) {
        MedDispatchRec closest = null;
        double min = Double.MAX_VALUE;

        for (MedDispatchRec r : list) {
            double d = myService.calculateDistance(current, r.getDelivery());
            if (d < min) {
                min = d;
                closest = r;
            }
        }

        return closest;
    }




private boolean canHandleAllInOneTrip(Drone drone, AvailableDrone availableDrone,
                                      List<MedDispatchRec> requests, ServicePoint sp) {

    double totalMoves = 0.0;
    double totalCapacity = 0.0;

    // Greedy ordering to simulate route
    List<MedDispatchRec> copy = new ArrayList<>(requests);
    Position prev = sp.getLocation();

    while (!copy.isEmpty()) {
        MedDispatchRec closest = getClosest(copy, prev);
        totalMoves += calculateMoves(prev, closest.getDelivery());
        prev = closest.getDelivery();
        copy.remove(closest);
    }

    // Return to SP
    totalMoves += calculateMoves(prev, sp.getLocation());

    // Combined capacity
    for (MedDispatchRec r : requests) {
        totalCapacity += r.getRequirements().getCapacity();
    }

    // Every req must be valid under the "all in one" constraints
    for (MedDispatchRec r : requests) {
        if (!isDroneSuitableForSingleReq(
                r, drone, availableDrone,
                totalMoves,
                requests.size(),
                totalCapacity)) {
            return false;
        }
    }

    return true;
}



    private boolean canHandleMultiTrip(Drone drone, AvailableDrone availableDrone,
                                       List<MedDispatchRec> requests, ServicePoint sp) {

        for (MedDispatchRec r : requests) {

            double moves = calculateMoves(sp.getLocation(), r.getDelivery())
                    + calculateMoves(r.getDelivery(), sp.getLocation());

            double capacity = r.getRequirements().getCapacity();

            // If any single request fails its own trip -> cannot multitrip
            if (!isDroneSuitableForSingleReq(
                    r, drone, availableDrone,
                    moves,
                    1,
                    capacity)) {
                return false;
            }
        }

        return true;
    }



    private boolean canDroneHandleAllRequests(Drone drone, AvailableDrone availableDrone,
                                              List<MedDispatchRec> requests, ServicePoint sp) {

        if (requests == null || requests.isEmpty()) {
            return true;
        }

        boolean allInOneOk = canHandleAllInOneTrip(drone, availableDrone, requests, sp);
        boolean multiTripOk = canHandleMultiTrip(drone, availableDrone, requests, sp);

        // Drone works if either plan is possible
        return allInOneOk || multiTripOk;
    }



    private boolean isDroneSuitableForSingleReq(MedDispatchRec request,
                                                    Drone drone,
                                                    AvailableDrone availableDrone,
                                                    double moves,
                                                    int requestCount,
                                                    double capacity) {

            Requirements req = request.getRequirements();

            // Capacity total
            if (drone.getCapability().getCapacity() < capacity) {
                System.out.println(drone.getId());
                System.out.println("capacity =" + capacity + "but this drone has " + drone.getCapability().getCapacity());
                return false;
            }

            // Moves
            //System.out.println("yay moves =" + moves + "and this drone has " + drone.getCapability().getMaxMoves());
            if (drone.getCapability().getMaxMoves() < moves) {
                System.out.println(drone.getId());
                System.out.println("moves =" + moves + "but this drone has " + drone.getCapability().getMaxMoves());
                return false;
            }

            // Cooling/heating
            if (req.isCooling() && !drone.getCapability().isCooling()) {
                System.out.println(drone.getId());
                System.out.println("cool");
                return false;
            }
            if (req.isHeating() && !drone.getCapability().isHeating()) {
                System.out.println(drone.getId());
                System.out.println("heat");
                return false;
            }

            // Availability
            if (!isDroneAvailableAt(drone, availableDrone, request)) {
                System.out.println(drone.getId());
                System.out.println("available");
                return false;
            }

            // Cost
            if (!withinCostLimit(drone, req, moves, requestCount)) {
                System.out.println(drone.getId());
                System.out.println("cost");

                return false;
            }

            return true;
        }


        private double calculateDistance(Position p1, Position p2) {
            double dx = p1.getLat() - p2.getLat();
            double dy = p1.getLng() - p2.getLng();
            System.out.println(Math.sqrt(dx * dx + dy * dy));
            return Math.sqrt(dx * dx + dy * dy);
        }

        private double calculateMovesbad(Position p1, Position p2) {
            System.out.println(calculateDistance(p1, p2) / STEP_SIZE);
            return calculateDistance(p1, p2) / STEP_SIZE;
        }

        private double calculateMoves(Position p1, Position p2) {
            double stepSize = 0.00015;

            double dLat = Math.abs(p1.getLat() - p2.getLat());
            double dLng = Math.abs(p1.getLng() - p2.getLng());

            int stepsLat = (int)Math.round(dLat / stepSize);
            int stepsLng = (int)Math.round(dLng / stepSize);

            return Math.max(stepsLat, stepsLng);
        }


    private boolean withinCostLimit(Drone drone, Requirements req, double moves, int reqCount) {
            if (req.getMaxCost() == null) return true;
            double total = drone.getCapability().getCostInitial()
                    + drone.getCapability().getCostFinal()
                    + (moves * drone.getCapability().getCostPerMove());
            double perDispatch = total / reqCount;
            return perDispatch <= req.getMaxCost();
        }

        private boolean isDroneAvailableAt(Drone drone,
                                           AvailableDrone availableDrone,
                                           MedDispatchRec req) {

            if (req.getDate() == null || req.getTime() == null) {
                return true;  // no constraint
            }

            DayOfWeek day = req.getDate().getDayOfWeek();
            LocalTime time = req.getTime();

            for (Availability a : availableDrone.getAvailability()) {
                if (a.getDayOfWeek() == day &&
                        !time.isBefore(a.getFrom()) &&
                        !time.isAfter(a.getUntil())) {

                    return true;
                }
            }

            return false;
        }

    // gets a bunch of reqests, trys to find the most efficient way to deleiver them
    // maybe before doing anything loop through them and group them into near by ones
    // like get a ratio of nearness to group them
    // and if they are all suffieceintly near they are all grouped
    // also grouped nearness by day, like if they are difrent days obvs not near. do that first
    // once i have groups - run avaible drones on them, see if  ther is a drone which can do it
    // then once there is calc the path using a greedy algorithm to choose the next drone and u A* for the route
    // need to consider no fly zones

        public DeliveryPath calcDeliveryPath(List<MedDispatchRec> requests) {
            List<List<MedDispatchRec>> clustersByDay = clusterByDay(requests);
            DeliveryPath deliveryPath = new DeliveryPath();
            deliveryPath.setDronePaths(new ArrayList<>());

            double totalMoves = 0;
            double totalCost = 0;

            for (List<MedDispatchRec> dailyRequests : clustersByDay) {
                List<MedDispatchRec> remaining = new ArrayList<>(dailyRequests);
                while (!remaining.isEmpty()) {
                    MedDispatchRec seed = remaining.get(0);
                    List<MedDispatchRec> candidateCluster = new ArrayList<>();
                    candidateCluster.add(seed);
                    boolean addedAny;
                    do{
                        addedAny = false;
                        for (MedDispatchRec r : remaining) {
                            if (candidateCluster.contains(r)) continue;
                            List<MedDispatchRec> trial = new ArrayList<>(candidateCluster);
                            trial.add(r);
                            List<DroneCandidate> drones = getDronesForMedRecOneTrip(trial);

                            if (!drones.isEmpty()) {
                                candidateCluster.add(r);
                                addedAny = true;
                            }

                        }
                    }while (addedAny);

                    List<DroneCandidate> clusterDrones = getDronesForMedRec(candidateCluster);
                    if (clusterDrones.isEmpty()) {
                        System.err.println("SOMETHING VERY BAD HAPPENED");
                    }

                    DroneCandidate bestCand = clusterDrones.get(0);
                    DronePath dronePath = new DronePath();

                    Cluster assignedCluster = new Cluster(candidateCluster, bestCand.getHomeSP().getLocation(), bestCand.getDrone());
                    dronePath.setDroneId(Integer.parseInt(bestCand.getDrone().getId()));
                    dronePath.setDeliveries(new ArrayList<>());

                    Position currentPos = assignedCluster.getServicePoint();
                    List<MedDispatchRec> toDeliver = new ArrayList<>(assignedCluster.getRequests());
                    double movesForThisDrone = 0;

                    System.out.print("drone: "+ bestCand.getDrone().getName() + "is going: ");
                    System.out.print(bestCand.getHomeSP().getName() + "--> (");
                    while (!toDeliver.isEmpty()) {
                        MedDispatchRec next = getClosest(toDeliver, currentPos);
                        Delivery deliveryDto = new Delivery();
                        deliveryDto.setDeliveryId(next.getId());
                        System.out.print(next.getId() + "--> (");
                        List<Position> flightPath = myRouter.findPath(currentPos, next.getDelivery(), myDataService.getRestrictedAreas());

                        Position hover = flightPath.getLast();
                        flightPath.add(hover);

                        deliveryDto.setFlightPath(flightPath);
                        dronePath.getDeliveries().add(deliveryDto);

                        movesForThisDrone += flightPath.size();
                        System.out.print(flightPath.size() + " steps) ");

                        // advance currentPos and remove delivered rec
                        currentPos = next.getDelivery();
                        toDeliver.remove(next);
                    }
                    List<Position> addTo = dronePath.getDeliveries().getLast().getFlightPath();
                    int before = addTo.size();
                    addTo.addAll(myRouter.findPath(currentPos, assignedCluster.getServicePoint(), myDataService.getRestrictedAreas()));
                    Position hover = addTo.getLast();
                    addTo.add(hover);
                    movesForThisDrone += addTo.size() - before;
                    System.out.print(bestCand.getHomeSP().getName() +"total steps: "+ movesForThisDrone + "\n\n");


                    deliveryPath.getDronePaths().add(dronePath);
                    totalMoves += movesForThisDrone;
                    totalCost += bestCand.getDrone().getCapability().getCostInitial()
                            + bestCand.getDrone().getCapability().getCostFinal()
                            + (movesForThisDrone * bestCand.getDrone().getCapability().getCostPerMove());
                    remaining.removeAll(candidateCluster);

                }

            }
            deliveryPath.setTotalMoves(totalMoves);
            deliveryPath.setTotalCost(totalCost);

            return deliveryPath;

        }


    public List<List<MedDispatchRec>> clusterByDay(List<MedDispatchRec> requests) {
        Map<LocalDate, List<MedDispatchRec>> byDay = new HashMap<>();

        for (MedDispatchRec r : requests) {
            LocalDate date = r.getDate(); // make sure r.getDate() returns LocalDate
            byDay.computeIfAbsent(date, k -> new ArrayList<>()).add(r);
        }

        return new ArrayList<>(byDay.values());
    }

    // get the distance between all of them and if its below a certain throshold they are all together
    // if the total is too much, start with on and add the closes to it and do that till its above the threshold
    // removing them from the possiblities as i go
    // then start the next cluster with the next one in the list
    // while distance is less that threshold and there is more than one left in working array
    // add to distance with next closests
    // once threshold is broken dont add that one and put those into a cluster and add it to cluster
    // then start again with the one that broke that cluster


    public Map<String, Object> calcDeliveryPathAsGeoJson(List<MedDispatchRec> requests) {
        if (requests.isEmpty()) {
            return Map.of(
                    "type", "LineString",
                    "coordinates", Collections.emptyList()
            );
        }

        // Pick the best service point
        ServicePoint sp = findBestServicePoint(requests);
        Position start = sp.getLocation();

        List<Position> path = new ArrayList<>();
        path.add(start); // start at service point

        List<MedDispatchRec> remaining = new ArrayList<>(requests);
        Position current = start;

        // Greedy delivery order
        while (!remaining.isEmpty()) {
            MedDispatchRec next = getClosest(remaining, current);
            List<Position> segment = myRouter.findPath(current, next.getDelivery(), myDataService.getRestrictedAreas());
            path.addAll(segment);

            // Hover at delivery (duplicate last position)
            if (!segment.isEmpty()) path.add(segment.get(segment.size() - 1));

            current = next.getDelivery();
            remaining.remove(next);
        }

        // Return to service point
        List<Position> backHome = myRouter.findPath(current, start, myDataService.getRestrictedAreas());
        path.addAll(backHome);

        // Convert to GeoJSON coordinates [lng, lat]
        List<List<Double>> coords = path.stream()
                .map(p -> List.of(p.getLng(), p.getLat()))
                .toList();

        return Map.of(
                "type", "LineString",
                "coordinates", coords
        );
    }
    public String buildGeoJson(DeliveryPath deliveryPath) {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"FeatureCollection\",\"features\":[");

        boolean firstFeature = true;
        int clusterIndex = 0;

        for (DronePath dronePath : deliveryPath.getDronePaths()) {

            // Each cluster/dronePath gets its own LineString
            List<String> lineCoords = new ArrayList<>();

            for (Delivery delivery : dronePath.getDeliveries()) {

                List<Position> path = delivery.getFlightPath();
                if (path == null || path.isEmpty()) continue;

                for (int i = 0; i < path.size(); i++) {
                    Position pos = path.get(i);

                    // Add coordinate to cluster linestring
                    lineCoords.add("[" + pos.getLng() + "," + pos.getLat() + "]");

                    // ----- HOVER DETECTION (duplicate coordinate) -----
                    if (i > 0) {
                        Position prev = path.get(i - 1);
                        if (prev.getLat() == pos.getLat() &&
                                prev.getLng() == pos.getLng()) {

                            if (!firstFeature) sb.append(",");
                            firstFeature = false;

                            // Add hover marker feature
                            sb.append("{")
                                    .append("\"type\":\"Feature\",")
                                    .append("\"properties\":{")
                                    .append("\"type\":\"hover\",")
                                    .append("\"droneId\":").append(dronePath.getDroneId()).append(",")
                                    .append("\"deliveryId\":").append(delivery.getDeliveryId())
                                    .append("},")
                                    .append("\"geometry\":{")
                                    .append("\"type\":\"Point\",")
                                    .append("\"coordinates\":[").append(pos.getLng()).append(",").append(pos.getLat()).append("]")
                                    .append("}")
                                    .append("}");
                        }
                    }
                }
            }

            // ----- CLUSTER LINESTRING (ONE PER DRONE PATH) -----
            if (!firstFeature) sb.append(",");
            firstFeature = false;

            sb.append("{")
                    .append("\"type\":\"Feature\",")
                    .append("\"properties\":{")
                    .append("\"type\":\"flight\",")
                    .append("\"droneId\":").append(dronePath.getDroneId()).append(",")
                    .append("\"clusterIndex\":").append(clusterIndex++)
                    .append("},")
                    .append("\"geometry\":{")
                    .append("\"type\":\"LineString\",")
                    .append("\"coordinates\":[").append(String.join(",", lineCoords)).append("]")
                    .append("}")
                    .append("}");
        }

        sb.append("]}");
        return sb.toString();
    }




}


