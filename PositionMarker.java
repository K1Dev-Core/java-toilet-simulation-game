import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class PositionMarker {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MarkerWindow markerWindow = new MarkerWindow();
            markerWindow.setVisible(true);
        });
    }
}

class MarkerWindow extends JFrame {
    private MarkerPanel markerPanel;
    private JTextArea positionTextArea;
    private JButton copyButton;
    private JButton clearButton;
    
    public MarkerWindow() {
        setTitle("Position Marker Tool");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        markerPanel = new MarkerPanel();
        add(markerPanel, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        copyButton = new JButton("Copy Positions");
        clearButton = new JButton("Clear All");
        
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyPositions();
            }
        });
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markerPanel.clearPositions();
                positionTextArea.setText("");
            }
        });
        
        buttonPanel.add(copyButton);
        buttonPanel.add(clearButton);
        
        positionTextArea = new JTextArea(8, 30);
        positionTextArea.setEditable(false);
        positionTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(positionTextArea);
        
        controlPanel.add(new JLabel("Marked Positions:"), BorderLayout.NORTH);
        controlPanel.add(scrollPane, BorderLayout.CENTER);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(controlPanel, BorderLayout.EAST);
        
        markerPanel.setPositionTextArea(positionTextArea);
    }
    
    private void copyPositions() {
        String text = positionTextArea.getText();
        if (!text.isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new java.awt.datatransfer.StringSelection(text), null);
            JOptionPane.showMessageDialog(this, "Positions copied to clipboard!");
        }
    }
}

class MarkerPanel extends JPanel {
    private BufferedImage backgroundImage;
    private List<Point> markedPositions;
    private JTextArea positionTextArea;
    
    public MarkerPanel() {
        setPreferredSize(new Dimension(1280, 672));
        markedPositions = new ArrayList<>();
        loadBackgroundImage();
        setupMouseListener();
    }
    
    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(new File("assets/bg/bg_map.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void setupMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point clickedPoint = e.getPoint();
                markedPositions.add(clickedPoint);
                updatePositionText();
                repaint();
            }
        });
    }
    
    public void setPositionTextArea(JTextArea textArea) {
        this.positionTextArea = textArea;
    }
    
    private void updatePositionText() {
        if (positionTextArea != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Marked Positions:\n");
            sb.append("==================\n");
            
            for (int i = 0; i < markedPositions.size(); i++) {
                Point pos = markedPositions.get(i);
                sb.append(String.format("Position %d: (%d, %d)\n", i + 1, pos.x, pos.y));
            }
            
            sb.append("\nJava Code Format:\n");
            sb.append("================\n");
            for (int i = 0; i < markedPositions.size(); i++) {
                Point pos = markedPositions.get(i);
                sb.append(String.format("new GameObject(%d, %d);\n", pos.x, pos.y));
            }
            
            positionTextArea.setText(sb.toString());
        }
    }
    
    public void clearPositions() {
        markedPositions.clear();
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        
        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke(3));
        
        for (int i = 0; i < markedPositions.size(); i++) {
            Point pos = markedPositions.get(i);
            
            g2d.drawLine(pos.x - 10, pos.y, pos.x + 10, pos.y);
            g2d.drawLine(pos.x, pos.y - 10, pos.x, pos.y + 10);
            
            g2d.setColor(Color.WHITE);
            g2d.fillOval(pos.x - 3, pos.y - 3, 6, 6);
            g2d.setColor(Color.YELLOW);
            
            String label = String.valueOf(i + 1);
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.drawString(label, pos.x - labelWidth/2, pos.y - 15);
        }
        
        g2d.dispose();
    }
}
