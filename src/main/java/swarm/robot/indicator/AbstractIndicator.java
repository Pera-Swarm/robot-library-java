package swarm.robot.indicator;

import org.json.simple.parser.ParseException;
import swarm.mqtt.RobotMqttClient;
import swarm.mqtt.MqttMsg;
import swarm.robot.Robot;

/**
 * Abstract Class implementation for Indicators
 * 
 * @author Nuwan Jaliyagoda
 */
public abstract class AbstractIndicator {
    protected RobotMqttClient robotMqttClient;
    protected int robotId;
    protected Robot robot;

    /**
     * AbstractIndicator class
     * 
     * @param robot      robot object
     * @param mqttClient mqttClient object
     */
    public AbstractIndicator(Robot robot, RobotMqttClient mqttClient) {
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
    public abstract void handleSubscription(Robot r, MqttMsg m) throws ParseException;

}
