import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private GamePanel gamePanel;
    
    public GameWindow() {
        setTitle("Toilet Simulation - Online Race");
        setSize(1280, 672);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        gamePanel = new GamePanel();
        add(gamePanel);
        
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();
    }
}
