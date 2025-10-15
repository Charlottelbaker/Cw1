package uk.ac.ed.acp.cw2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.data.DTO.*;


import java.net.URL;

@Service
public class BasicService {
    public double calculateDistance(Position pos1, Position pos2) {
        double dx = pos1.getLat() - pos2.getLat();
        double dy = pos1.getLng() - pos2.getLng();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Boolean isCloseTo (Position pos1, Position pos2){
        double distance = calculateDistance(pos1, pos2);
        return (distance < 0.00015);
    }

    public Position nextPosition (Position pos, double angle) {
        double step = 0.00015;
        double thetaRad = Math.toRadians(angle);

        double nextLongitude = pos.getLng() + step * Math.cos(thetaRad);
        double nextLatitude  = pos.getLat()  + step * Math.sin(thetaRad);

        Position nextpos = new Position();
        nextpos.setLat(nextLatitude);
        nextpos.setLng(nextLongitude);

        return (nextpos);
    }

    public Boolean  isInRegion  (Region region, Position pos) {
        return region.isIn(pos);
    }

    }

