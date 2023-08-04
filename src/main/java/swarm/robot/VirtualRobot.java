package swarm.robot;

/**
 * VirtualRobot implementation of the Robot
 * 
 * @author Nuwan Jaliyagoda
 */
public class VirtualRobot extends Robot {

    /**
     * VirtualRobot class
     * 
     * @param id      robot Id
     * @param x       X coordinate as double
     * @param y       Y coordinate as double
     * @param heading Heading direction in degrees, as double
     */
    public VirtualRobot(int id, double x, double y, double heading) {
        super(id, x, y, heading, 'V');
    }

    /**
     * Handles the event loop of the robot
     */
    @Override
    public void loop() throws Exception {

    }

    /**
     * Handles sensorInterrupt triggers of the robot
     */
    @Override
    public void sensorInterrupt(String sensor, String value) {
        switch (sensor) {
            case "distance":
                System.out.println("Distance sensor interrupt on " + id + "with value" + value);
                break;

            case "color":
                System.out.println("Color sensor interrupt on " + id + "with value" + value);
                break;

            case "proximity":
                System.out.println("Proximity sensor interrupt on " + id + "with value" + value);
                break;

            default:
                // TODO: make an exception other than println
                System.out.println("Unknown sensor type");
        }
    }

    /**
     * Handle communicationInterrupt triggers of the robot
     */
    @Override
    public void communicationInterrupt(String msg) {

    }

}