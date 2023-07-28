package swarm.robot.exception;

public class SensorException extends Exception {
    public SensorException(String s) {
        System.out.println("Sensor Error: " + s);
    }
}
