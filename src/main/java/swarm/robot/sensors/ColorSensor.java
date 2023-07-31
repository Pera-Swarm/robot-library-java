package swarm.robot.sensors;

import org.json.simple.parser.ParseException;
import swarm.mqtt.RobotMqttClient;

import java.util.HashMap;

import org.json.simple.JSONObject;
import swarm.mqtt.MqttMsg;
import swarm.robot.exception.SensorException;
import swarm.robot.Robot;
import swarm.robot.types.RGBColorType;

public class ColorSensor extends AbstractSensor {

    private final static int MQTT_TIMEOUT = 1000;

    private enum mqttTopic {
        COLOR_IN, COLOR_LOOK
    }

    private HashMap<mqttTopic, String> topicsSub = new HashMap<mqttTopic, String>();
    private boolean col_lock = false;

    RGBColorType color;

    public ColorSensor(Robot robot, RobotMqttClient m) {
        super(robot, m);

        color = new RGBColorType(0, 0, 0);
        subscribe(mqttTopic.COLOR_IN, "sensor/color/" + robotId);
        subscribe(mqttTopic.COLOR_LOOK, "sensor/color/" + robotId + "/?");
    }

    private void subscribe(mqttTopic key, String topic) {
        topicsSub.put(key, topic); // Put to the queue
        robotMqttClient.subscribe(topic); // Subscribe through MqttHandler
    }

    @Override
    public void handleSubscription(Robot robot, MqttMsg m) {
        String topic = m.topic, msg = m.message;

        if (topic.equals(topicsSub.get(mqttTopic.COLOR_IN))) {
            // sensor/color/{id}
            // System.out.println("Color Input>" + msg);

            color.setColor(msg);
            col_lock = false;

        } else {
            System.out.println("Received (unknown): " + topic + "> " + color.toString());
        }
    }

    public RGBColorType getColor() throws Exception {

        // Prepare the message
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
            // System.out.print(".");
            robot.delay(100);
            timeout = (System.currentTimeMillis() - stratTime > MQTT_TIMEOUT);
        }

        if (timeout) {
            throw new SensorException("Color sensor timeout");
        }
        return color;
    }

    public void sendColor(int r, int g, int b, int ambient) {
        // Only for test, virtual robots will not invoke this.

        JSONObject obj = new JSONObject();
        obj.put("id", robotId);
        obj.put("R", r);
        obj.put("G", g);
        obj.put("B", b);
        obj.put("ambient", ambient);

        robotMqttClient.publish("sensor/color/", obj.toJSONString());
    }
}
