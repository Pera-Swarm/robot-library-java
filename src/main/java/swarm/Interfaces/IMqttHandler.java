package swarm.Interfaces;

import swarm.mqtt.MqttMsg;
import swarm.robot.Robot;

public interface IMqttHandler {

    public void handleSubscription(Robot r, MqttMsg m);

}
