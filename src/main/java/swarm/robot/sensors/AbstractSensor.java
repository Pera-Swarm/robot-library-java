package swarm.robot.sensors;

import swarm.Interfaces.IMqttHandler;
import swarm.mqtt.RobotMqttClient;
import swarm.mqtt.MqttMsg;
import swarm.robot.Robot;

/**
 * Abstract Class implementation for Sensors
 * 
 * @author Nuwan Jaliyagoda
 */
public abstract class AbstractSensor implements IMqttHandler {

    protected RobotMqttClient robotMqttClient;
    protected int robotId;
    protected Robot robot;

    /**
     * AbstractSensor class
     * 
     * @param robot      robot object
     * @param mqttClient mqttClient object
     */
    public AbstractSensor(Robot robot, RobotMqttClient mqttClient) {
        this.robotMqttClient = mqttClient;
        this.robotId = robot.getId();
        this.robot = robot;
    }

    /**
     * Handle related MQTT subscriptions
     * 
     * @param robot Robot object
     * @param m     Subscription topic received object
     */
    public void handleSubscription(Robot r, MqttMsg m) {

    }

}
