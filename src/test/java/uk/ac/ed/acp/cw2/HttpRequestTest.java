package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpRequestTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpEntity<String> jsonEntity(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(json, headers);
    }

    @Test
    void contextLoads() {

    }

    @Test
    void mainMethodShouldRun() {
        Application.main(new String[]{});
        assertThat(true).isTrue();
    }

    @Test
    void actuatorHealthShouldReturn200() {
        String url = "http://localhost:" + port + "/actuator/health";
        var response = this.restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }


    @Test
    void uidShouldReturnStudentId() {
        String uid = this.restTemplate.getForObject("http://localhost:" + port + "/api/v1/uid", String.class);
        assertThat(uid).isEqualTo("s2400493");
    }

    @Test
    void distanceToShouldReturnCorrectDistance() {
        String url = "http://localhost:" + port + "/api/v1/distanceTo";
        String jsonRequest = """
            {
              "position1": {"lat": 0.0, "lng": 0.0},
              "position2": {"lat": 0.0001, "lng": 0.0001}
            }
        """;
        Double response = this.restTemplate.postForObject(url, jsonEntity(jsonRequest), Double.class);
        assertThat(response).isNotNull();
        assertThat(response).isGreaterThan(0.0);
    }

    @Test
    void isCloseToShouldReturnTrueWhenPositionsAreClose() {
        String url = "http://localhost:" + port + "/api/v1/isCloseTo";
        String json = """
            {
              "position1": {"lat": 0.0, "lng": 0.0},
              "position2": {"lat": 0.0001, "lng": 0.0001}
            }
        """;
        Boolean response = this.restTemplate.postForObject(url, jsonEntity(json), Boolean.class);
        assertThat(response).isTrue();
    }

    @Test
    void isCloseToShouldReturnFalseWhenPositionsAreFar() {
        String url = "http://localhost:" + port + "/api/v1/isCloseTo";
        String json = """
            {
              "position1": {"lat": 0.0, "lng": 0.0},
              "position2": {"lat": 1.0, "lng": 1.0}
            }
        """;
        Boolean response = this.restTemplate.postForObject(url, jsonEntity(json), Boolean.class);
        assertThat(response).isFalse();
    }

    @Test
    void nextPositionShouldReturnValidPosition() {
        String url = "http://localhost:" + port + "/api/v1/nextPosition";
        String json = """
          {
                  "start": {
                  "lng": -3.192473,
                  "lat": 55.946233
                  },
                  "angle": 45
                  }
        """;
        String response = this.restTemplate.postForObject(url, jsonEntity(json), String.class);
        assertThat(response).contains("lat").contains("lng");
    }

    @Test
    void isInRegionShouldReturnTrueForPointInsideRegion() {
        String url = "http://localhost:" + port + "/api/v1/isInRegion";
        String json = """
            {
              "position": {"lng": -3.189, "lat": 55.944},
              "region": {
                "name": "central",
                "vertices": [
                  {"lng": -3.192473, "lat": 55.946233},
                  {"lng": -3.192473, "lat": 55.942617},
                  {"lng": -3.184319, "lat": 55.942617},
                  {"lng": -3.184319, "lat": 55.946233},
                  {"lng": -3.192473, "lat": 55.946233}
                ]
              }
            }
        """;
        Boolean response = this.restTemplate.postForObject(url, jsonEntity(json), Boolean.class);
        assertThat(response).isTrue();
    }

    @Test
    void isInRegionShouldReturnFalseForPointOutsideRegion() {
        String url = "http://localhost:" + port + "/api/v1/isInRegion";
        String json = """
            {
              "position": {"lng": -3.2, "lat": 55.95},
              "region": {
                "name": "central",
                "vertices": [
                  {"lng": -3.192473, "lat": 55.946233},
                  {"lng": -3.192473, "lat": 55.942617},
                  {"lng": -3.184319, "lat": 55.942617},
                  {"lng": -3.184319, "lat": 55.946233},
                  {"lng": -3.192473, "lat": 55.946233}
                ]
              }
            }
        """;
        Boolean response = this.restTemplate.postForObject(url, jsonEntity(json), Boolean.class);
        assertThat(response).isFalse();
    }

    @Test
    void isInRegionShouldReturnTrueForPointOnBorder() {
        String url = "http://localhost:" + port + "/api/v1/isInRegion";
        String json = """
            {
              "position": {"lng": -3.192473, "lat": 55.944425},
              "region": {
                "name": "central",
                "vertices": [
                  {"lng": -3.192473, "lat": 55.946233},
                  {"lng": -3.192473, "lat": 55.942617},
                  {"lng": -3.184319, "lat": 55.942617},
                  {"lng": -3.184319, "lat": 55.946233},
                  {"lng": -3.192473, "lat": 55.946233}
                ]
              }
            }
        """;
        Boolean response = this.restTemplate.postForObject(url, jsonEntity(json), Boolean.class);
        assertThat(response)
                .as("A point on the region border should still be considered inside")
                .isTrue();
    }

    @Test
    void isInRegionShouldReturn400ForUnclosedRegion() {
        String url = "http://localhost:" + port + "/api/v1/isInRegion";
        String invalidJson = """
    {
      "position": {"lng": -3.189, "lat": 55.944},
      "region": {
        "name": "central",
        "vertices": [
          {"lng": -3.192473, "lat": 55.946233},
          {"lng": -3.192473, "lat": 55.942617}
        ]
      }
    }
    """;
        var response = this.restTemplate.postForEntity(url, jsonEntity(invalidJson), String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }


    // testing bad data

    @Test
    void distanceToShouldReturn400ForInvalidJson() {
        String url = "http://localhost:" + port + "/api/v1/distanceTo";
        String badJson = """
    {
      "position1": {"lat": 0.0}
    }
    """;

        var response = this.restTemplate.postForEntity(url, jsonEntity(badJson), String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }


    @Test
    void isCloseToShouldReturn400ForMalformedRequest() {
        String url = "http://localhost:" + port + "/api/v1/isCloseTo";
        String badJson = """
        {
          "position1": {"lat": 0.0}
        }
        """;
        var response = this.restTemplate.postForEntity(url, jsonEntity(badJson), String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void nextPositionShouldReturn400ForInvalidJson() {
        String url = "http://localhost:" + port + "/api/v1/nextPosition";
        String badJson = """
        {
          "position": {"lat": 0.0},
          "angle": "notANumber"
        }
        """;
        var response = this.restTemplate.postForEntity(url, jsonEntity(badJson), String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }


}
