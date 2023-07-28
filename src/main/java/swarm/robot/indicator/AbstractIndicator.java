package swarm.robot.indicator;

import org.json.simple.parser.ParseException;
import swarm.mqtt.RobotMqttClient;
import swarm.mqtt.MqttMsg;
import swarm.robot.Robot;

public abstract class AbstractIndicator {
    protected RobotMqttClient robotMqttClient;
    protected int robotId;
    protected Robot robot;

    public AbstractIndicator(Robot robot, RobotMqttClient m) {
        this.robotMqttClient = m;
        this.robotId = robot.getId();
        this.robot = robot;
    }

    private void subscribe(String topic) {
        // This will subscribe to a given topic through mqttHandler
    }

    // This will handle incoming messages with already subscribed topics
    public abstract void handleSubscription(Robot r, MqttMsg m) throws ParseException;

}
