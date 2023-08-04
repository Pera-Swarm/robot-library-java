package swarm.robot;

import org.json.simple.parser.ParseException;
import swarm.Interfaces.IRobotState;
import swarm.configs.MQTTSettings;
import swarm.mqtt.RobotMqttClient;
import swarm.mqtt.MqttMsg;
import swarm.robot.communication.DirectedCommunication;
import swarm.robot.communication.SimpleCommunication;
import swarm.robot.helpers.Coordinate;
import swarm.robot.helpers.MotionController;
import swarm.robot.helpers.RobotMQTT;
import swarm.robot.indicator.NeoPixel;
import swarm.robot.sensors.ColorSensor;
import swarm.robot.sensors.DistanceSensor;
import swarm.robot.sensors.ProximitySensor;

/**
 * Abstract Class implementation for Robot
 * 
 * @author Nuwan Jaliyagoda
 */
public abstract class Robot implements Runnable, IRobotState {

    // Sensors -----------------------------------------------------------
    public DistanceSensor distSensor;
    public ProximitySensor proximitySensor;
    public ColorSensor colorSensor;

    // Communication -----------------------------------------------------
    public SimpleCommunication simpleComm;
    public DirectedCommunication directedComm;

    // Output ------------------------------------------------------------
    public NeoPixel neoPixel;

    // Helper and Controller objects ------------------------------------
    public MotionController motion;
    public RobotMQTT robotMQTT;
    public Coordinate coordinates;
    public RobotMqttClient robotMqttClient;

    // Variables ---------------------------------------------------------
    protected int id;
    protected char reality;
    protected robotState state = robotState.WAIT;

    /**
     * Abstract Robot class
     * 
     * @param id      robot Id
     * @param x       X coordinate as double
     * @param y       Y coordinate as double
     * @param heading Heading direction in degrees, as double
     * @param reality Reality of the robot, currently only support 'V'
     */
    public Robot(int id, double x, double y, double heading, char reality) {

        this.id = id;
        this.reality = reality;

        robotMqttClient = new RobotMqttClient(MQTTSettings.server, MQTTSettings.port, MQTTSettings.userName,
                MQTTSettings.password, MQTTSettings.channel);

        coordinates = new Coordinate(id, x, y, heading, robotMqttClient);
        robotMQTT = new RobotMQTT(id, robotMqttClient, reality);

        // Request to create a robot instance at Simulator
        robotMQTT.robotCreate(coordinates.getX(), coordinates.getY(), coordinates.getHeading());

        delay(1000);
        this.motion = new MotionController(coordinates);
    }

    /**
     * Setup the modules, emulators and indicators
     */
    public void setup() {
        // Setup each module
        distSensor = new DistanceSensor(this, robotMqttClient);
        proximitySensor = new ProximitySensor(this, robotMqttClient);
        colorSensor = new ColorSensor(this, robotMqttClient);

        neoPixel = new NeoPixel(this, robotMqttClient);

        simpleComm = new SimpleCommunication(id, robotMqttClient);
        directedComm = new DirectedCommunication(id, robotMqttClient);

        coordinates.setCoordinate(coordinates.getX(), coordinates.getY(), coordinates.getHeading());
        coordinates.publishCoordinate();
    }

    /**
     * Get robot Id
     * 
     * @return robotId as integer
     */
    public int getId() {
        return this.id;
    }

    /**
     * Robot's main event loop
     */
    @Override
    public final void run() {
        setup();

        // noinspection InfiniteLoopStatement
        while (true) {
            long begin_time = System.currentTimeMillis();
            try {
                loop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            long end_time = System.currentTimeMillis();

            // DO NOT REMOVE OR EDIT THIS DELAY
            delay((int) Math.max(0, 1000 - (end_time - begin_time)));

            try {
                handleSubscribeQueue();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle MQTT subscriptions
     */
    public final void handleSubscribeQueue() throws ParseException {

        while (!robotMqttClient.inQueue.isEmpty()) {
            MqttMsg m = robotMqttClient.inQueue.poll();

            if (m != null) {
                switch (m.topicGroups[0]) {
                    case "robot":
                        // Robot messages
                        // System.out.println("robot message received");
                        robotMQTT.handleSubscription(this, m);
                        break;

                    case "sensor":
                        // Sensor messages

                        if (m.topicGroups[1].equals("distance")) {
                            // System.out.println("distance sensor message received");
                            distSensor.handleSubscription(this, m);
                        } else if (m.topicGroups[1].equals("color")) {
                            // System.out.println("color sensor message received");
                            colorSensor.handleSubscription(this, m);
                        } else if (m.topicGroups[1].equals("proximity")) {
                            // System.out.println("proximity sensor message received");
                            proximitySensor.handleSubscription(this, m);
                        }

                        break;
                    case "localization":
                        // Localization messages
                        // System.out.println("localization message received");

                        if (m.topic.equals("localization/update/?")) {
                            coordinates.handleSubscription(this, m);
                        }

                        break;
                    case "comm":
                        // Communication messages
                        // System.out.println("communication message received");
                        if (m.topicGroups[2].equals("simple")) {
                            simpleComm.handleSubscription(this, m);
                        } else {
                            directedComm.handleSubscription(this, m);
                        }
                        break;

                    case "output":
                        if ("NeoPixel".equals(m.topicGroups[1])) {
                            neoPixel.handleSubscription(this, m);
                        }
                        break;
                }
            }
        }
    }

    /**
     * Publish the queued MQTT messages
     * 
     * @Deprecated
     */
    private void handlePublishQueue() {
        // Publish messages which are collected in the outgoing queue

        while (!robotMqttClient.outQueue.isEmpty()) {
            MqttMsg m = robotMqttClient.outQueue.poll();
            assert m != null;
            robotMqttClient.publish(m.topic, m.message, m.QoS);
        }
    }

    /**
     * Method which handles START command
     */
    @Override
    public void start() {
        System.out.println("Robot start on " + id);
        state = robotState.RUN;
    }

    /**
     * Method which handles STOP command
     */
    @Override
    public void stop() {
        System.out.println("Robot stop on " + id);
        state = robotState.WAIT;
    }

    /**
     * Method which handles RESET command
     */
    @Override
    public void reset() {
        System.out.println("Robot reset on " + id);
        state = robotState.BEGIN;
    }

    /**
     * Delay the robot functionality for a given time
     * 
     * @param delay, delay in milliseconds
     */
    public void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

}