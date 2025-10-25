import java.io.Serializable;
import java.util.List;

public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        JOIN, RUN, STOP, WIN, POS, PLAYER_COUNT, GAME_START, GAME_STATE, PLAYER_INDEX, WINNER, JOIN_REJECTED
    }
    
    private MessageType type;
    private String playerId;
    private int playerCount;
    private boolean gameStarted;
    private int playerIndex;
    private int x, y;
    private boolean running;
    private List<PlayerData> players;
    private String winner;
    private int characterIndex;
    
    public GameMessage(MessageType type) {
        this.type = type;
    }
    
    public MessageType getType() { return type; }
    public String getPlayerId() { return playerId; }
    public int getPlayerCount() { return playerCount; }
    public boolean isGameStarted() { return gameStarted; }
    public int getPlayerIndex() { return playerIndex; }
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isRunning() { return running; }
    public List<PlayerData> getPlayers() { return players; }
    public String getWinner() { return winner; }
    public int getCharacterIndex() { return characterIndex; }
    
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }
    public void setGameStarted(boolean gameStarted) { this.gameStarted = gameStarted; }
    public void setPlayerIndex(int playerIndex) { this.playerIndex = playerIndex; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setRunning(boolean running) { this.running = running; }
    public void setPlayers(List<PlayerData> players) { this.players = players; }
    public void setWinner(String winner) { this.winner = winner; }
    public void setCharacterIndex(int characterIndex) { this.characterIndex = characterIndex; }
}
