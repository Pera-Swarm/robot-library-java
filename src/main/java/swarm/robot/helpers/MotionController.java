package swarm.robot.helpers;

import swarm.configs.RobotSettings;
import swarm.robot.exception.MotionControllerException;

public class MotionController {

    // This is the maximum interval allowed to coordinate calculation, smaller
    // values increase the smoothness of the movement
    static final private int maxInterval = 100;

    // REM: Obtain this by an experiment
    static final private double speedFactor = 0.05; // to be match with cm/s speed

    private final Coordinate c;

    public MotionController(Coordinate c) {
        this.c = c;
    }

    // Simplified functions --------------

    public void rotate(int speed) {
        rotate(speed, 1000);
    }

    public void rotate(int speed, int interval) {
        move(speed, -1 * speed, interval);
    }

    public void move(int leftSpeed, int rightSpeed) {
        move(leftSpeed, rightSpeed, maxInterval);
    }

    // -----------------------------------

    // TODO: implement this
    public void rotateDegree(int speed, float degree) {
        // degree is relative angle displacement from the current heading
        // prepare a mathematical equation to translate speed and degree into
        // the function 'public void move(int leftSpeed, int rightSpeed, int interval)'
    }

    // TODO: implement & test this
    public void moveDistance(int speed, float distance) {
        // distance is relative displacement (in cm) from the current position
        int interval = (int) Math.ceil((double) distance / speed);
        move(speed, speed, interval);
    }

    // -----------------------------------
    public void move(int leftSpeed, int rightSpeed, int interval) {
        if (isSpeedInRange(leftSpeed) && isSpeedInRange(rightSpeed)) {

            int steps = (int) Math.ceil((double) interval / maxInterval);
            int stepInterval = interval / steps;

            int cumulativeInterval = 0;

            debug("Move using " + steps + " steps, each has " + stepInterval + " intervals");

            for (int j = 0; j < steps; j++) {
                double dL = leftSpeed * speedFactor * (stepInterval / 1000.0);
                double dR = rightSpeed * speedFactor * (stepInterval / 1000.0);
                double d = (dL + dR) / 2.0;
                double h = c.getHeadingRad();

                double x = c.getX() + d * Math.cos(h);
                double y = c.getY() + d * Math.sin(h);
                double heading = (c.getHeadingRad() + (dR - dL) / (RobotSettings.ROBOT_WIDTH)); // in radians

                c.setCoordinate(x, y, Math.toDegrees(heading));

                // Any two coordinate transmissions should have a gap of 1000ms
                cumulativeInterval += stepInterval;

                if (cumulativeInterval >= 2000) {
                    debug("Adding extra delay, " + cumulativeInterval);

                    c.publishCoordinate();
                    delay(cumulativeInterval - 1000);
                    cumulativeInterval -= 1000;
                }
            }
            c.publishCoordinate(); // Publish the coordinate info through MQTT to the simulator

        } else {
            try {
                throw new MotionControllerException("One of the provided speeds is out of range in move() function");
            } catch (MotionControllerException motionControllerException) {
                motionControllerException.printStackTrace();
            }
        }
    }

    public boolean goToGoal(double targetX, double targetY, int velocity) {
        return goToGoal(targetX, targetY, velocity, maxInterval);
    }

    // TODO: Not fully implemented
    public boolean goToGoal(double targetX, double targetY, int velocity, int interval) {

        double x = c.getX();
        double y = c.getY();
        double heading = c.getHeadingRad();
        double dx = targetX - x;
        double dy = targetY - y;
        double phiD = Math.atan2(dy, dx);
        double w = 0.2 * (phiD - heading);

        debug("dx:" + dx + " dy:" + dy + " head:" + Math.toDegrees(heading) + " w:" + Math.toDegrees(w) + " phiD:"
                + phiD);

        c.setX(x + 5 * Math.cos(w));
        c.setY(y + 5 * Math.sin(w));
        c.setHeadingRad(heading + w);

        // Publish the coordinate info through MQTT to the simulator
        c.publishCoordinate();

        return (c.getX() == targetX && c.getY() == targetY);
    }

    // validate speeds to be between [-255, 255]
    public boolean isSpeedInRange(int speed) {
        if (speed > 0) {
            return (speed >= RobotSettings.ROBOT_SPEED_MIN && speed <= RobotSettings.ROBOT_SPEED_MAX);
        } else if (speed < 0) {
            return (speed <= -1 * RobotSettings.ROBOT_SPEED_MIN && speed >= -1 * RobotSettings.ROBOT_SPEED_MAX);
        }

        // 0 speed is acceptable
        return true;
    }

    // TODO: Implement this 
    private double PID(double e) {
        return 1 * e;
    }

    private void delay(int interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static double getSlope(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.toDegrees(Math.atan2(dy, dx));
    }

    public static void debug(String msg) {
        debug(msg, 0);
    }

    public static void debug(String msg, int level) {
        if (level > 1) {
            System.out.println(msg);
        }
    }

}