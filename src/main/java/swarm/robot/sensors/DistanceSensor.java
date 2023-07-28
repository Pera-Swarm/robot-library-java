package swarm.robot.sensors;

import org.json.simple.parser.ParseException;
import swarm.mqtt.RobotMqttClient;

import java.util.HashMap;

import org.json.simple.JSONObject;
import swarm.mqtt.MqttMsg;
import swarm.robot.exception.SensorException;
import swarm.robot.Robot;

public class DistanceSensor extends AbstractSensor {

    private enum mqttTopic {
        DISTANCE_IN, DISTANCE_LOOK
    }

    private HashMap<mqttTopic, String> topicsSub = new HashMap<mqttTopic, String>();

    private boolean dist_lock = false;
    private int dist_value = 0;

    private final static int MQTT_TIMEOUT = 1000;

    public DistanceSensor(Robot robot, RobotMqttClient m) {
        super(robot, m);
        subscribe(mqttTopic.DISTANCE_IN, "sensor/distance/" + robotId);
        subscribe(mqttTopic.DISTANCE_LOOK, "sensor/distance/" + robotId + "/?");
    }

    private void subscribe(mqttTopic key, String topic) {
        topicsSub.put(key, topic); // Put to the queue
        robotMqttClient.subscribe(topic); // Subscribe through MqttHandler
    }

    @Override
    public void handleSubscription(Robot robot, MqttMsg m) {
        // sensor/distance/
        String topic = m.topic;
        String msg = m.message;

        if (topic.equals(topicsSub.get(mqttTopic.DISTANCE_IN))) {
            // sensor/distance/{id}
            // System.out.println("Input>" + msg);

            if (msg.compareTo("Infinity") == 0) {
                // -1 will be returned as a fail-proof option. Should throw an exception
                dist_value = -1;
            } else {
                dist_value = Integer.parseInt(msg);
            }
            dist_lock = false;

            // } else if (topic.equals(topicsSub.get(mqttTopic.DISTANCE_LOOK))) {
            // // sensor/distance/{id}/?
            // System.out.println("Received: " + topic + "> " + msg);

        } else {
            System.out.println("Received (unknown): " + topic + "> " + msg);
        }
    }

    public double getDistance() throws Exception {
        // Prepare the message
        JSONObject msg = new JSONObject();
        msg.put("id", robotId);
        msg.put("reality", "M"); // inform the requesting reality

        // Start measuring the time
        long begin = System.currentTimeMillis();
        // ---------------------------

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
            // System.out.print(".");
            robot.delay(100);
            timeout = (System.currentTimeMillis() - startTime > MQTT_TIMEOUT);
        }

        if (timeout) {
            throw new SensorException("Distance sensor timeout");
        }
        // System.out.println(" Distance: " + dist_value);

        // Stop measuring the time, and publish to log channel
        long end = System.currentTimeMillis();

        JSONObject logMsg = new JSONObject();
        logMsg.put("id", robotId);
        logMsg.put("msg", (end - begin));
        robotMqttClient.publish("robot/log", logMsg.toJSONString());
        // ---------------------------

        return dist_value;
    }

    public void sendDistance(double d) {
        // Only for test, virtual robots will not invoke this.

        JSONObject obj = new JSONObject();
        obj.put("id", robotId);
        obj.put("dist", d);

        robotMqttClient.publish("sensor/distance/", obj.toJSONString());
    }

}
