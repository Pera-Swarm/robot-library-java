package swarm;

import swarm.robot.Robot;
import swarm.robot.VirtualRobot;
import swarm.robot.communication.Communication;
import swarm.robot.exception.RGBColorException;
import swarm.robot.types.RGBColorType;
import swarm.robotImplementations.DiscoverColorRobot;
import swarm.robotImplementations.ObstacleAvoidRobot;

public class Swarm extends Thread {

    public static void main(String[] args) throws RGBColorException {

        // int[] robotList = {10, 11, 12, 13, 14};
        // int[] robotList = {10};

        // Linear Robot Formation
        // lineFormation(robotList, -90, 75, 90, 35, 0);

        // Circular Robot formation
//        circularFormation(robotList, 0, 0, 0, 60, 15, 45);

        // Spiral Robot Formation
        //spiralFormation(robotList, 0, 0, 90, 40, 12, 90, 30);

        //Discover colored obstacles
        //RGBColorType obstacleColor = new RGBColorType(255,0,0);
        //discoverColor(robotList,0,0,0,60,15,obstacleColor);

        discoverColor();
    }

    private static void discoverColor() {

        Robot[] vr = new VirtualRobot[5];
        RGBColorType obstacleColor = null;

        try {
            obstacleColor = new RGBColorType(255,0,0);
        } catch (RGBColorException e) {
            e.printStackTrace();
        }

        vr[0] = new DiscoverColorRobot(0, 52, 32, 45, obstacleColor);
        vr[1] = new DiscoverColorRobot(1, -32, 64, -20, obstacleColor);
        vr[2] = new DiscoverColorRobot(2, 49, -23, 3, obstacleColor);
        vr[3] = new DiscoverColorRobot(3, 5, 76, -70, obstacleColor);
        vr[4] = new DiscoverColorRobot(4, -69, 54, -30, obstacleColor);

        //robots start to move
        for (Robot robot : vr) {
//            vr[i] = new DiscoverColorRobot(robotList[i], startX + incX * i, startY + incY * i, heading);
//            vr[i].discover(obstacleColor);
            new Thread(robot).start();
        }
    }

    private static void lineFormation(int[] robotList, int startX, int startY, int heading, int incX, int incY) {
        Robot[] vr = new VirtualRobot[robotList.length];

        for (int i = 0; i < robotList.length; i++) {
            vr[i] = new ObstacleAvoidRobot(robotList[i], startX + incX * i, startY + incY * i, heading);
            new Thread(vr[i]).start();
        }
    }

    private static void circularFormation(int[] robotList, int centerX, int centerY, int headingOffset, int radius, int startAngle, int deltaAngle) {
        Robot[] vr = new VirtualRobot[robotList.length];

        int x, y, robotHeading;

        for (int i = 0; i < robotList.length; i++) {
            double a = (startAngle + i * deltaAngle);
            x = (int) (radius * Math.cos(a * Math.PI / 180));
            y = (int) (radius * Math.sin(a * Math.PI / 180));
            robotHeading = (int) (a + headingOffset);

            vr[i] = new ObstacleAvoidRobot(robotList[i], x, y, robotHeading);
            new Thread(vr[i]).start();
        }
    }

    private static void spiralFormation(int[] robotList, int centerX, int centerY, int headingOffset, int startRadius, int deltaRadius, int startAngle, int deltaAngle) {
        Robot[] vr = new VirtualRobot[robotList.length];

        int x, y, robotHeading;

        for (int i = 0; i < robotList.length; i++) {
            double a = (startAngle + i * deltaAngle);
            double r = startRadius + (deltaRadius * i);
            x = (int) (r * Math.cos(a * Math.PI / 180));
            y = (int) (r * Math.sin(a * Math.PI / 180));
            robotHeading = (int) (a + headingOffset);

            vr[i] = new VirtualRobot(robotList[i], x, y, robotHeading);
            new Thread(vr[i]).start();
        }
    }
}