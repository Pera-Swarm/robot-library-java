package swarm.robot.exception;

public class MqttClientException extends Exception {
    public MqttClientException(String s) {
        System.out.println("MQTT Error: " + s);
    }
}