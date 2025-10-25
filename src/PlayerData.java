import java.io.Serializable;

public class PlayerData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private int x, y;
    private boolean running;
    private int characterIndex;
    
    public PlayerData(String id, int x, int y, boolean running) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.running = running;
        this.characterIndex = 0;
    }
    
    public PlayerData(String id, int x, int y, boolean running, int characterIndex) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.running = running;
        this.characterIndex = characterIndex;
    }
    
    public String getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isRunning() { return running; }
    public int getCharacterIndex() { return characterIndex; }
    
    public void setRunning(boolean running) { this.running = running; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setCharacterIndex(int characterIndex) { this.characterIndex = characterIndex; }
    
    public void move() {
        if (running) {
            x += Config.RUNNING_SPEED;
        }
    }
}
