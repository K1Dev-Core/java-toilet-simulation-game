import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameState {
    private List<Player> players;
    private String winner;
    private int playerCount;
    private boolean gameStarted;
    private Map<String, Integer> playerIndices;
    
    public GameState() {
        players = new ArrayList<>();
        winner = null;
        playerCount = 0;
        gameStarted = false;
        playerIndices = new HashMap<>();
    }
    
    public synchronized void addPlayer(Player player) {
        boolean exists = false;
        for (Player p : players) {
            if (p.getId().equals(player.getId())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            players.add(player);
        }
    }
    
    public synchronized void updatePlayerPosition(String playerId, int x, int y, boolean running) {
        for (Player player : players) {
            if (player.getId().equals(playerId)) {
                player.setPosition(x, y);
                player.setRunning(running);
                return;
            }
        }
    }
    
    public synchronized void updateOrCreatePlayer(String playerId, int x, int y, boolean running, java.awt.image.BufferedImage spriteSheet) {
        Player foundPlayer = null;
        for (Player player : players) {
            if (player.getId().equals(playerId)) {
                foundPlayer = player;
                break;
            }
        }
        
        if (foundPlayer != null) {
            foundPlayer.setPosition(x, y);
            foundPlayer.setRunning(running);
        } else {
            Player newPlayer = new Player(x, y);
            newPlayer.setId(playerId);
            newPlayer.setRunning(running);
            players.add(newPlayer);
        }
    }
    
    public synchronized void updateOrCreatePlayer(String playerId, int x, int y, boolean running, int characterIndex) {
        Player foundPlayer = null;
        for (Player player : players) {
            if (player.getId().equals(playerId)) {
                foundPlayer = player;
                break;
            }
        }
        
        if (foundPlayer != null) {
            foundPlayer.updateTargetPosition(x, y, running);
            foundPlayer.loadAnimations(characterIndex);
        } else {
            Player newPlayer = new Player(x, y, characterIndex);
            newPlayer.setId(playerId);
            newPlayer.updateTargetPosition(x, y, running);
            players.add(newPlayer);
        }
    }
    
    public synchronized List<Player> getOtherPlayers() {
        return new ArrayList<>(players);
    }
    
    public synchronized Player getPlayer(String playerId) {
        for (Player player : players) {
            if (player.getId().equals(playerId)) {
                return player;
            }
        }
        return null;
    }
    
    public synchronized void removePlayer(String playerId) {
        players.removeIf(p -> p.getId().equals(playerId));
        playerIndices.remove(playerId);
    }
    
    public synchronized void clearPlayers() {
        players.clear();
        playerIndices.clear();
    }
    
    public synchronized void setWinner(String playerId) {
        if (winner == null && playerId != null) {
            winner = playerId;
        }
    }
    
    public synchronized String getWinner() {
        return winner;
    }
    
    public synchronized void setPlayerCount(int count) {
        if (count != this.playerCount) {
            this.playerCount = count;
        }
    }
    
    public synchronized int getPlayerCount() {
        return playerCount;
    }
    
    public synchronized void setGameStarted(boolean started) {
        if (started != this.gameStarted) {
            this.gameStarted = started;
            if (started) {
                winner = null;
            }
        }
    }
    
    public synchronized boolean isGameStarted() {
        return gameStarted;
    }
    
    public synchronized void setPlayerIndex(String playerId, int playerIndex) {
        playerIndices.put(playerId, playerIndex);
    }
    
    public synchronized int getPlayerIndex(String playerId) {
        return playerIndices.getOrDefault(playerId, -1);
    }
    
    public synchronized int getTotalPlayers() {
        return players.size();
    }
}
