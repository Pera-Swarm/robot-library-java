package swarm.robot.sensors;

import org.json.simple.parser.ParseException;
import swarm.mqtt.RobotMqttClient;

import java.util.HashMap;

import org.json.simple.JSONObject;
import swarm.mqtt.MqttMsg;
import swarm.robot.exception.SensorException;
import swarm.robot.Robot;
import swarm.robot.types.RGBColorType;

/**
 * Color Sensors Emulator class
 * 
 * @author Nuwan Jaliyagoda
 */
public class ColorSensor extends AbstractSensor {

    private enum mqttTopic {
        COLOR_IN, COLOR_LOOK
    }

    private final static int MQTT_TIMEOUT = 1000;

    private HashMap<mqttTopic, String> topicsSub = new HashMap<mqttTopic, String>();
    private boolean col_lock = false;

    RGBColorType color;

    /**
     * ColorSensor class
     * 
     * @param robot      robot object
     * @param mqttClient mqttClient object
     */
    public ColorSensor(Robot robot, RobotMqttClient mqttClient) {
        super(robot, mqttClient);

        color = new RGBColorType(0, 0, 0);
        subscribe(mqttTopic.COLOR_IN, "sensor/color/" + robotId);
        subscribe(mqttTopic.COLOR_LOOK, "sensor/color/" + robotId + "/?");
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
     * Handle colorSensor related MQTT subscriptions
     * 
     * @param robot Robot object
     * @param m     Subscription topic received object
     */
    @Override
    public void handleSubscription(Robot robot, MqttMsg m) {
        String topic = m.topic, msg = m.message;

        if (topic.equals(topicsSub.get(mqttTopic.COLOR_IN))) {
            // sensor/color/{id}

            color.setColor(msg);
            col_lock = false;

        } else {
            System.out.println("Received (unknown): " + topic + "> " + color.toString());
        }
    }

    /**
     * Get the emulated color sensor reading from the simulator
     * 
     * @return color as RGBColorType
     * @throws SensorException
     */
    public RGBColorType getColor() throws SensorException {

        JSONObject msg = new JSONObject();
        msg.put("id", robotId);
        msg.put("reality", "M"); // inform the requesting reality

        col_lock = true; // Acquire the color sensor lock

        robotMqttClient.publish("sensor/color", msg.toJSONString());
        robot.delay(250);

        long stratTime = System.currentTimeMillis();
        boolean timeout = false;

        while (col_lock && !timeout) {
            try {
                robot.handleSubscribeQueue();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            robot.delay(100);
            timeout = (System.currentTimeMillis() - stratTime > MQTT_TIMEOUT);
        }

        if (timeout) {
            throw new SensorException("Color sensor timeout");
        }
        return color;
    }

    /**
     * Send the current color information on MQTT requests, Only for test, virtual
     * robots will not invoke this.
     * 
     * @param red     Red intensity, [0,255]
     * @param green   Green intensity, [0,255]
     * @param blue    Blue intensity, [0,255]
     * @param ambient Ambient status, [0,255]
     */
    public void sendColor(int red, int green, int blue, int ambient) {
        JSONObject obj = new JSONObject();
        obj.put("id", robotId);
        obj.put("R", red);
        obj.put("G", green);
        obj.put("B", blue);
        obj.put("ambient", ambient);

        robotMqttClient.publish("sensor/color/", obj.toJSONString());
    }
}
