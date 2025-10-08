package uk.ac.ed.acp.cw2.controller;

import lombok.RequiredArgsConstructor;
import uk.ac.ed.acp.cw2.DTO.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.data.RuntimeEnvironment;


import java.net.URL;
import java.time.Instant;

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

    private double calculateDistance(Position pos1, Position pos2) {
        double dx = pos1.getLat() - pos2.getLat();
        double dy = pos1.getLng() - pos2.getLng();
        return Math.sqrt(dx * dx + dy * dy);
    }
    @PostMapping("/distanceTo")
    public ResponseEntity<?> distanceTo(@RequestBody DistanceRequest request) {
    //request.getPosition1() and request.getPosition2() now hold the data
        Position pos1 = request.getPosition1();
        Position pos2 = request.getPosition2();
        if (!pos1.isValid()){ResponseEntity.badRequest().body("Invalid Position data");}
        if (!pos2.isValid()){ResponseEntity.badRequest().body("Invalid Position data");}


        double distance = calculateDistance(pos1, pos2);

        {
            return ResponseEntity.ok(distance);
        }

    }

    //return true if the two positions are close (< 0.00015), otherwise false.
    @PostMapping("/isCloseTo")
    public ResponseEntity<?> isCloseTo (@RequestBody DistanceRequest request) {
        Position pos1 = request.getPosition1();
        Position pos2 = request.getPosition2();
        if (!pos1.isValid()){ResponseEntity.badRequest().body("Invalid Position data");}
        if (!pos2.isValid()){ResponseEntity.badRequest().body("Invalid Position data");}
        double distance = calculateDistance(pos1, pos2);

        return ResponseEntity.ok(distance < 0.00015);
    }

    @PostMapping("/nextPosition")
    public ResponseEntity<?> nextPosition (@RequestBody NextPositionRequest request) {
        Position pos = request.getPosition();
        if (!pos.isValid()){ResponseEntity.badRequest().body("Invalid Position data");}
        double angle = request.getAngle();
        if (angle < 0 || angle > 360){ResponseEntity.badRequest().body("Invalid angle");}

        double step = 0.00015;
        double thetaRad = Math.toRadians(angle);

        double nextLongitude = pos.getLng() + step * Math.cos(thetaRad);
        double nextLatitude  = pos.getLat()  + step * Math.sin(thetaRad);

        Position nextpos = new Position();
        nextpos.setLat(nextLatitude);
        nextpos.setLng(nextLongitude);

        return (ResponseEntity.ok(nextpos));
    }

    @PostMapping("/isInRegion ")
    public ResponseEntity<?>  isInRegion  (@RequestBody RegionRequest request) {
        Position pos = request.getPosition();
        Region region = request.getRegion();
        if (!region.isValid()) {
            return ResponseEntity.badRequest().body("Invalid region data");}
        return ResponseEntity.ok(region.isIn(pos));
    }




}
