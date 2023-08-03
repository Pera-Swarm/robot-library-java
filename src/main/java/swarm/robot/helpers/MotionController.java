package swarm.robot.helpers;

import swarm.configs.RobotSettings;
import swarm.robot.exception.MotionControllerException;

/**
 * The class that calculate the robot motions
 * 
 * @author Nuwan Jaliyagoda
 */
public class MotionController {

    /**
     * This is the maximum duration allowed to coordinate calculation, smaller
     * values increase the smoothness of the movement, but more CPU time
     */
    static final private int maxDuration = 100;

    /* This is to be match with cm/s speed */
    static final private double speedFactor = 0.1;

    private final Coordinate c;

    private MotionController(Coordinate c) {
        this.c = c;
    }

    /**
     * Wrapper for void move(int, int, int)
     * 
     * @see MotionController#move(int, int, int)
     * @throws MotionControllerException
     */
    public void move(int leftSpeed, int rightSpeed) {
        move(leftSpeed, rightSpeed, maxDuration);
    }

    /**
     * Wrapper for void move(int, int, double)
     * 
     * @see MotionController#move(int, int, double)
     * @throws MotionControllerException
     */
    public void move(int leftSpeed, int rightSpeed, int duration) {
        move(leftSpeed, rightSpeed, (double) duration);
    }

    /**
     * Wrapper for void rotate(int, double)
     * 
     * @see MotionController#rotate(int, double)
     * @throws MotionControllerException
     */
    public void rotate(int speed) {
        rotate(speed, (double) maxDuration);
    }

    /**
     * Wrapper for void rotate(int, double)
     * 
     * @see MotionController#rotate(int, double)
     * @throws MotionControllerException
     */
    public void rotate(int speed, int duration) {
        rotate(speed, (double) duration);
    }

    /**
     * Rotate the robot in given speed for a given duration
     * 
     * @param speed    the value for speed, [ROBOT_SPEED_MIN, ROBOT_SPEED_MAX]
     * @param duration the duration, in ms
     * @throws MotionControllerException
     */
    public void rotate(int speed, double duration) {
        move(speed, -1 * speed, duration);
    }

    /**
     * Rotate to a given reltive angle in radians, with a given speed
     * 
     * @param speed  the value for speed, [ROBOT_SPEED_MIN, ROBOT_SPEED_MAX]
     * @param degree the relative degree to be rotate, positive means CW, [-180,
     *               180]
     * @throws MotionControllerException
     */
    public void rotateRadians(int speed, float radians) {
        rotateDegree(speed, (float) Math.toDegrees(radians));
    }

    /**
     * Rotate to a given reltive angle in degrees, with a given speed
     * 
     * @param speed  the value for speed, [ROBOT_SPEED_MIN, ROBOT_SPEED_MAX]
     * @param degree the relative degree to be rotate, positive means CW, [-180,
     *               180]
     * @throws MotionControllerException
     */
    public void rotateDegree(int speed, float degree) {
        try {
            if (degree == 0 || degree < -180 || degree > 180)
                throw new MotionControllerException("Degree should in range [-180, 180]");

            if (speed < RobotSettings.ROBOT_SPEED_MIN)
                throw new MotionControllerException("Speed should be greater than ROBOT_SPEED_MIN");

            int sign = (int) (degree / Math.abs(degree));
            double distance = (2 * Math.PI * RobotSettings.ROBOT_RADIUS * (Math.abs(degree) / 360)) * 10;
            float duration = (float) (distance / Math.abs(speed)) * 1000;
            debug("Sign: " + sign + " Distance: " + distance + " Duration: " + duration);

            rotate(sign * speed, duration);

        } catch (MotionControllerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Move the robot by given speed for a given distance
     * 
     * @param speed    the value for speed, [±ROBOT_SPEED_MIN, ±ROBOT_SPEED_MAX]
     * @param distance the distance as integer
     * @throws MotionControllerException
     */
    public void moveDistance(int speed, int distance) {
        moveDistance(speed, (double) distance);
    }

    /**
     * Move the robot by given speed for a given distance
     * 
     * @param speed    the value for speed, [±ROBOT_SPEED_MIN, ±ROBOT_SPEED_MAX]
     * @param distance the distance as double
     * @throws MotionControllerException
     */
    public void moveDistance(int speed, double distance) {
        // distance is relative displacement (in cm) from the current position
        double duration = (distance * 10 / Math.abs(speed)) * 1000; // in ms
        move(speed, speed, duration);
    }

    /**
     * The core move method
     * 
     * @param leftSpeed  left motor speed
     * @param rightSpeed right motor speed
     * @param duration   the duration, in ms
     * @throws MotionControllerException
     */
    public void move(int leftSpeed, int rightSpeed, double duration) {
        if (isSpeedInRange(leftSpeed) && isSpeedInRange(rightSpeed)) {

            int steps = Math.max(1, (int) Math.ceil(duration / maxDuration));
            int stepInterval = (int) duration / steps;
            int cumulativeInterval = 0;
            debug("Calculate movement using " + steps + " steps, each has " + stepInterval + " intervals", 1);

            for (int j = 0; j < steps; j++) {
                double dL = leftSpeed * speedFactor * (stepInterval / 1000.0);
                double dR = rightSpeed * speedFactor * (stepInterval / 1000.0);
                double d = (dL + dR) / 2.0;
                double h = c.getHeadingRad();

                double x = c.getX() + d * Math.cos(h);
                double y = c.getY() + d * Math.sin(h);
                double heading = (c.getHeadingRad() + (dR - dL) / (RobotSettings.ROBOT_WIDTH)); // in radians

                c.setCoordinate(x, y, Math.toDegrees(heading));

                cumulativeInterval += stepInterval;
                if (cumulativeInterval >= 500) {
                    debug("Adding extra delay of " + cumulativeInterval);
                    delay(cumulativeInterval - 500);
                    c.publishCoordinate();
                    cumulativeInterval -= 500;
                }
            }

            // Round to the nearest int > To be tested
            c.setCoordinate(Math.round(c.getX()), Math.round(c.getY()), Math.round(c.getHeading()));

            c.publishCoordinate(); // Publish the coordinate info through MQTT to the simulator

        } else {
            try {
                throw new MotionControllerException("One of the provided speeds is out of range in move() function");
            } catch (MotionControllerException motionControllerException) {
                motionControllerException.printStackTrace();
            }
        }
    }

    /**
     * Validate the robot move speed
     * 
     * @param speed speed, [ROBOT_SPEED_MIN, ROBOT_SPEED_MAX]
     * @return True if the speed in the allowed range
     */
    private boolean isSpeedInRange(int speed) {
        if (speed > 0) {
            return (speed >= RobotSettings.ROBOT_SPEED_MIN && speed <= RobotSettings.ROBOT_SPEED_MAX);
        } else if (speed < 0) {
            return (speed <= -1 * RobotSettings.ROBOT_SPEED_MIN && speed >= -1 * RobotSettings.ROBOT_SPEED_MAX);
        }
        return true; // 0 is acceptable
    }

    /**
     * Internal delay method
     * 
     * @param duration duration in ms
     * @throws InterruptedException
     */
    private void delay(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Debug support function
     * 
     * @param msg Debug Message
     * @throws InterruptedException
     */
    private static void debug(String msg) {
        debug(msg, 0);
    }

    /**
     * Debug support function
     * 
     * @param msg   Debug Message
     * @param level Debug level
     * @throws InterruptedException
     */
    private static void debug(String msg, int level) {
        if (level > 0) {
            System.out.println("[DEBUG]\t" + msg);
        }
    }

    // TODO
    // private double PID(double e) {
    // return 1 * e;
    // }

    // TODO
    // public boolean goToGoal(double targetX, double targetY, int velocity) {
    // return goToGoal(targetX, targetY, velocity, maxDuration);
    // }

    // TODO
    // public boolean goToGoal(double targetX, double targetY, int velocity, int
    // duration) {

    // double x = c.getX();
    // double y = c.getY();
    // double heading = c.getHeadingRad();
    // double dx = targetX - x;
    // double dy = targetY - y;
    // double phiD = Math.atan2(dy, dx);
    // double w = 0.2 * (phiD - heading);

    // debug("dx:" + dx + " dy:" + dy + " head:" + Math.toDegrees(heading) + " w:" +
    // Math.toDegrees(w) + " phiD:"
    // + phiD);

    // c.setX(x + 5 * Math.cos(w));
    // c.setY(y + 5 * Math.sin(w));
    // c.setHeadingRad(heading + w);

    // // Publish the coordinate info through MQTT to the simulator
    // c.publishCoordinate();

    // return (c.getX() == targetX && c.getY() == targetY);
    // }

    // TODO
    // private static double getSlope(double x1, double y1, double x2, double y2) {
    // double dx = x2 - x1;
    // double dy = y2 - y1;
    // return Math.toDegrees(Math.atan2(dy, dx));
    // }
}