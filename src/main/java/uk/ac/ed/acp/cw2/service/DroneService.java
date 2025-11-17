package uk.ac.ed.acp.cw2.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.data.DTO.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DroneService {

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
            case "=": return droneValue == v;
            case "<": return droneValue < v;
            case ">": return droneValue > v;
            case "<=": return droneValue <= v;
            case ">=": return droneValue >= v;
            default: return false;
        }
    }

    private String doubleToComparableString(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int)value);
        } else {
            return String.valueOf(value);
        }
    }

    public List<String> findDroneFromQuery(String attribute, String value) {
        Drone[] drones = myDataService.getAllDrones();
        List<String> result = new ArrayList<>();
        for (Drone d : drones){
            if (droneMatches(d, attribute, "=", value)){
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
        for (DronesForServicePoint d : allDronesForServicePoints){
            if (d.getServicePointId() == id){
                return d;
            }
        }
        return null;
    }

    public List<String> getDronesForMedRec(List<MedDispatchRec> requests) {
        DronesForServicePoint[] allDronesForServicePoints = myDataService.getDronesForServicePoints();
        Drone[] drones = myDataService.getAllDrones();

        double TotCapacityRequired = 0;
        for (MedDispatchRec m : requests) {
            TotCapacityRequired += m.getRequirements().getCapacity();
        }
        ServicePoint[] servicePoints = myDataService.getServicePoints();
        ServicePoint bestPoint = null;
        double bestDistanceTotal = Double.MAX_VALUE;

        // this bit gets the best sp for these medrecs
        for (ServicePoint sp : servicePoints) {
            List<MedDispatchRec> remaining = new ArrayList<>(requests);
            Position prevPoint = sp.getPosition();
            double distanceTot = 0;
            while (!remaining.isEmpty()) {
                MedDispatchRec closest = null;
                double minDist = Double.MAX_VALUE;
                for (MedDispatchRec m : remaining) {
                    double temp = myService.calculateDistance(m.getDelivery(), prevPoint);
                    if (temp < minDist) {
                        minDist = temp;
                        closest = m;
                    }
                }
                distanceTot += minDist;
                prevPoint = closest.getDelivery();
                remaining.remove(closest);
            }

            if (distanceTot < bestDistanceTotal) {
                bestDistanceTotal = distanceTot;
                bestPoint = sp;
            }

        }
        List<AvailableDrone> acceptableDrones = getDronesAtServicePoint(bestPoint.getId(), allDronesForServicePoints).getDrones();
        for (MedDispatchRec m : requests) {
            for (AvailableDrone ad : acceptableDrones) {
                Drone d = findDroneDetails(String.valueOf(ad.getId()));
                if (!isDroneSuitableForRequest(m, d, ad, TotCapacityRequired, bestDistanceTotal/0.00015)){
                    acceptableDrones.remove(ad);
                }
            }
        }
        List<String> result = new ArrayList<>();
        for (AvailableDrone ad : acceptableDrones) {
            result.add(String.valueOf(ad.getId()));
        }
        return (result);

    }

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
    }

    // gets a few medrecs and must find at least one drone capable of doing all of them
    // first loop through medrecs and total 'weight'
    // then for each service point TO calc total moves
        // loop through array
            // get distance from prev point ( initaly service ) for each remaining request
            // if distance min replace
        // remove drone with min distance from array
        // add that to distancetot
    // choose best service station and check get all drones, add to array
    // loop through each medrec
    // check availbility and requiremtns, remove from array if they dont match






