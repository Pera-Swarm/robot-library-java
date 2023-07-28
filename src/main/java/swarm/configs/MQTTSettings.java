package swarm.configs;

public class MQTTSettings {

    public static String server;
    public static int port = 1883;
    public static String userName;
    public static String password;
    public static String channel;

    public static void print() {
        System.out.println("server: " + server);
        System.out.println("port: " + port);
        System.out.println("username: " + userName);
        System.out.println("password: " + password);
        System.out.println("channel: " + channel);
    }
}
