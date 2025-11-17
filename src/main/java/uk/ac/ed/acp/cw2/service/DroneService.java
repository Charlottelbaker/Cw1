package uk.ac.ed.acp.cw2.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.data.DTO.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class DroneService {

    private final DataService myDataService;

    public DroneService(DataService dataService) {
        this.myDataService = dataService;
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









}
