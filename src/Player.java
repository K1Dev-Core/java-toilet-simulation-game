import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int x, y;
    private List<BufferedImage> walkFrames;
    private List<BufferedImage> stopFrames;
    private int currentFrame;
    private long lastFrameTime;
    private int frameDelay = 150;
    private int displayWidth = 72;
    private int displayHeight = 72;
    private boolean isRunning;
    private int speed = 5;
    private String id;
    private int stepsRemaining = 0;
    private int targetX, targetY;
    private boolean targetRunning;
    private long lastUpdateTime;
    private static final float INTERPOLATION_SPEED = 0.3f;
    
    public Player(int x, int y, BufferedImage spriteSheet) {
        this.x = x;
        this.y = y;
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.isRunning = false;
        this.id = "Player1";
        this.targetX = x;
        this.targetY = y;
        this.targetRunning = false;
        this.lastUpdateTime = System.currentTimeMillis();
        
        this.walkFrames = new ArrayList<>();
        this.stopFrames = new ArrayList<>();
        loadAnimations();
    }
    
    public Player(int x, int y, int characterIndex) {
        this.x = x;
        this.y = y;
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.isRunning = false;
        this.id = "Player1";
        this.targetX = x;
        this.targetY = y;
        this.targetRunning = false;
        this.lastUpdateTime = System.currentTimeMillis();
        
        this.walkFrames = new ArrayList<>();
        this.stopFrames = new ArrayList<>();
        loadAnimations(characterIndex);
    }
    
    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.isRunning = false;
        this.id = "Player1";
        this.targetX = x;
        this.targetY = y;
        this.targetRunning = false;
        this.lastUpdateTime = System.currentTimeMillis();
        
        this.walkFrames = new ArrayList<>();
        this.stopFrames = new ArrayList<>();
        loadAnimations(0);
    }
    
    private void loadAnimations() {
        loadAnimations(0);
    }
    
    private int currentCharacterIndex = -1;
    
    public void loadAnimations(int characterIndex) {
        if (currentCharacterIndex == characterIndex) {
            return;
        }
        currentCharacterIndex = characterIndex;
        
        walkFrames.clear();
        stopFrames.clear();
        
        try {
            String characterFolder;
            if (characterIndex == 0) {
                characterFolder = "d1";
            } else if (characterIndex == 1) {
                characterFolder = "d2";
            } else if (characterIndex == 2) {
                characterFolder = "d3";
            } else {
                characterFolder = "d4";
            }
            
            for (int i = 1; i <= 7; i++) {
                File file = new File("assets/DinoSprites/" + characterFolder + "/walk/" + i + ".png");
                if (file.exists()) {
                    BufferedImage img = ImageIO.read(file);
                    walkFrames.add(img);
                }
            }
            
            int stopFrameCount = 4;
            for (int i = 1; i <= stopFrameCount; i++) {
                File file = new File("assets/DinoSprites/" + characterFolder + "/stop/" + i + ".png");
                if (file.exists()) {
                    BufferedImage img = ImageIO.read(file);
                    stopFrames.add(img);
                }
            }
            
        } catch (Exception e) {
        }
    }
    
    public void updateTargetPosition(int x, int y, boolean running) {
        this.targetX = x;
        this.targetY = y;
        this.targetRunning = running;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public void update() {
        updateAnimation();
        
        if (stepsRemaining > 0) {
            x += speed;
            stepsRemaining--;
            isRunning = true;
            if (stepsRemaining == 0) {
                isRunning = false;
            }
        }
    }
    
    public void updateWithInterpolation() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        
        if (deltaTime > 0.02f) {
            deltaTime = 0.02f;
        }
        
        float lerpFactor = INTERPOLATION_SPEED * deltaTime * 50;
        
        this.x = (int) (this.x + (targetX - this.x) * lerpFactor);
        this.y = (int) (this.y + (targetY - this.y) * lerpFactor);
        this.isRunning = targetRunning;
        
        updateAnimation();
    }
    
    public void step() {
        stepsRemaining = 3;
    }
    
    public void startRunning() {
        isRunning = true;
    }
    
    public void stopRunning() {
        isRunning = false;
    }
    
    public boolean checkCollision(ToiletObject toilet) {
        int distance = (int) Math.sqrt(Math.pow(x - toilet.getX(), 2) + Math.pow(y - toilet.getY(), 2));
        return distance < 50;
    }
    
    public void render(Graphics2D g2d) {
        updateAnimation();
        
        BufferedImage currentSprite = getCurrentSprite();
        if (currentSprite != null) {
            g2d.drawImage(currentSprite, x - displayWidth/2, y - displayHeight/2, displayWidth, displayHeight, null);
        }
    }
    
    private BufferedImage getCurrentSprite() {
        List<BufferedImage> frames = isRunning ? walkFrames : stopFrames;
        if (frames.isEmpty()) {
            return null;
        }
        
        int frameIndex = currentFrame % frames.size();
        return frames.get(frameIndex);
    }
    
    private void updateAnimation() {
        List<BufferedImage> frames = isRunning ? walkFrames : stopFrames;
        if (frames.isEmpty()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > frameDelay) {
            currentFrame = (currentFrame + 1) % frames.size();
            lastFrameTime = currentTime;
        }
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void setRunning(boolean running) {
        this.isRunning = running;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
}
