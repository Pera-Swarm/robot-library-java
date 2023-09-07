package swarm.robot.indicator;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import swarm.mqtt.RobotMqttClient;
import swarm.mqtt.MqttMsg;
import swarm.robot.Robot;
import swarm.robot.types.RGBColorType;

import java.util.HashMap;

/**
 * NeoPixel LED Ring class
 * 
 * @author Nuwan Jaliyagoda
 */
public class NeoPixel extends AbstractIndicator {

    private enum mqttTopic {
        NEOPIXEL_IN
    }

    private final HashMap<NeoPixel.mqttTopic, String> topicsSub = new HashMap<NeoPixel.mqttTopic, String>();

    private int R, G, B;

    /**
     * NeoPixel class
     * 
     * @param robot      robot object
     * @param mqttClient mqttClient object
     */
    public NeoPixel(Robot robot, RobotMqttClient mqttClient) {
        super(robot, mqttClient);
    }

    /**
     * Subscribe to a MQTT topic
     * 
     * @param key   Subscription topic key
     * @param topic Subscription topic value
     */
    protected void subscribe(mqttTopic key, String topic) {
        topicsSub.put(key, topic);
        robotMqttClient.subscribe(topic);
    }

    /**
     * Handle NeoPixel related MQTT subscriptions
     * 
     * @param robot Robot object
     * @param m     Subscription topic received object
     */
    @Override
    public void handleSubscription(Robot r, MqttMsg m) throws ParseException {
        String topic = m.topic, msg = m.message;

        if (topic.equals(topicsSub.get(mqttTopic.NEOPIXEL_IN))) {
            // output/neopixel/{id}

            String[] colors = msg.split(" ");
            R = Integer.parseInt(colors[0]);
            G = Integer.parseInt(colors[1]);
            B = Integer.parseInt(colors[2]);

            changeColor(R, G, B);

        } else {
            System.out.println("Received (unknown): " + topic + "> " + msg);
        }
    }

    /**
     * Change the color of the NeoPixel Ring
     * 
     * @param red   Red intensity, [0,255]
     * @param green Green intensity, [0,255]
     * @param blue  Blue intensity, [0,255]
     */
    public void changeColor(int red, int green, int blue) {

        RGBColorType color = new RGBColorType(red, green, blue);

        JSONObject obj = new JSONObject();
        obj.put("id", robotId);
        obj.put("R", color.getR());
        obj.put("G", color.getG());
        obj.put("B", color.getB());
        robotMqttClient.publish("output/neopixel", obj.toJSONString());
    }
}
