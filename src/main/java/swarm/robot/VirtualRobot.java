package swarm.robot;

public class VirtualRobot extends Robot {

    public VirtualRobot(int id, double x, double y, double heading) {
        super(id, x, y, heading, 'V');
    }

    @Override
    public void loop() throws Exception {

    }

    @Override
    public void interrupt() {

    }

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

    @Override
    public void communicationInterrupt(String msg) {

    }

}