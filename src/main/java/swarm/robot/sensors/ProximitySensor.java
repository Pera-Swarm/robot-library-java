package swarm.robot.sensors;

import org.json.simple.parser.ParseException;
import swarm.mqtt.RobotMqttClient;

import java.util.HashMap;

import org.json.simple.JSONObject;
import swarm.mqtt.MqttMsg;
import swarm.robot.Robot;
import swarm.robot.exception.ProximityException;
import swarm.robot.exception.SensorException;
import swarm.robot.types.ProximityReadingType;

public class ProximitySensor extends AbstractSensor {

    private final static int MQTT_TIMEOUT = 1000;

    private enum mqttTopic {
        PROXIMITY_IN
    }

    private HashMap<mqttTopic, String> topicsSub = new HashMap<mqttTopic, String>();

    private boolean proximity_lock = false;
    private ProximityReadingType proximity;

    public ProximitySensor(Robot robot, RobotMqttClient m) {
        super(robot, m);
        subscribe(mqttTopic.PROXIMITY_IN, "sensor/proximity/" + robotId);
    }

    private void subscribe(mqttTopic key, String topic) {
        topicsSub.put(key, topic); // Put to the queue
        robotMqttClient.subscribe(topic); // Subscribe through MqttHandler
    }

    @Override
    public void handleSubscription(Robot robot, MqttMsg m) {
        // sensor/proximity/
        String topic = m.topic;
        String msg = m.message;

        if (topic.equals(topicsSub.get(mqttTopic.PROXIMITY_IN))) {
            // sensor/proximity/{id}
            // System.out.println("Input>" + msg);
            try {
                proximity = new ProximityReadingType(msg);
            } catch (ProximityException e) {
                e.printStackTrace();
            }
            proximity_lock = false;

        } else {
            System.out.println("Received (unknown): " + topic + "> " + msg);
        }
    }

    public ProximityReadingType getProximity() throws Exception {

        // Prepare the message
        JSONObject msg = new JSONObject();
        msg.put("id", robotId);
        msg.put("reality", "M"); // inform the requesting reality

        proximity_lock = true; // Acquire the proximity sensor lock

        robotMqttClient.publish("sensor/proximity", msg.toJSONString());
        robot.delay(250);

        long startTime = System.currentTimeMillis();
        boolean timeout = false;

        while (proximity_lock && !timeout) {
            try {
                robot.handleSubscribeQueue();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            System.out.print(".");
            robot.delay(100);
            timeout = (System.currentTimeMillis() - startTime > MQTT_TIMEOUT);
        }

        if (timeout) {
            throw new SensorException("Distance sensor timeout");
        }

        System.out.println(" proximity: " + proximity.toString());
        return proximity;
    }

    public void sendProximity() {
        // Only for test, virtual robots will not invoke this.

        JSONObject obj = new JSONObject();
        obj.put("id", robotId);
        obj.put("proximity", proximity.toString());
        robotMqttClient.publish("sensor/proximity/", obj.toJSONString());
    }
}
