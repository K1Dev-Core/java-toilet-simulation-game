import java.io.*;
import java.net.*;

public class NetworkManager {
    private DatagramSocket socket;
    private String playerId;
    private GameState gameState;
    private boolean connected = false;
    private int serverPort = 12348;
    private int selectedCharacterIndex;
    
    public NetworkManager(GameState gameState, java.awt.image.BufferedImage spriteSheet) {
        this.gameState = gameState;
        this.playerId = "Player" + System.currentTimeMillis();
        this.selectedCharacterIndex = 0;
        
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(50);
            
            Thread receiver = new Thread(() -> {
                byte[] buffer = new byte[4096];
                while (true) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        
                        if (!connected) {
                            serverPort = packet.getPort();
                            connected = true;
                        }
                        
                        handle(packet);
                        
                    } catch (SocketTimeoutException e) {
                    } catch (IOException e) {
                        break;
                    }
                }
            });
            receiver.setDaemon(true);
            receiver.start();
            
            new Thread(() -> {
                while (!connected) {
                    try {
                        for (int p = 12348; p <= 12358 && !connected; p++) {
                            GameMessage msg = new GameMessage(GameMessage.MessageType.JOIN);
                            msg.setPlayerId(playerId);
                            msg.setCharacterIndex(selectedCharacterIndex);
                            
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ObjectOutputStream oos = new ObjectOutputStream(baos);
                            oos.writeObject(msg);
                            byte[] data = baos.toByteArray();
                            
                            DatagramPacket packet = new DatagramPacket(
                                data, data.length,
                                InetAddress.getByName("localhost"),
                                p
                            );
                            socket.send(packet);
                        }
                        Thread.sleep(500);
                    } catch (Exception e) {
                        break;
                    }
                }
            }).start();
            
        } catch (Exception e) {
        }
    }
    
    public void sendJoin() {
        if (!connected) {
            new Thread(() -> {
                while (!connected) {
                    try {
                        for (int p = 12348; p <= 12358 && !connected; p++) {
                            GameMessage msg = new GameMessage(GameMessage.MessageType.JOIN);
                            msg.setPlayerId(playerId);
                            msg.setCharacterIndex(selectedCharacterIndex);
                            
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ObjectOutputStream oos = new ObjectOutputStream(baos);
                            oos.writeObject(msg);
                            byte[] data = baos.toByteArray();
                            
                            DatagramPacket packet = new DatagramPacket(
                                data, data.length,
                                InetAddress.getByName("localhost"),
                                p
                            );
                            socket.send(packet);
                        }
                        Thread.sleep(500);
                    } catch (Exception e) {
                        break;
                    }
                }
            }).start();
        }
    }
    
    public void sendRun() {
        if (!connected) return;
        try {
            GameMessage msg = new GameMessage(GameMessage.MessageType.RUN);
            msg.setPlayerId(playerId);
            send(msg);
        } catch (Exception e) {}
    }
    
    public void sendStop() {
        if (!connected) return;
        try {
            GameMessage msg = new GameMessage(GameMessage.MessageType.STOP);
            msg.setPlayerId(playerId);
            send(msg);
        } catch (Exception e) {}
    }
    
    public void sendPosition(int x, int y, boolean running) {
        if (!connected) return;
        try {
            GameMessage msg = new GameMessage(GameMessage.MessageType.POS);
            msg.setPlayerId(playerId);
            msg.setPosition(x, y);
            msg.setRunning(running);
            send(msg);
        } catch (Exception e) {}
    }
    
    public void sendWin() {
        if (!connected) return;
        try {
            GameMessage msg = new GameMessage(GameMessage.MessageType.WIN);
            msg.setPlayerId(playerId);
            send(msg);
        } catch (Exception e) {}
    }
    
    private void send(GameMessage msg) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(msg);
        byte[] data = baos.toByteArray();
        
        DatagramPacket packet = new DatagramPacket(
            data, data.length,
            InetAddress.getByName("localhost"),
            serverPort
        );
        
        socket.send(packet);
    }
    
    private void handle(DatagramPacket packet) {
        try {
            byte[] data = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
            
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            GameMessage msg = (GameMessage) ois.readObject();
            
            switch (msg.getType()) {
                case PLAYER_COUNT:
                    gameState.setPlayerCount(msg.getPlayerCount());
                    break;
                case PLAYER_INDEX:
                    gameState.setPlayerIndex(msg.getPlayerId(), msg.getPlayerIndex());
                    break;
                case GAME_START:
                    gameState.setGameStarted(true);
                    break;
                case GAME_STATE:
                    java.util.List<PlayerData> players = msg.getPlayers();
                    if (players != null) {
                        for (PlayerData p : players) {
                            if (!p.getId().equals(playerId)) {
                                gameState.updateOrCreatePlayer(p.getId(), p.getX(), p.getY(), p.isRunning(), p.getCharacterIndex());
                            }
                        }
                    }
                    break;
                case WINNER:
                    gameState.setWinner(msg.getWinner());
                    break;
                case JOIN:
                case RUN:
                case STOP:
                case POS:
                case WIN:
                case JOIN_REJECTED:
                default:
                    break;
            }
        } catch (Exception e) {
        }
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void setSelectedCharacterIndex(int characterIndex) {
        this.selectedCharacterIndex = characterIndex;
    }
}
