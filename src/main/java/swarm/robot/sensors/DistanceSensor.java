package swarm.robot.sensors;

import org.json.simple.parser.ParseException;
import swarm.mqtt.RobotMqttClient;

import java.util.HashMap;

import org.json.simple.JSONObject;
import swarm.mqtt.MqttMsg;
import swarm.robot.exception.SensorException;
import swarm.robot.Robot;

/**
 * Distance Sensors Emulator class
 * 
 * @author Nuwan Jaliyagoda
 */
public class DistanceSensor extends AbstractSensor {

    private enum mqttTopic {
        DISTANCE_IN, DISTANCE_LOOK
    }

    private final static int MQTT_TIMEOUT = 1000;

    private HashMap<mqttTopic, String> topicsSub = new HashMap<mqttTopic, String>();

    private boolean dist_lock = false;
    private int dist_value = 0;

    /**
     * DistanceSensor class
     * 
     * @param robot      robot object
     * @param mqttClient mqttClient object
     */
    public DistanceSensor(Robot robot, RobotMqttClient m) {
        super(robot, m);
        subscribe(mqttTopic.DISTANCE_IN, "sensor/distance/" + robotId);
        subscribe(mqttTopic.DISTANCE_LOOK, "sensor/distance/" + robotId + "/?");
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
     * Handle distanceSensor related MQTT subscriptions
     * 
     * @param robot Robot object
     * @param m     Subscription topic received object
     */
    @Override
    public void handleSubscription(Robot robot, MqttMsg m) {
        String topic = m.topic, msg = m.message;

        if (topic.equals(topicsSub.get(mqttTopic.DISTANCE_IN))) {
            // sensor/distance/{id}

            if (msg.compareTo("Infinity") == 0) {
                // -1 will be returned as a fail-proof option. Should throw an exception
                dist_value = -1;
            } else {
                dist_value = Integer.parseInt(msg);
            }
            dist_lock = false;

        } else {
            System.out.println("Received (unknown): " + topic + "> " + msg);
        }
    }

    /**
     * Get the emulated distance sensor reading from the simulator
     * 
     * @return distance as double
     * @throws SensorException
     */
    public double getDistance() throws Exception {

        JSONObject msg = new JSONObject();
        msg.put("id", robotId);
        msg.put("reality", "M"); // inform the requesting reality

        dist_lock = true; // Acquire the distance sensor lock

        robotMqttClient.publish("sensor/distance", msg.toJSONString());
        robot.delay(250);

        long startTime = System.currentTimeMillis();
        boolean timeout = false;

        while (dist_lock && !timeout) {
            try {
                robot.handleSubscribeQueue();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            robot.delay(100);
            timeout = (System.currentTimeMillis() - startTime > MQTT_TIMEOUT);
        }

        if (timeout) {
            throw new SensorException("Distance sensor timeout");
        }

        return dist_value;
    }

    /**
     * Send the current distance information on MQTT requests, Only for test,
     * virtual robots will not invoke this.
     * 
     * @param dist distanceReading
     */
    public void sendDistance(double dist) {

        JSONObject obj = new JSONObject();
        obj.put("id", robotId);
        obj.put("dist", dist);

        robotMqttClient.publish("sensor/distance/", obj.toJSONString());
    }

}
