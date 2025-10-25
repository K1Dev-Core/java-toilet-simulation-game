import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {
    private DatagramSocket serverSocket;
    private int port;
    private boolean running;
    private Map<String, PlayerData> players;
    private Map<String, Integer> clientPorts;
    private Map<String, InetAddress> clientAddresses;
    private boolean gameStarted = false;
    private int maxPlayers;
    private int minPlayersToStart;
    private long lastBroadcastTime;
    private static final long BROADCAST_INTERVAL = 5;
    
    public GameServer() {
        this.port = Config.SERVER_PORT;
        this.maxPlayers = Config.MAX_PLAYERS;
        this.minPlayersToStart = Config.MIN_PLAYERS_TO_START;
        this.lastBroadcastTime = System.currentTimeMillis();
        
        try {
            serverSocket = new DatagramSocket(port);
            players = new ConcurrentHashMap<>();
            clientPorts = new ConcurrentHashMap<>();
            clientAddresses = new ConcurrentHashMap<>();
            running = false;
        } catch (SocketException e) {
            for (int i = 1; i <= 10; i++) {
                try {
                    port = Config.SERVER_PORT + i;
                    serverSocket = new DatagramSocket(port);
                    players = new ConcurrentHashMap<>();
                    clientPorts = new ConcurrentHashMap<>();
                    clientAddresses = new ConcurrentHashMap<>();
                    running = false;
                    break;
                } catch (SocketException ex) {
                    if (i == 10) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    public void start() {
        if (serverSocket == null) {
            return;
        }
        running = true;
        
        Thread broadcastThread = new Thread(() -> {
            while (running) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastBroadcastTime >= BROADCAST_INTERVAL) {
                    if (gameStarted && players.size() > 0) {
                        broadcastGameState();
                    }
                    lastBroadcastTime = currentTime;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        broadcastThread.start();
        
        Thread serverThread = new Thread(() -> {
            while (running) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    serverSocket.receive(packet);
                    
                    byte[] receivedData = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), 0, receivedData, 0, packet.getLength());
                    handleClientMessage(receivedData, packet.getAddress(), packet.getPort());
                    
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }
        });
        serverThread.start();
    }
    
    private void handleClientMessage(byte[] data, InetAddress clientAddress, int clientPort) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            GameMessage gameMessage = (GameMessage) ois.readObject();
            
            String playerId = gameMessage.getPlayerId();
            
            clientAddresses.put(playerId, clientAddress);
            if (!clientPorts.containsKey(playerId)) {
                clientPorts.put(playerId, clientPort);
            }
            
            switch (gameMessage.getType()) {
                case JOIN:
                    if (gameStarted) {
                        sendJoinRejected(playerId, "Game already started");
                        break;
                    }
                    
                    if (players.size() >= maxPlayers) {
                        sendJoinRejected(playerId, "Server is full");
                        break;
                    }
                    
                    clientPorts.put(playerId, clientPort);
                    addNewPlayer(playerId, gameMessage.getCharacterIndex());
                    broadcastGameState();
                    break;
                case RUN:
                    updatePlayerState(playerId, true);
                    break;
                case STOP:
                    updatePlayerState(playerId, false);
                    break;
                case WIN:
                    broadcastWinner(playerId);
                    break;
                case POS:
                    int x = gameMessage.getX();
                    int y = gameMessage.getY();
                    boolean running = gameMessage.isRunning();
                    updatePlayerPosition(playerId, x, y, running);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void addNewPlayer(String playerId, int characterIndex) {
        if (!players.containsKey(playerId)) {
            int[] yPositions = {110, 260, 400, 550};
            int playerIndex = players.size();
            int y = (playerIndex < yPositions.length) ? yPositions[playerIndex] : 110;
            
            players.put(playerId, new PlayerData(playerId, 30, y, false, characterIndex));
            broadcastPlayerCount();
            broadcastPlayerIndex(playerId, playerIndex);
        }
    }
    
    private void broadcastPlayerCount() {
        try {
            GameMessage message = new GameMessage(GameMessage.MessageType.PLAYER_COUNT);
            message.setPlayerCount(players.size());
            
            byte[] data = serializeMessage(message);
            
            for (String playerId : clientPorts.keySet()) {
                sendToClient(playerId, data);
            }
            
            if (players.size() >= minPlayersToStart && !gameStarted) {
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        if (!gameStarted && players.size() >= minPlayersToStart) {
                            gameStarted = true;
                            GameMessage startMessage = new GameMessage(GameMessage.MessageType.GAME_START);
                            try {
                                byte[] startData = serializeMessage(startMessage);
                                
                                for (String pid : clientPorts.keySet()) {
                                    sendToClient(pid, startData);
                                }
                            } catch (IOException ioException) {
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void broadcastPlayerIndex(String playerId, int playerIndex) {
        try {
            GameMessage message = new GameMessage(GameMessage.MessageType.PLAYER_INDEX);
            message.setPlayerId(playerId);
            message.setPlayerIndex(playerIndex);
            
            byte[] data = serializeMessage(message);
            sendToClient(playerId, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updatePlayerState(String playerId, boolean isRunning) {
        PlayerData playerData = players.get(playerId);
        if (playerData != null) {
            playerData.setRunning(isRunning);
            if (isRunning) {
                playerData.move();
            }
        }
    }
    
    private void updatePlayerPosition(String playerId, int x, int y, boolean running) {
        PlayerData playerData = players.get(playerId);
        if (playerData != null) {
            playerData.setPosition(x, y);
            playerData.setRunning(running);
        } else {
            players.put(playerId, new PlayerData(playerId, x, y, running, 0));
        }
    }
    
    private void broadcastGameState() {
        try {
            if (players.isEmpty()) {
                return;
            }
            
            GameMessage message = new GameMessage(GameMessage.MessageType.GAME_STATE);
            message.setPlayers(new ArrayList<>(players.values()));
            
            byte[] data = serializeMessage(message);
            
            for (String playerId : clientPorts.keySet()) {
                sendToClient(playerId, data);
            }
        } catch (Exception e) {
        }
    }
    
    private void broadcastWinner(String winnerId) {
        try {
            GameMessage message = new GameMessage(GameMessage.MessageType.WINNER);
            message.setWinner(winnerId);
            
            byte[] data = serializeMessage(message);
            
            for (String playerId : clientPorts.keySet()) {
                sendToClient(playerId, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void sendJoinRejected(String playerId, String reason) {
        try {
            GameMessage message = new GameMessage(GameMessage.MessageType.JOIN_REJECTED);
            message.setPlayerId(playerId);
            message.setWinner(reason);
            
            byte[] data = serializeMessage(message);
            sendToClient(playerId, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private byte[] serializeMessage(GameMessage message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(message);
        oos.flush();
        return baos.toByteArray();
    }
    
    private void sendToClient(String playerId, byte[] data) {
        try {
            Integer clientPort = clientPorts.get(playerId);
            InetAddress clientAddress = clientAddresses.get(playerId);
            
            if (clientPort != null && clientAddress != null) {
                DatagramPacket packet = new DatagramPacket(data, data.length, clientAddress, clientPort);
                serverSocket.send(packet);
            }
        } catch (IOException e) {
        }
    }
    
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
    
    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
        
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        server.stop();
        scanner.close();
    }
}
