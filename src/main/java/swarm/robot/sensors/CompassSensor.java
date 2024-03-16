package swarm.robot.sensors;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import swarm.mqtt.MqttMsg;
import swarm.mqtt.RobotMqttClient;
import swarm.robot.Robot;
import swarm.robot.VirtualRobot;
import swarm.robot.exception.SensorException;

import java.util.HashMap;

/**
 * Compass Sensors Emulator class
 * 
 * @author Dinuka Mudalige
 */
public class CompassSensor extends AbstractSensor {
    private enum mqttTopic {
        COMPASS_OUT
    }

    private HashMap<CompassSensor.mqttTopic, String> topicsSub = new HashMap<>();
    private double reading;

    private double bearing;

    private static final double TOLERANCE = 0.000001;  // Value should be closer to zero

    private static final double NORTH = 0.0;  // Should measure relative to a reference

    public CompassSensor(Robot robot, double initialBearing, RobotMqttClient mqttClient) {
        super(robot, mqttClient);
        this.bearing = initialBearing;
        subscribe(CompassSensor.mqttTopic.COMPASS_OUT, "sensor/compass/" + robotId + "/?");
    }

    /**
     * Subscribe to a MQTT topic
     *
     * @param key   Subscription topic key
     * @param topic Subscription topic value
     */
    private void subscribe(CompassSensor.mqttTopic key, String topic) {
        topicsSub.put(key, topic);
        robotMqttClient.subscribe(topic);
    }

    /**
     * Handle compassSensor related MQTT subscriptions
     *
     * @param robot Robot object
     * @param m     Subscription topic received object
     */
    @Override
    public void handleSubscription(Robot robot, MqttMsg m) throws RuntimeException {
        String topic = m.topic, msg = m.message;

        if (topic.equals(topicsSub.get(mqttTopic.COMPASS_OUT))) {
            sendCompass(readCompass());
        } else {
            System.out.println("Received (unknown): " + topic + "> " + msg);
        }
    }

    /**
     * Get the emulated compass sensor reading from the simulator
     *
     * @return heading as double
     * @throws SensorException
     */
    public double readCompass() {
        try {
            if (robot instanceof VirtualRobot) {

                double robotHead = robot.coordinates.getHeading();
                if (Math.abs(robotHead - bearing) < TOLERANCE) {  // 'robotHead' is approximately equal to 'bearing'
                    reading = Math.abs(NORTH) + bearing;
                } else {
                    reading = convertToNorth(robotHead, bearing);
                    bearing = reading;
                }

            } else {
                robot.handleSubscribeQueue();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return reading;
    }

    /**
     * Send the compass reading to simulation server
     * 
     * @param compass compass reading
     */
    public void sendCompass(double compass) {

        JSONObject obj = new JSONObject();
        obj.put("id", robotId);
        obj.put("compass", compass);

        robotMqttClient.publish("sensor/compass/", obj.toJSONString());
    }

    private double convertToNorth(double rotatedAngle, double currentOrientation) {
        // Normalize the current orientation to ensure it's within [0, 360)
        currentOrientation = (currentOrientation + 360) % 360;

        double bearing = Math.abs(NORTH) + currentOrientation + rotatedAngle;

        bearing = (bearing + 360) % 360;

        return bearing;
    }
}
