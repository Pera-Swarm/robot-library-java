package swarm.robot.helpers;

import org.json.simple.JSONObject;

import swarm.mqtt.RobotMqttClient;
import swarm.mqtt.MqttMsg;
import swarm.robot.Robot;

import java.util.HashMap;

/**
 * The class that handle robot's MQTT interactions
 * 
 * @author Nuwan Jaliyagoda
 */
public class RobotMQTT {

    protected RobotMqttClient robotMqttClient;
    protected int robotId;
    protected char reality;

    private enum mqttTopic {
        ROBOT_MSG, ROBOT_MSG_BROADCAST
    }

    private final HashMap<mqttTopic, String> topicsSub = new HashMap<mqttTopic, String>();

    /**
     * RobotMQTT class
     * 
     * @param robotId
     * @param mqtt    MQTT object
     * @param reality reality of the robot, currently only support 'V'
     */
    public RobotMQTT(int robotId, RobotMqttClient mqtt, char reality) {
        this.robotId = robotId;
        this.robotMqttClient = mqtt;
        this.reality = reality;

        subscribe(mqttTopic.ROBOT_MSG, "robot/msg/" + robotId);
        subscribe(mqttTopic.ROBOT_MSG_BROADCAST, "robot/msg/broadcast");
    }

    /**
     * Create robot instance on the simulator
     * 
     * @param robotId
     * @param x       X coordinate as double
     * @param y       Y coordinate as double
     * @param heading Heading direction in degrees, as double
     */
    public void robotCreate(double x, double y, double heading) {
        JSONObject msg = new JSONObject();
        msg.put("id", robotId);
        msg.put("x", x);
        msg.put("y", y);
        msg.put("heading", heading);
        msg.put("reality", this.reality);

        String jsonString = msg.toJSONString();
        robotMqttClient.publish("robot/create", jsonString);
    }

    /**
     * Subscribe to a MQTT topic
     * 
     * @param key   Subscription topic key
     * @param topic Subscription topic value
     */
    private void subscribe(mqttTopic key, String topic) {
        topicsSub.put(key, topic);
        robotMqttClient.subscribe(topic);
    }

    /**
     * Handle localization related MQTT subscriptions
     * 
     * @param robot   Robot object
     * @param message Subscription topic value
     */
    public void handleSubscription(Robot robot, MqttMsg m) {
        String topic = m.topic, msg = m.message;

        if (topic.equals(topicsSub.get(mqttTopic.ROBOT_MSG))
                || topic.equals(topicsSub.get(mqttTopic.ROBOT_MSG_BROADCAST))) {
            // robot/msg/{robotId} or robot/msg/broadcast
            // System.out.println("Received: " + topic + "> " + msg);

            String msgTopic = msg.split(" ")[0];

            switch (msgTopic) {
                case "ID?":
                    // ID? -1 message
                    JSONObject obj = new JSONObject();
                    obj.put("id", robotId);
                    obj.put("reality", "V");
                    robotMqttClient.publish("robot/live", obj.toJSONString());
                    System.out.println("robot/live > " + obj.toJSONString());
                    break;

                case "START":
                    // Execute START command
                    robot.start();
                    break;

                case "STOP":
                    // Execute STOP command
                    robot.stop();
                    break;

                case "RESET":
                    // Execute RESET command
                    robot.reset();
                    break;
            }

        } else {
            System.out.println("Received (unknown): " + topic + "> " + msg);
        }

    }
}
