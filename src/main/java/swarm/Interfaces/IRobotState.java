package swarm.Interfaces;

public interface IRobotState {
    enum robotState {
        WAIT, RUN, BEGIN
    }

    public void loop() throws Exception;

    public void interrupt();

    public void sensorInterrupt(String sensor, String value);

    public void communicationInterrupt(String msg);

    public void start();

    public void stop();

    public void reset();
}
