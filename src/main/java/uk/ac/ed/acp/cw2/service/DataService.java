package uk.ac.ed.acp.cw2.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.acp.cw2.data.DTO.*;

@Service
public class DataService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public DataService(String ilpEndpoint) {
        this.baseUrl = ilpEndpoint;
    }

    public Drone[] getAllDrones() {
        return restTemplate.getForObject(baseUrl + "/drones", Drone[].class);
    }

    public DroneForServicePoint[] getDronesForServicePoints() {
        return restTemplate.getForObject(baseUrl + "/drones-for-service-points",
                DroneForServicePoint[].class);
    }

    public RestrictedArea[] getRestrictedAreas() {
        return restTemplate.getForObject(baseUrl + "/restricted-areas",
                RestrictedArea[].class);
    }

    public ServicePoint[] getServicePoints() {
        return restTemplate.getForObject(baseUrl + "/service-points",
                ServicePoint[].class);
    }
}
