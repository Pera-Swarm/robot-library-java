package swarm.robot.helpers;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import swarm.Interfaces.IMqttHandler;
import swarm.mqtt.RobotMqttClient;
import swarm.mqtt.MqttMsg;
import swarm.robot.Robot;

import java.util.HashMap;

import static java.lang.Math.ceil;

/**
 * The class that handling position and heading information of the Virtual
 * Robots
 * 
 * @author Nuwan Jaliyagoda
 */
public class Coordinate implements IMqttHandler {

    private double x, y, heading;
    protected RobotMqttClient robotMqttClient;
    private final int robotId;

    private enum mqttTopic {
        ROBOT_LOCALIZATION
    }

    private final HashMap<mqttTopic, String> topicsSub = new HashMap<mqttTopic, String>();

    /**
     * Coordinate class
     * 
     * @param robotId
     * @param x          X coordinate as double
     * @param y          Y coordinate as double
     * @param heading    Heading direction in degrees, as double
     * @param mqttClient mqttClient object
     */
    public Coordinate(int robotId, double x, double y, double heading, RobotMqttClient mqttClient) {
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.robotId = robotId;
        this.robotMqttClient = mqttClient;

        subscribe(mqttTopic.ROBOT_LOCALIZATION, "localization/update/?");
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
     * Handle localization related MQTT subscriptions
     * 
     * @param robot   Robot object
     * @param message Subscription topic value
     */
    public void handleSubscription(Robot robot, MqttMsg message) {
        String topic = message.topic;
        // String msg = message.message;

        if (topic.equals(topicsSub.get(mqttTopic.ROBOT_LOCALIZATION))) {
            System.out.println("publishing the localization data of robot " + robot.getId());
            publishCoordinate();
        }
    }

    /**
     * Get robot's current X coordinate
     * 
     * @return X coordinate as a double
     */
    public double getX() {
        return this.x;
    }

    /**
     * Get robot's current Y coordinate
     * 
     * @return Y coordinate as a double
     */
    public double getY() {
        return this.y;
    }

    /**
     * Set robot's current X coordinate
     * 
     * @param x coordinate as a double
     */

    public void setX(double x) {
        this.x = x;
    }

    /**
     * Set robot's current Y coordinate
     * 
     * @param y coordinate as a double
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Get robot's current heading direction
     * 
     * @return heading in degrees
     */
    public double getHeading() {
        return this.heading;
    }

    /**
     * Get robot's current heading direction
     * 
     * @see Coordinate#getHeading()
     * @return heading in radians
     */
    public double getHeadingRad() {
        return (double) (Math.toRadians(this.heading));
    }

    /**
     * Set robot's current heading direction
     * 
     * @param heading in degrees, [-180, 180]
     */
    public void setHeading(double heading) {
        this.heading = getNormalizedHeading(heading);
    }

    /**
     * Set robot's current heading direction
     * 
     * @param heading in radians, [-PI, PI]
     */
    public void setHeadingRad(double heading) {
        setHeading(Math.toDegrees(heading));
    }

    /**
     * Set robot's current (x,y) coordinate
     * 
     * @param x X coordinate as a double
     * @param y Y coordinate as a double
     */
    public void setCoordinate(double x, double y) {
        setX(x);
        setY(y);
    }

    /**
     * Set robot's current (x,y) coordinate and heading direction
     * 
     * @param x       X coordinate as a double
     * @param y       Y coordinate as a double
     * @param heading Heading in radians, [-PI, PI]
     */
    public void setCoordinate(double x, double y, double heading) {
        setCoordinate(x, y);
        setHeading(heading);
    }

    /**
     * Print robot's current (x,y,heading) values to StdOut
     * 
     */
    public void print() {
        System.out.println(this.toString());
    }

    /**
     * Returns robot's current (x,y,heading) values as a string
     * 
     * @return String
     */
    public String toString() {
        return "x:" + round2(x) + " y:" + round2(y) + " heading:" + round2(heading);
    }

    /**
     * Publish current (x,y,heading) values to the MQTT channel
     * 
     */
    public void publishCoordinate() {
        JSONObject coord = new JSONObject();
        coord.put("id", robotId);
        coord.put("x", getX());
        coord.put("y", getY());
        coord.put("heading", getHeading());
        coord.put("reality", "V");

        JSONArray data = new JSONArray();
        data.add(coord);

        robotMqttClient.publish("localization/update", data.toString());
    }

    /**
     * Round a double into 2 decimal places
     * 
     * @param x as a double
     * @return rounded double value with 2 decimal points
     */
    private double round2(double x) {
        return Math.round(x * 100) / 100.0;
    }

    /**
     * Normalize the heading angle into [-180,180] range
     * 
     * @param heading as a double
     * @return rnormalized heading direction in range [-180,180]
     */
    private double getNormalizedHeading(double heading) {
        return heading - ceil(heading / 360 - 0.5) * 360;
    }

}
