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

//    private String doubleToComparableString(double value) {
//        if (value == Math.floor(value)) {
//            return String.valueOf((int) value);
//        } else {
//            return String.valueOf(value);
//        }
//    }

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

//    public List<String> getDronesForMedRec2(List<MedDispatchRec> requests) {
//        DronesForServicePoint[] allDronesForServicePoints = myDataService.getDronesForServicePoints();
//        Drone[] drones = myDataService.getAllDrones();
//
//        double TotCapacityRequired = 0;
//        for (MedDispatchRec m : requests) {
//            TotCapacityRequired += m.getRequirements().getCapacity();
//        }
//        ServicePoint[] servicePoints = myDataService.getServicePoints();
//        ServicePoint bestPoint = null;
//        double bestDistanceTotal = Double.MAX_VALUE;
//
//        // this bit gets the best sp for these medrecs
//        for (ServicePoint sp : servicePoints) {
//            List<MedDispatchRec> remaining = new ArrayList<>(requests);
//            Position prevPoint = sp.getLocation();
//            double distanceTot = 0;
//            while (!remaining.isEmpty()) {
//                MedDispatchRec closest = null;
//                double minDist = Double.MAX_VALUE;
//                for (MedDispatchRec m : remaining) {
//                    double temp = myService.calculateDistance(m.getDelivery(), prevPoint);
//                    if (temp < minDist) {
//                        minDist = temp;
//                        closest = m;
//                    }
//                }
//                distanceTot += minDist;
//                prevPoint = closest.getDelivery();
//                remaining.remove(closest);
//            }
//
//            if (distanceTot < bestDistanceTotal) {
//                bestDistanceTotal = distanceTot;
//                bestPoint = sp;
//            }
//
//        }
//        List<AvailableDrone> acceptableDrones = getDronesAtServicePoint(bestPoint.getId(), allDronesForServicePoints).getDrones();
//        for (MedDispatchRec m : requests) {
//            Iterator<AvailableDrone> it = acceptableDrones.iterator();
//            while (it.hasNext()) {
//                AvailableDrone ad = it.next();
//                Drone d = findDroneDetails(String.valueOf(ad.getId()));
//
//                if (!isDroneSuitableForRequest(m, d, ad, TotCapacityRequired, bestDistanceTotal / 0.00015)) {
//                    System.out.println("Rejected drone " + d.getId() + " because:");
//                    System.out.println("  capacity=" + d.getCapability().getCapacity());
//                    System.out.println("  requiredCapacity=" + TotCapacityRequired);
//                    System.out.println("  cooling=" + d.getCapability().isCooling() +
//                            " required=" + m.getRequirements().isCooling());
//                    System.out.println("  heating=" + d.getCapability().isHeating() +
//                            " required=" + m.getRequirements().isHeating());
//                    System.out.println("  maxCost=" + m.getRequirements().getMaxCost());
//                    System.out.println("  droneCost=" + calculateCost(d, bestDistanceTotal / 0.00015));
//                    System.out.println("  available? " );
//                    it.remove();
//                }
//
//            }
//        }
//
//        List<String> result = new ArrayList<>();
//        for (AvailableDrone ad : acceptableDrones) {
//            result.add(String.valueOf(ad.getId()));
//        }
//        return (result);
//
//    }

    private boolean isDroneSuitableForRequest(MedDispatchRec request, Drone drone,
                                              AvailableDrone availableDrone, double totalCapacity,
                                              double movesOnPath) {
        Requirements req = request.getRequirements();

        if (drone.getCapability().getCapacity() < totalCapacity) {
            return false;
        }
        if (drone.getCapability().getMaxMoves() < movesOnPath) { // MIGHT BE AN ISSUE
            return false;
        }
        if (req.isCooling() && !drone.getCapability().isCooling()) {
            return false;
        }
        if (req.isHeating() && !drone.getCapability().isHeating()) {
            return false;
        }

        if (request.getDate() != null && request.getTime() != null) {
            DayOfWeek requestDay = request.getDate().getDayOfWeek();
            LocalTime requestTime = request.getTime();

            boolean available = false;

            for (Availability a : availableDrone.getAvailability()) {
                if (a.getDayOfWeek() == requestDay) {
                    if (!requestTime.isBefore(a.getFrom())
                            && !requestTime.isAfter(a.getUntil())) {
                        available = true;
                        break;
                    }
                }
            }

            if (!available) {
                return false;
            }
        }

        Double maxCost = req.getMaxCost();

        if (maxCost != null) {
            double totalCost = drone.getCapability().getCostInitial()
                    + drone.getCapability().getCostFinal()
                    + (movesOnPath * drone.getCapability().getCostPerMove());

            if (totalCost > maxCost) {
                return false;
            }
        }
        return true;
    }


    private double calculateCost(Drone drone, double movesOnPath) {
        double totalCost = drone.getCapability().getCostInitial()
                + drone.getCapability().getCostFinal()
                + (movesOnPath * drone.getCapability().getCostPerMove());
        return totalCost;
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
        return result;
    }



//    public List<String> getDronesForMedRec(List<MedDispatchRec> requests) {
//
//        ServicePoint bestPoint = findBestServicePoint(requests);
//        if (bestPoint == null) {
//            return Collections.emptyList();
//        }
//
//        List<AvailableDrone> dronesAtSP = getDronesAtServicePoint(bestPoint.getId(),
//                myDataService.getDronesForServicePoints()).getDrones();
//
//        Iterator<AvailableDrone> it = dronesAtSP.iterator();
//        while (it.hasNext()) {
//            AvailableDrone ad = it.next();
//            Drone drone = findDroneDetails(String.valueOf(ad.getId()));
//
//            if (!canDroneHandleAllRequests(drone, ad, requests, bestPoint)) {
//                it.remove();
//            }
//        }
//
//        List<String> result = new ArrayList<>();
//        for (AvailableDrone ad : dronesAtSP) {
//            result.add(String.valueOf(ad.getId()));
//        }
//        return result;
//        }



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



    private boolean canDroneHandleAllRequests(Drone drone, AvailableDrone availableDrone,
                                              List<MedDispatchRec> requests, ServicePoint sp) {

        if (requests == null || requests.isEmpty()) {
            return true; // nothing to do
        }

        // --- ALL-IN-ONE TRIP: build greedy route, sum moves and capacity ---
        double totalMovesAllInOne = 0.0;
        double totalCapacityAllInOne = 0.0;

        List<MedDispatchRec> copy = new ArrayList<>(requests);
        Position prev = sp.getLocation();

        while (!copy.isEmpty()) {
            MedDispatchRec closest = getClosest(copy, prev);
            totalMovesAllInOne += calculateMoves(prev, closest.getDelivery());
            prev = closest.getDelivery();
            copy.remove(closest);
        }
        // include return leg to service point (so it's a complete trip)
        totalMovesAllInOne += calculateMoves(prev, sp.getLocation());

        // sum capacities for all requests
        for (MedDispatchRec r : requests) {
            totalCapacityAllInOne += r.getRequirements().getCapacity();
        }

        // Check: ALL requests must be OK when considering the full-trip parameters
        boolean allInOneOk = true;
        for (MedDispatchRec r : requests) {
            // use totalMovesAllInOne, totalCapacityAllInOne and total request count
            if (!isDroneSuitableForSingleReq(r, drone, availableDrone,
                    totalMovesAllInOne,
                    requests.size(),
                    totalCapacityAllInOne)) {
                allInOneOk = false;
                break;
            }
        }

        // --- MULTI-TRIP: each request is a separate trip from/to service point ---
        boolean multiTripOk = true;
        for (MedDispatchRec r : requests) {
            // moves for a single-request trip: SP -> req -> SP
            double movesSingleTrip = calculateMoves(sp.getLocation(), r.getDelivery())
                    + calculateMoves(r.getDelivery(), sp.getLocation());
            double capacitySingle = r.getRequirements().getCapacity();

            // For single-trip checks, pass count=1 and capacity=capacitySingle
            if (!isDroneSuitableForSingleReq(r, drone, availableDrone,
                    movesSingleTrip,
                    1,
                    capacitySingle)) {
                multiTripOk = false;
                break;
            }
        }

        // Return true if at least one of the two options is entirely feasible
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
        List<Cluster> finalClusters =  new ArrayList<>();


        for (List<MedDispatchRec> dailyCluster : clustersByDay) {
            int threshold = 100;

            System.out.println("daily cluster for loop");

            List<List<MedDispatchRec>> problems = new ArrayList<>();
            problems.add(dailyCluster);

            while (!problems.isEmpty() && threshold >= 0) {
                System.out.println("problems isnt empty");

                List<List<MedDispatchRec>> clusters = new ArrayList<>();
                for (List<MedDispatchRec> group : problems) {
                    clusters.addAll(clusterByArea(group, threshold));
                }

                List<List<MedDispatchRec>> nextProblems = new ArrayList<>();
                System.out.println("hello");


                // Check if each new cluster has a usable drone
                for (List<MedDispatchRec> c : clusters) {


                    List<DroneCandidate> drones = getDronesForMedRec(c);
                    System.out.println(drones.toString());

                    if (!drones.isEmpty()) {
                        Cluster cluster = new Cluster(c, drones.getFirst().getHomeSP().getLocation(), drones.getFirst().getDrone());
                        finalClusters.add(cluster);
                    } else {
                        nextProblems.add(c);
                    }
                }

                problems = nextProblems;
                threshold--;
            }
        }
    //    h = sqrt ( (current_cell.x – goal.x)2 + (current_cell.y – goal.y)2 )
        DeliveryPath deliveryPath = new  DeliveryPath();
        deliveryPath.setDronePaths(new ArrayList<>());

        double totalMoves = 0;
        double totalCost = 0;
        System.out.println("there are " + finalClusters.size() + " clusters");
        for(Cluster cluster : finalClusters){
            System.out.println("starting a path for this cluster");
            // for each cluster start at start point and greedy find closest drop off
            // then run A* with that
            // then go from that one to the next closest point ... until cluster is empty
            Position start = cluster.getServicePoint();
            Cluster clusterToDo = new Cluster(new ArrayList<>(cluster.getRequests()),
                                                cluster.getServicePoint(), cluster.getDrone());
            DronePath dronePath = new DronePath();
            dronePath.setDroneId(Integer.parseInt(cluster.getDrone().getId()));
            dronePath.setDeliveries(new ArrayList<>());
            double moves = 0;

            while(!clusterToDo.getRequests().isEmpty()){
                Delivery delivery = new Delivery();
                MedDispatchRec goal = getClosest(clusterToDo.getRequests(), clusterToDo.getServicePoint());
                delivery.setDeliveryId(goal.getId());
                System.out.println("path find for");
                List<Position> path = myRouter.findPath(start, goal.getDelivery(), myDataService.getRestrictedAreas());
                moves += path.size();
                if (!path.isEmpty()) {
                    Position hover = path.getLast();
                    path.add(hover);
                }
                delivery.setFlightPath(path);
                start = goal.getDelivery();
                clusterToDo.getRequests().remove(goal);
                dronePath.getDeliveries().add(delivery);
            }
            List<Position> deliveryToAddTo = dronePath.getDeliveries().getLast().getFlightPath();
            Position goal = clusterToDo.getServicePoint();
            List<Position> path = myRouter.findPath(start, goal, myDataService.getRestrictedAreas());
            moves += path.size();
            deliveryToAddTo.addAll(path);
            dronePath.getDeliveries().getLast().setFlightPath(deliveryToAddTo);
            deliveryPath.getDronePaths().add(dronePath);
            totalMoves += moves;
            totalCost += cluster.getDrone().getCapability().getCostInitial()
                    + cluster.getDrone().getCapability().getCostFinal()
                    + (moves*cluster.getDrone().getCapability().getCostPerMove());
        }
        deliveryPath.setTotalCost(totalCost);
        deliveryPath.setTotalMoves(totalMoves);


       // we need to return “totalCost”: 1234.44, “totalMoves”: 12111, “dronePaths”:
        // each drone path has “droneId”: 4, “deliveries": [ { “deliveryId”: 123, “flightPath”: [
return deliveryPath;
    }

//    public DeliveryPath calcDeliveryPath(List<MedDispatchRec> requests) {
//        // Group by day (existing helper)
//        List<List<MedDispatchRec>> clustersByDay = clusterByDay(requests);
//
//        DeliveryPath deliveryPath = new DeliveryPath();
//        deliveryPath.setDronePaths(new ArrayList<>());
//
//        double totalMoves = 0;
//        double totalCost = 0;
//
//        for (List<MedDispatchRec> dailyRequests : clustersByDay) {
//
//            // Work on a mutable copy of today's requests
//            List<MedDispatchRec> remaining = new ArrayList<>(dailyRequests);
//
//            // While there are still requests to assign
//            while (!remaining.isEmpty()) {
//
//                // Seed start: pick the request with the earliest time (or first in list)
//                // You can change selection strategy (largest-demand first) if desired
//                MedDispatchRec seed = remaining.get(0);
//
//                // Greedily grow cluster starting from seed
//                List<MedDispatchRec> candidateCluster = new ArrayList<>();
//                candidateCluster.add(seed);
//
//                boolean addedAny;
//                do {
//                    addedAny = false;
//                    // Try to add each remaining request (except those already in candidateCluster)
//                    for (MedDispatchRec r : new ArrayList<>(remaining)) {
//                        if (candidateCluster.contains(r)) continue;
//
//                        List<MedDispatchRec> trial = new ArrayList<>(candidateCluster);
//                        trial.add(r);
//
//                        // Use your existing helper to see if any drone can do this set
//                        List<DroneCandidate> drones = getDronesForMedRec(trial);
//
//                        if (!drones.isEmpty()) {
//                            // Accept r into the cluster (the trial is feasible)
//                            candidateCluster.add(r);
//                            addedAny = true;
//                        }
//                        // If no drone can handle the trial cluster, skip r
//                    }
//                    // repeat until no additions in a full pass
//                } while (addedAny);
//
//                // At this point candidateCluster is maximal (greedily)
//                // Confirm there exists at least one drone for this cluster
//                List<DroneCandidate> clusterDrones = getDronesForMedRec(candidateCluster);
//
//                if (clusterDrones.isEmpty()) {
//                    // Unlikely because we grew the cluster only when trials were feasible,
//                    // but if happens (race or helper oddness) fall back to singletons
//                    // Try to salvage by assigning the seed alone (or skip)
//                    List<MedDispatchRec> one = Collections.singletonList(seed);
//                    List<DroneCandidate> singleDrones = getDronesForMedRec(one);
//                    if (singleDrones.isEmpty()) {
//                        // cannot deliver this request by any drone; log and remove to avoid infinite loop
//                        System.err.println("No drone can serve single request id=" + seed.getId() + "; skipping");
//                        remaining.remove(seed);
//                        continue;
//                    } else {
//                        candidateCluster = new ArrayList<>(one);
//                        clusterDrones = singleDrones;
//                    }
//                }
//
//                // Choose the best drone candidate for the cluster.
//                // Strategy: pick candidate with smallest estimated total route distance starting at its home SP.
//                DroneCandidate bestCand = null;
//                double bestScore = Double.POSITIVE_INFINITY;
//                for (DroneCandidate cand : clusterDrones) {
//                    ServicePoint home = cand.getHomeSP();
//                    if (home == null || home.getLocation() == null) continue;
//                    double dist = routeDistanceStartingAt(home.getLocation(), candidateCluster);
//                    if (dist < bestScore) {
//                        bestScore = dist;
//                        bestCand = cand;
//                    }
//                }
//                if (bestCand == null) {
//                    // fallback to first
//                    bestCand = clusterDrones.get(0);
//                }
//
//                // Build Cluster object using your Cluster ctor (requests, servicePoint, drone)
//                Cluster assignedCluster = new Cluster(candidateCluster, bestCand.getHomeSP().getLocation(), bestCand.getDrone());
//                // Create DronePath DTO
//                DronePath dronePath = new DronePath();
//                try {
//                    dronePath.setDroneId(Integer.parseInt(bestCand.getDrone().getId()));
//                } catch (Exception ex) {
//                    // If ID parsing fails, use -1 as fallback
//                    dronePath.setDroneId(-1);
//                }
//                dronePath.setDeliveries(new ArrayList<>());
//
//                // Now create route for this cluster: start at service point, visit greedily nearest neighbour
//                Position currentPos = assignedCluster.getServicePoint();
//                List<MedDispatchRec> toDeliver = new ArrayList<>(assignedCluster.getRequests());
//                double movesForThisDrone = 0;
//
//                while (!toDeliver.isEmpty()) {
//                    MedDispatchRec next = getClosest(toDeliver, currentPos);
//                    if (next == null) break; // defensive
//
//                    Delivery deliveryDto = new Delivery();
//                    deliveryDto.setDeliveryId(next.getId());
//
//                    // Call your router (keeps same signature you used before)
//                    List<Position> flightPath = myRouter.findPath(currentPos, next.getDelivery(), myDataService.getRestrictedAreas());
//
//                    // Add hover (two identical entries) to signal delivery
//                    if (!flightPath.isEmpty()) {
//                        Position hover = flightPath.get(flightPath.size() - 1);
//                        flightPath.add(new Position(hover.getLat(), hover.getLng()));
//                    } else {
//                        // If router returned empty path (shouldn't), still create hover at target
//                        Position hover = new Position();
//                        hover.setLat(next.getDelivery().getLat());
//                        hover.setLng(next.getDelivery().getLng());
//                        flightPath.add(hover);
//                        flightPath.add(new Position(hover.getLat(), hover.getLng()));
//                    }
//
//                    deliveryDto.setFlightPath(flightPath);
//                    dronePath.getDeliveries().add(deliveryDto);
//
//                    movesForThisDrone += flightPath.size();
//
//                    // advance currentPos and remove delivered rec
//                    currentPos = next.getDelivery();
//                    toDeliver.remove(next);
//                }
//
//                // After finishing deliveries, return to service point
//                Delivery returnDelivery = new Delivery();
//                returnDelivery.setDeliveryId(0); // sentinel for return-home
//                List<Position> returnPath = myRouter.findPath(currentPos, assignedCluster.getServicePoint(), myDataService.getRestrictedAreas());
//                // ensure hover at service point to mark termination
//                if (!returnPath.isEmpty()) {
//                    Position hover = returnPath.get(returnPath.size() - 1);
//                    returnPath.add(new Position(hover.getLat(), hover.getLng()));
//                } else {
//                    Position hover = new Position();
//                    hover.setLat(assignedCluster.getServicePoint().getLat());
//                    hover.setLng(assignedCluster.getServicePoint().getLng());
//                    returnPath.add(hover);
//                    returnPath.add(new Position(hover.getLat(), hover.getLng()));
//                }
//                returnDelivery.setFlightPath(returnPath);
//                dronePath.getDeliveries().add(returnDelivery);
//
//                movesForThisDrone += returnPath.size();
//
//                // Add drone path to overall result
//                deliveryPath.getDronePaths().add(dronePath);
//
//                // Update totals
//                totalMoves += movesForThisDrone;
//                totalCost += bestCand.getDrone().getCapability().getCostInitial()
//                        + bestCand.getDrone().getCapability().getCostFinal()
//                        + (movesForThisDrone * bestCand.getDrone().getCapability().getCostPerMove());
//
//                // Remove assigned requests from the remaining pool
//                remaining.removeAll(candidateCluster);
//            }
//        }
//
//        deliveryPath.setTotalMoves(totalMoves);
//        deliveryPath.setTotalCost(totalCost);
//
//        return deliveryPath;
//    }



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
    private List<List<MedDispatchRec>> clusterByArea(List<MedDispatchRec> requests, int threshold) {
        List<List<MedDispatchRec>> clusters = new ArrayList<>();
        if (requests.isEmpty()) return clusters;


        // Check if the entire set is one cluster:
        if (routeDistanceStartingAt(
                requests.getFirst().getDelivery(),
                requests.subList(1, requests.size())) <= threshold) {

            clusters.add(new ArrayList<>(requests));
            return clusters;
        }

        // Otherwise build clusters incrementally
        List<MedDispatchRec> copy = new ArrayList<>(requests);

        while (!copy.isEmpty()) {

            // Start a new cluster
            List<MedDispatchRec> cluster = new ArrayList<>();

            MedDispatchRec current = copy.remove(0);
            cluster.add(current);

            while (!copy.isEmpty()) {

                MedDispatchRec next = getClosest(copy, current.getDelivery());
                double distance = myService.calculateDistance(
                        current.getDelivery(),
                        next.getDelivery()
                );

                if (distance > threshold) {
                    // Too far
                    break;
                }

                copy.remove(next);
                cluster.add(next);

                current = next;
            }

            clusters.add(cluster);
        }

        return clusters;
    }


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


