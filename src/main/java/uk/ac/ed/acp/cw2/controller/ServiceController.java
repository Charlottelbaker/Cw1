package uk.ac.ed.acp.cw2.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.data.DTO.*;
import uk.ac.ed.acp.cw2.service.BasicService;
import uk.ac.ed.acp.cw2.service.BasicService.*;


import java.net.URL;

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

    private final BasicService CalcDistanceService;

    private final BasicService CalcCloseService;

    private final BasicService CalcNextPosService;

    private  final BasicService CalcInRegionService;

    public ServiceController(BasicService Service) {
        this.CalcDistanceService = Service;
        this.CalcCloseService = Service;
        this.CalcNextPosService = Service;
        this.CalcInRegionService = Service;

    }

    @PostMapping("/distanceTo")
    public double distanceTo(@Valid @RequestBody DistanceRequest request) {
        Position pos1 = request.getPosition1();
        Position pos2 = request.getPosition2();
        return CalcDistanceService.calculateDistance(pos1, pos2);

    }

    //return true if the two positions are close (< 0.00015), otherwise false.
    @PostMapping("/isCloseTo")
    public Boolean isCloseTo (@Valid @RequestBody DistanceRequest request) {
        Position pos1 = request.getPosition1();
        Position pos2 = request.getPosition2();
        return CalcCloseService.isCloseTo(pos1, pos2);
    }

    @PostMapping("/nextPosition")
    public Position nextPosition (@Valid @RequestBody NextPositionRequest request) {
        Position pos = request.getPosition();
        double angle = request.getAngle();

        return CalcNextPosService.nextPosition(pos, angle);
    }

    @PostMapping("/isInRegion")
    public Boolean  isInRegion  (@Valid @RequestBody RegionRequest request) {
        Position pos = request.getPosition();
        Region region = request.getRegion();
        return CalcInRegionService.isInRegion(region, pos);
    }




}
