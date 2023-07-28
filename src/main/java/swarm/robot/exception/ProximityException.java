package swarm.robot.exception;

public class ProximityException extends Exception {
    public ProximityException(String msg) {
        System.out.println("Proximity reading error: " + msg);
    }
}
