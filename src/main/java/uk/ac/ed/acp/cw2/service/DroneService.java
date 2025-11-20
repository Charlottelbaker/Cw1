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

    public DroneService(BasicService Service, DataService dataService) {
        this.myDataService = dataService;
        this.myService = Service;
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


    public List<String> getDronesForMedRec(List<MedDispatchRec> requests) {

        ServicePoint bestPoint = findBestServicePoint(requests);
        if (bestPoint == null) {
            return Collections.emptyList();
        }

        List<AvailableDrone> dronesAtSP = getDronesAtServicePoint(bestPoint.getId(),
                myDataService.getDronesForServicePoints()).getDrones();

        Iterator<AvailableDrone> it = dronesAtSP.iterator();
        while (it.hasNext()) {
            AvailableDrone ad = it.next();
            Drone drone = findDroneDetails(String.valueOf(ad.getId()));

            if (!canDroneHandleAllRequests(drone, ad, requests, bestPoint)) {
                it.remove();
            }
        }

        List<String> result = new ArrayList<>();
        for (AvailableDrone ad : dronesAtSP) {
            result.add(String.valueOf(ad.getId()));
        }
        return result;
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

    private MedDispatchRec getClosest(List<MedDispatchRec> list, MedDispatchRec current) {
        MedDispatchRec closest = null;
        double min = Double.MAX_VALUE;

        for (MedDispatchRec r : list) {
            double d = myService.calculateDistance(current.getDelivery(), r.getDelivery());
            if (d < min) {
                min = d;
                closest = r;
            }
        }

        return closest;
    }



    private boolean canDroneHandleAllRequests(Drone drone,
                                                  AvailableDrone availableDrone,
                                                  List<MedDispatchRec> requests,
                                                  ServicePoint sp) {

            for (MedDispatchRec req : requests) {
                double moves = calculateMoves(sp.getLocation(), req.getDelivery());
                if (!isDroneSuitableForSingleReq(req, drone, availableDrone, moves, requests.size())) {
                    return false;
                }
            }
            return true;
        }


        private boolean isDroneSuitableForSingleReq(MedDispatchRec request,
                                                    Drone drone,
                                                    AvailableDrone availableDrone,
                                                    double moves,
                                                    int requestCount) {

            Requirements req = request.getRequirements();

            // Capacity — PER request, not total
            if (drone.getCapability().getCapacity() < req.getCapacity()) {
                System.out.println("capacity");
                return false;
            }

            // Moves
            if (drone.getCapability().getMaxMoves() < moves) {
                System.out.println("moves");
                return false;
            }

            // Cooling/heating
            if (req.isCooling() && !drone.getCapability().isCooling()) {
                System.out.println("cool");
                return false;
            }
            if (req.isHeating() && !drone.getCapability().isHeating()) {
                System.out.println("heat");
                return false;
            }

            // Availability
            if (!isDroneAvailableAt(drone, availableDrone, request)) {
                System.out.println("available");
                return false;
            }

            // Cost
            if (!withinCostLimit(drone, req, moves, requestCount)) {
                System.out.println("coat");
                return false;
            }

            return true;
        }


        private double calculateDistance(Position p1, Position p2) {
            double dx = p1.getLat() - p2.getLat();
            double dy = p1.getLng() - p2.getLng();
            return Math.sqrt(dx * dx + dy * dy);
        }

        private double calculateMoves(Position p1, Position p2) {
            return calculateDistance(p1, p2) / STEP_SIZE;
        }

        private boolean withinCostLimit(Drone drone, Requirements req, double moves, int reqCount) {
            if (req.getMaxCost() == null) return true;
            double total = drone.getCapability().getCostInitial()
                    + drone.getCapability().getCostFinal()
                    + (moves * drone.getCapability().getCostPerMove());

            double perDispatch = total / reqCount;
            System.out.println("perDispatch: " + perDispatch);
            System.out.println("maxcost" + req.getMaxCost());
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

    public

    private List<List<MedDispatchRec>> clusterByDay(List<MedDispatchRec> requests) {
        List<List<MedDispatchRec>> clusters = new ArrayList<>();
        if (requests.isEmpty()) return clusters;

        LocalDate currentDate = requests.get(0).getDate();
        List<MedDispatchRec> currentCluster = new ArrayList<>();
        clusters.add(currentCluster);

        for (MedDispatchRec rec : requests) {
            if (rec.getDate().equals(currentDate)) {
                currentCluster.add(rec);
            } else {
                currentDate = rec.getDate();
                currentCluster = new ArrayList<>();
                clusters.add(currentCluster);
                currentCluster.add(rec);
            }
        }

        return clusters;
    }

    // get the distance between all of them and if its below a certain throshold they are all together
    // if the total is too much, start with on and add the closes to it and do that till its above the threshold
    // removing them from the possiblities as i go
    // then start the next cluster with the next one in the list
    // while distance is less that threshold and there is more than one left in working array
    // add to distance with next closests
    // once threshold is broken dont add that one and put those into a cluster and add it to cluster
    // then start again with the one that broke that cluster
    private List<List<MedDispatchRec>> clusterByArea(List<MedDispatchRec> requests) {
        List<List<MedDispatchRec>> clusters = new ArrayList<>();
        if (requests.isEmpty()) return clusters;

        double threshold = 10;

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

                MedDispatchRec next = getClosest(copy, current);
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







}


