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

/**
 * Proximity Sensors Emulator class
 * 
 * @author Nuwan Jaliyagoda
 */
public class ProximitySensor extends AbstractSensor {

    private enum mqttTopic {
        PROXIMITY_IN
    }

    private final static int MQTT_TIMEOUT = 1000;

    private HashMap<mqttTopic, String> topicsSub = new HashMap<mqttTopic, String>();

    private boolean proximity_lock = false;
    private ProximityReadingType proximity;

    /**
     * ProximitySensor class
     * 
     * @param robot      robot object
     * @param mqttClient mqttClient object
     */
    public ProximitySensor(Robot robot, RobotMqttClient m) {
        super(robot, m);
        subscribe(mqttTopic.PROXIMITY_IN, "sensor/proximity/" + robotId);
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
     * Handle proximitySensor related MQTT subscriptions
     * 
     * @param robot Robot object
     * @param m     Subscription topic received object
     */
    @Override
    public void handleSubscription(Robot robot, MqttMsg m) {
        String topic = m.topic, msg = m.message;

        if (topic.equals(topicsSub.get(mqttTopic.PROXIMITY_IN))) {
            // sensor/proximity/{id}

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

    /**
     * Get the emulated proximity sensor reading from the simulator
     * 
     * @return distance as double
     * @throws SensorException
     */
    public ProximityReadingType getProximity() throws Exception {

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

        return proximity;
    }

    /**
     * Send the current proximity information on MQTT requests, Only for test,
     * virtual robots will not invoke this.
     * 
     * @param dist proximityReading
     */
    public void sendProximity() {

        JSONObject obj = new JSONObject();
        obj.put("id", robotId);
        obj.put("proximity", proximity.toString());
        robotMqttClient.publish("sensor/proximity/", obj.toJSONString());
    }
}
