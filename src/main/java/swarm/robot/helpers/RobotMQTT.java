package swarm.robot.helpers;

import org.json.simple.JSONObject;

import swarm.mqtt.RobotMqttClient;
import swarm.mqtt.MqttMsg;
import swarm.robot.Robot;

import java.util.HashMap;

public class RobotMQTT {

    protected RobotMqttClient robotMqttClient;
    protected int robotId;
    protected char reality;

    private enum mqttTopic {
        ROBOT_MSG, ROBOT_MSG_BROADCAST
    }

    private final HashMap<mqttTopic, String> topicsSub = new HashMap<mqttTopic, String>();

    public RobotMQTT(int robotId, RobotMqttClient mqtt, char reality) {
        this.robotId = robotId;
        this.robotMqttClient = mqtt;
        this.reality = reality;

        subscribe(mqttTopic.ROBOT_MSG, "robot/msg/" + robotId);
        subscribe(mqttTopic.ROBOT_MSG_BROADCAST, "robot/msg/broadcast");
    }

    public void robotCreate(double x, double y, double heading) {
        JSONObject msg = new JSONObject();
        msg.put("id", robotId);
        msg.put("x", x);
        msg.put("y", y);
        msg.put("heading", heading);
        msg.put("reality", "V");

        String jsonString = msg.toJSONString();
        robotMqttClient.publish("robot/create", jsonString);
    }

    private void subscribe(mqttTopic key, String topic) {
        topicsSub.put(key, topic); // Put to the queue
        robotMqttClient.subscribe(topic); // Subscribe through MqttHandler
    }

    public void handleSubscription(Robot r, MqttMsg m) {
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
                    // execute pattern.start method
                    r.start();
                    break;

                case "STOP":
                    // execute pattern.stop method
                    r.stop();
                    break;

                case "RESET":
                    // execute pattern.reset method
                    r.reset();
                    break;
            }

        } else {
            System.out.println("Received (unknown): " + topic + "> " + msg);
        }

    }
}
