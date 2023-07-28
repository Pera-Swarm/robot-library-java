package swarm.robot.sensors;

import swarm.Interfaces.IMqttHandler;
import swarm.mqtt.RobotMqttClient;
import swarm.mqtt.MqttMsg;
import swarm.robot.Robot;

public abstract class AbstractSensor implements IMqttHandler {

    protected RobotMqttClient robotMqttClient;
    protected int robotId;
    protected Robot robot;

    public AbstractSensor(Robot robot, RobotMqttClient m) {
        this.robotMqttClient = m;
        this.robotId = robot.getId();
        this.robot = robot;
    }

    public void handleSubscription(Robot r, MqttMsg m) {
        // This will handle incoming messages with already subscribed topics
    }

}
