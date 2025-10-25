import java.awt.*;
import java.awt.image.BufferedImage;

public class ToiletObject {
    private int x, y;
    private BufferedImage closedImage;
    private BufferedImage openImage;
    private int displayWidth = 80;
    private int displayHeight = 80;
    private int animationFrame = 0;
    private int animationCounter = 0;
    private int animationSpeed = 7;
    
    public ToiletObject(int x, int y, BufferedImage closedImage, BufferedImage openImage) {
        this.x = x;
        this.y = y;
        this.closedImage = closedImage;
        this.openImage = openImage;
    }
    
    public void update() {
        animationCounter++;
        if (animationCounter >= animationSpeed) {
            animationFrame = (animationFrame + 1) % 2;
            animationCounter = 0;
        }
    }
    
    public void render(Graphics2D g2d) {
        BufferedImage currentImage = (animationFrame == 0) ? closedImage : openImage;
        if (currentImage != null) {
            g2d.drawImage(currentImage, x - displayWidth/2, y - displayHeight/2, displayWidth, displayHeight, null);
        }
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
}
