package uk.ac.ed.acp.cw2.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DroneNotFoundException extends RuntimeException {
    public DroneNotFoundException() {
        super();
    }

    public DroneNotFoundException(String message) {
        super(message);
    }
}
