package Robots;

import swarm.robot.VirtualRobot;
import swarm.robot.types.RGBColorType;

public class DiscoverColorRobot extends VirtualRobot {

    private int currentHopId;
    private boolean colorUpdated;
    private final RGBColorType obstacleColor;
    private boolean searching;

    public DiscoverColorRobot(int id, double x, double y, double heading, RGBColorType obstacleColor) {
        super(id, x, y, heading);
        this.obstacleColor = obstacleColor;
    }

    public void setup() {
        super.setup();
        neoPixel.changeColor(0, 0, 0);
        searching = true;
    }

    @Override
    public void loop() throws Exception {
        super.loop();
        // System.out.println(state);

        if (searching && state == robotState.RUN) {
            double dist = distSensor.getDistance();

            if (dist < 15) {
                RGBColorType color = colorSensor.getColor();
                System.out.println("\t An obstacle detected, stop");
                System.out.println("observed:" + color.toString() + " must be:" + obstacleColor.toString());

                if (obstacleColor.compareTo(color)) {
                    // Color is matching
                    System.out.println("\t Matching obstacle detected");
                    String id = Integer.toString(getId());
                    simpleComm.sendMessage("1 " + obstacleColor.toStringColor(), 70);
                    neoPixel.changeColor(obstacleColor.getR(), obstacleColor.getG(), obstacleColor.getB());
                    state = robotState.WAIT;
                    searching = false;

                } else {
                    // Generate a random number in [-1000,1000] range
                    // if even, rotate CW, otherwise rotate CCW an angle depends on the random
                    // number
                    int random = -1000 + ((int) ((Math.random() * 2000)));
                    int sign = (random % 2 == 0) ? 1 : -1;

                    System.out.println("\t Not the obstacle we are looking for, go back and rotate "
                            + ((sign > 0) ? "CW" : "CCW"));

                    // Go back a little
                    motion.move(-100, -100, 900);

                    // rotate
                    int loopCount = 0; // to avoid infinity loop
                    while (distSensor.getDistance() < 35 && loopCount < 5) {
                        motion.rotate(50 * sign, 1000);
                        loopCount++;
                    }
                    // TODO: This is a temp update to restrict the robot into arena
                    // if (coordinates.getX() >= 90) coordinates.setX(85);
                    // if (coordinates.getX() <= -90) coordinates.setX(-85);
                    // if (coordinates.getY() >= 90) coordinates.setY(85);
                    // if (coordinates.getY() <= -90) coordinates.setY(-85);

                    // rotate a little
                    motion.rotate(50 * sign, 500);

                }
            } else {
                motion.move(100, 100, 1000);
                delay(1000);
            }
        } else {
            // Task is completed, waiting
            delay(150);
        }
    }

    @Override
    public void sensorInterrupt(String sensor, String value) {

        switch (sensor) {
            case "distance":
                System.out.println("Distance sensor interrupt on " + id);
                break;

            case "color":
                System.out.println("Color sensor interrupt on " + id);
                break;

            case "proximity":
                System.out.println("Proximity sensor interrupt on " + id);
                break;

            default:
                System.out.println("Unknown sensor type");
        }
    }

    @Override
    public void communicationInterrupt(String msg) {

        // split the message
        String[] s = msg.split(" ");
        System.out.println("communicationInterrupt on " + id + " with msg:" + msg);

        if (s.length == 4) {
            int hopId = Integer.parseInt(s[0]);

            if (hopId > currentHopId) {
                int hopR = Integer.parseInt(s[1]);
                int hopG = Integer.parseInt(s[2]);
                int hopB = Integer.parseInt(s[3]);
                neoPixel.changeColor(hopR, hopG, hopB);
                currentHopId = hopId;
                colorUpdated = true;

                delay(2000);

                // Send it to the next robot
                simpleComm.sendMessage((hopId + 1) + " " + hopR + " " + hopG + " " + hopB, 120);

                searching = false;
                System.out.println("Send the received message...");
            }
        } else {
            System.out.println("Invalid msg received");
        }
    }

    public void reset() {
        neoPixel.changeColor(0, 0, 0);
        colorUpdated = false;
        currentHopId = -1;
        searching = true;
    }

    public void start() {
        super.start();

        // Things to do when start action
        neoPixel.changeColor(0, 0, 0);
        colorUpdated = false;
        currentHopId = -1;
        searching = true;
    }

}
