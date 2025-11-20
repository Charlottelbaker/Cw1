package uk.ac.ed.acp.cw2.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.data.DTO.*;
import uk.ac.ed.acp.cw2.exception.DroneNotFoundException;
import uk.ac.ed.acp.cw2.service.BasicService;
import uk.ac.ed.acp.cw2.service.DataService;
import uk.ac.ed.acp.cw2.service.DroneService;


import java.net.URL;
import java.util.List;

/**
 * Controller class that handles various HTTP endpoints for the application.
 * Provides functionality for serving the index page, retrieving a static UUID,
 * and managing key-value pairs through POST requests.
 */
@RestController()
@RequestMapping("/api/v1")
public class ServiceController {



    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Value("${ilp.service.url}")
    public URL serviceUrl;


    @GetMapping("/")
    public String index() {
        return "<html><body>" +
                "<h1>Welcome from ILP</h1>" +
                "<h4>ILP-REST-Service-URL:</h4> <a href=\"" + serviceUrl + "\" target=\"_blank\"> " + serviceUrl+ " </a>" +
                "</body></html>";
    }

    @GetMapping("/uid")
    public String uid() {
        return "s2400493";
    }

    @GetMapping("/demo")
    public String demo() {
        return "demo";
    }

//    @PostMapping("/addStudent")
//    public String addStudent(@RequestBody String studentName) {
//        return "Hello, "+studentName;
//    }

    private final BasicService myService;
    private final DataService myDataService;
    private final DroneService myDroneService;

    public ServiceController(BasicService Service,  DataService DataService, DroneService DroneService) {
        this.myService = Service;
        this.myDataService = DataService;
        this.myDroneService = DroneService;
        }

    @PostMapping("/distanceTo")
    public double distanceTo(@Valid @RequestBody DistanceRequest request) {
        Position pos1 = request.getPosition1();
        Position pos2 = request.getPosition2();
        return myService.calculateDistance(pos1, pos2);

    }

    //return true if the two positions are close (< 0.00015), otherwise false.
    @PostMapping("/isCloseTo")
    public Boolean isCloseTo (@Valid @RequestBody DistanceRequest request) {
        Position pos1 = request.getPosition1();
        Position pos2 = request.getPosition2();
        return myService.isCloseTo(pos1, pos2);
    }

    @PostMapping("/nextPosition")
    public Position nextPosition (@Valid @RequestBody NextPositionRequest request) {
        Position pos = request.getPosition();
        double angle = request.getAngle();

        return myService.nextPosition(pos, angle);
    }

    @PostMapping("/isInRegion")
    public Boolean  isInRegion  (@Valid @RequestBody RegionRequest request) {
        Position pos = request.getPosition();
        Region region = request.getRegion();
        return myService.isInRegion(region, pos);
    }

    @GetMapping("/droneswithcooling/{state}")
    public List<String> dronesWithCooling(@PathVariable boolean state) {
        return myDroneService.findDronesWithCooling(state);
    }

    @GetMapping("/droneswithheating/{state}")
    public List<String> dronesWithHeating(@PathVariable boolean state) {
        return myDroneService.findDronesWithHeating(state);
    }


    @GetMapping("/dronedetails/{id}")
    public Drone droneDetails(@PathVariable String id) {
        Drone result = myDroneService.findDroneDetails(id);
        if (result == null) {
            throw new DroneNotFoundException();
        }
        return result;
    }
///api/v1/queryAsPath/capacity/8
    @GetMapping("/queryAsPath/{attribute}/{value}")
    public List<String> queryAsPath(@PathVariable String attribute, @PathVariable String value) {
        return myDroneService.findDroneFromQuery(attribute, value);

    }

    @PostMapping("query")
    public List<String> query(@Valid @RequestBody List<QueryRequest> queries) {
        return myDroneService.queryDrones(queries);
    }

    @PostMapping("queryAvailableDrones")
    public List<String> queryAvailableDrones(@Valid @RequestBody List<MedDispatchRec> requests) {
        List<DroneCandidate> list = myDroneService.getDronesForMedRec(requests);
        List<Drone> drones = list.stream().map(DroneCandidate::getDrone).toList();
        return drones.stream().map(Drone::getId).toList();
    }
    // gets a few medrecs and must find at least one drone capable of doing all of them
    // first loop through medrecs and total 'weight'
    // then for each service point calc total moves
    // choose best service station and check get all drones, add to array
    // loop through each medrec
        // check availbility and requiremtns, remove from array if they dont match

//    @PostMapping("calcDeliveryPath")
//    public List<String> calcDeliveryPath(@Valid @RequestBody List<MedDispatchRec> requests) {
//        return myDroneService.getDronesForDeliveries(requests);
//    }
    // gets a bunch of reqests, trys to find the most efficient way to deleiver them
    // maybe before doing anything loop through them and group them into near by ones
    // like get a ratio of nearness to group them
    // and if they are all suffieceintly near they are all grouped
    // also grouped nearness by day, like if they are difrent days obvs not near. do that first
    // once i have groups - run avaible drones on them, see if  ther is a drone which can do it
    // then once there is calc the path using a greedy algorithm to choose the next drone and u A* for the route
    // need to consider no fly zones


}
