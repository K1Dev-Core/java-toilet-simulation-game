public class Config {
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 12348;
    public static final int MAX_PLAYERS = 4;
    public static final int MIN_PLAYERS_TO_START = 3;
    public static final int RUNNING_SPEED = 10;
    
    public static final int CLIENT_TIMEOUT = 5000;
    public static final int CLIENT_PORT_RANGE_START = 50000;
    public static final int CLIENT_PORT_RANGE_END = 60000;
    
    public static final String GAME_NAME = "Toilet Simulation";
    public static final String GAME_VERSION = "1.0";
    
    public static void printConfig() {
        System.out.println("=== Current Configuration ===");
        System.out.println("Server IP: " + SERVER_IP);
        System.out.println("Server Port: " + SERVER_PORT);
        System.out.println("Max Players: " + MAX_PLAYERS);
        System.out.println("Min Players to Start: " + MIN_PLAYERS_TO_START);
        System.out.println("Running Speed: " + RUNNING_SPEED);
        System.out.println("Client Port Range: " + CLIENT_PORT_RANGE_START + "-" + CLIENT_PORT_RANGE_END);
        System.out.println("Client Timeout: " + CLIENT_TIMEOUT + "ms");
        System.out.println("Game Name: " + GAME_NAME);
        System.out.println("Game Version: " + GAME_VERSION);
        System.out.println("==============================");
    }
}
