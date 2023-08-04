package swarm.robot.communication;

import org.json.simple.JSONObject;
import swarm.mqtt.RobotMqttClient;
import swarm.mqtt.MqttMsg;
import swarm.robot.Robot;

import java.util.HashMap;

public class SimpleCommunication extends Communication {

    enum mqttTopic {
        COMMUNICATION_IN_SIMP
    }

    private final HashMap<mqttTopic, String> topicsSub = new HashMap<mqttTopic, String>();

    public SimpleCommunication(int robotId, RobotMqttClient m) {
        super(robotId, m);
        subscribe(mqttTopic.COMMUNICATION_IN_SIMP, "comm/in/simple/" + robotId);
    }

    private void subscribe(mqttTopic key, String topic) {
        topicsSub.put(key, topic);
        robotMqttClient.subscribe(topic);
    }

    public void sendMessage(String msg) {
        JSONObject obj = new JSONObject();
        obj.put("id", robotId);
        obj.put("msg", msg);
        robotMqttClient.publish("comm/out/simple", obj.toJSONString());
    }

    public void sendMessage(String msg, int distance) {
        JSONObject obj = new JSONObject();
        obj.put("id", robotId);
        obj.put("msg", msg);
        obj.put("dist", distance);
        robotMqttClient.publish("comm/out/simple", obj.toJSONString());
    }

    @Override
    public void handleSubscription(Robot robot, MqttMsg m) {
        String topic = m.topic, msg = m.message;

        if (topic.equals(topicsSub.get(mqttTopic.COMMUNICATION_IN_SIMP))) {
            // comm/in/simple/{id}
            // System.out.println("Received: " + topic + "> " + msg);
            robot.communicationInterrupt(msg);

        } else {
            System.out.println(topicsSub.get(mqttTopic.COMMUNICATION_IN_SIMP));
            System.out.println("Received (unknown simp): " + topic + "> " + msg);
        }
    }
}
