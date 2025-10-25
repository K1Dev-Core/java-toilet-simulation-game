import java.awt.*;
import java.awt.image.BufferedImage;

public class DinoCharacter {
    private int x, y;
    private BufferedImage spriteSheet;
    private int currentFrame;
    private long lastFrameTime;
    private int frameDelay = 200;
    private int spriteWidth = 24;
    private int spriteHeight = 24;
    private int displayWidth = 72;
    private int displayHeight = 72;
    private int totalFrames = 6;
    
    public DinoCharacter(int x, int y, BufferedImage spriteSheet) {
        this.x = x;
        this.y = y;
        this.spriteSheet = spriteSheet;
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
    }
    
    public void render(Graphics2D g2d) {
        updateAnimation();
        
        if (spriteSheet != null) {
            int srcX = currentFrame * spriteWidth;
            int srcY = 0;
            
            BufferedImage currentSprite = spriteSheet.getSubimage(srcX, srcY, spriteWidth, spriteHeight);
            g2d.drawImage(currentSprite, x - displayWidth/2, y - displayHeight/2, displayWidth, displayHeight, null);
        }
    }
    
    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > frameDelay) {
            currentFrame = (currentFrame + 1) % totalFrames;
            lastFrameTime = currentTime;
        }
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
}
