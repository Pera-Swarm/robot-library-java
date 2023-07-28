package swarm.robot.helpers;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import swarm.Interfaces.IMqttHandler;
import swarm.mqtt.RobotMqttClient;
import swarm.mqtt.MqttMsg;
import swarm.robot.Robot;

import java.util.HashMap;

import static java.lang.Math.ceil;

public class Coordinate implements IMqttHandler {

    private double x, y, heading;
    protected RobotMqttClient robotMqttClient;
    private final int robotId;

    private enum mqttTopic {
        ROBOT_LOCALIZATION
    }

    private final HashMap<mqttTopic, String> topicsSub = new HashMap<mqttTopic, String>();

    public Coordinate(int robotId, double x, double y, double heading, RobotMqttClient m) {
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.robotId = robotId;
        this.robotMqttClient = m;

        subscribe(mqttTopic.ROBOT_LOCALIZATION, "localization/update/?");
    }

    private void subscribe(mqttTopic key, String topic) {
        topicsSub.put(key, topic); // Put to the queue
        robotMqttClient.subscribe(topic); // Subscribe through MqttHandler
    }

    public void handleSubscription(Robot r, MqttMsg m) {
        String topic = m.topic;
        String msg = m.message;

        if (topic.equals(topicsSub.get(mqttTopic.ROBOT_LOCALIZATION))) {
            System.out.println("publishing the localization data of robot " + robotId);
            publishCoordinate();
        }
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getHeading() {
        return this.heading;
    }

    public double getHeadingRad() {
        return (double) (Math.toRadians(this.heading));
    }

    public void setHeading(double heading) {
        // heading between [180,-180)
        this.heading = getNormalizedHeading(heading);
    }

    public void setHeadingRad(double heading) {
        // translate to degrees and store
        setHeading(Math.toDegrees(heading));
    }

    public void setCoordinate(double x, double y) {
        setX(x);
        setY(y);
    }

    public void setCoordinate(double x, double y, double heading) {
        setCoordinate(x, y);
        setHeading(heading);
    }

    public void print() {
        System.out.println(this.toString());
    }

    public String toString() {
        return "x:" + round2(x) + " y:" + round2(y) + " heading:" + round2(heading);
    }

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

    // Helper functions -----------------------------------------------------

    private double round2(double x) {
        return Math.round(x * 100) / 100.0;
    }

    private double getNormalizedHeading(double heading) {
        return heading - ceil(heading / 360 - 0.5) * 360;
    }

}
