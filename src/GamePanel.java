import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class GamePanel extends JPanel {
    private BufferedImage backgroundImage;
    private List<ToiletObject> toiletObjects;
    private BufferedImage dinoSpriteSheet;
    private BufferedImage toiletImage;
    private BufferedImage toiletOpenImage;
    private GameState gameState;
    private NetworkManager networkManager;
    private Player player;
    private boolean gameStarted;
    private boolean waitingForPlayers;
    private int playerCount;
    private String gameStatus;
    private int myPlayerIndex;
    private boolean characterSelected;
    private int selectedCharacterIndex;
    private List<BufferedImage> characterPreviews;
    private boolean spacePressed;
    private long lastSpacePress;
    private int comboCount;
    private long lastComboTime;
    private String comboText;
    private float comboTextAngle;
    private int comboTextScale;
    private boolean winSoundPlayed;
    private boolean loseSoundPlayed;
    private long comboTextStartTime;
    private JButton prevButton;
    private JButton nextButton;
    private JButton joinButton;
    private Clip buttonSound;
    private Clip clickSound;
    private Clip winSound;
    private Clip loseSound;
    private BufferedImage shitImage;
    
    public GamePanel() {
        setPreferredSize(new Dimension(1280, 672));
        toiletObjects = new ArrayList<>();
        gameState = new GameState();
        player = null;
        gameStarted = false;
        waitingForPlayers = true;
        playerCount = 0;
        myPlayerIndex = -1;
        gameStatus = "Waiting for players...";
        characterSelected = false;
        selectedCharacterIndex = 0;
        characterPreviews = new ArrayList<>();
        spacePressed = false;
        lastSpacePress = 0;
        comboCount = 0;
        lastComboTime = 0;
        comboText = "";
        winSoundPlayed = false;
        loseSoundPlayed = false;
        comboTextStartTime = 0;
        
        loadBackgroundImage();
        loadDinoSprites();
        loadToiletImage();
        loadShitImage();
        loadCharacterPreviews();
        createToiletObjects();
        
        loadSounds();
        createButtons();
        
        networkManager = null;
        
        setupKeyListener();
        startAnimationTimer();
    }
    
    private void loadSounds() {
        try {
            AudioInputStream buttonStream = AudioSystem.getAudioInputStream(new File("assets/sfx/button.wav"));
            buttonSound = AudioSystem.getClip();
            buttonSound.open(buttonStream);
            
            AudioInputStream clickStream = AudioSystem.getAudioInputStream(new File("assets/sfx/click3_1.wav"));
            clickSound = AudioSystem.getClip();
            clickSound.open(clickStream);
            
            AudioInputStream winStream = AudioSystem.getAudioInputStream(new File("assets/sfx/toilet.wav"));
            winSound = AudioSystem.getClip();
            winSound.open(winStream);
            
            AudioInputStream loseStream = AudioSystem.getAudioInputStream(new File("assets/sfx/Shit.wav"));
            loseSound = AudioSystem.getClip();
            loseSound.open(loseStream);
        } catch (Exception e) {
        }
    }
    
    private void playButtonSound() {
        if (buttonSound != null) {
            buttonSound.setFramePosition(0);
            buttonSound.start();
        }
    }
    
    private void playClickSound() {
        if (clickSound != null) {
            clickSound.setFramePosition(0);
            clickSound.start();
        }
    }
    
    private void playWinSound() {
        if (winSound != null && !winSoundPlayed) {
            winSound.setFramePosition(0);
            winSound.start();
            winSoundPlayed = true;
        }
    }
    
    private void playLoseSound() {
        if (loseSound != null && !loseSoundPlayed) {
            loseSound.setFramePosition(0);
            loseSound.start();
            loseSoundPlayed = true;
        }
    }
    
    
    private void createButtons() {
        prevButton = new JButton("← Previous");
        nextButton = new JButton("Next →");
        joinButton = new JButton("JOIN GAME");
        
        prevButton.setBounds(450, 320, 100, 35);
        nextButton.setBounds(730, 320, 100, 35);
        joinButton.setBounds(590, 420, 100, 40);
        
        prevButton.setFont(new Font("Arial", Font.BOLD, 14));
        nextButton.setFont(new Font("Arial", Font.BOLD, 14));
        joinButton.setFont(new Font("Arial", Font.BOLD, 16));
        
        prevButton.setBackground(new Color(70, 130, 180));
        nextButton.setBackground(new Color(70, 130, 180));
        joinButton.setBackground(new Color(34, 139, 34));
        
        prevButton.setForeground(Color.WHITE);
        nextButton.setForeground(Color.WHITE);
        joinButton.setForeground(Color.WHITE);
        
        prevButton.setFocusPainted(false);
        nextButton.setFocusPainted(false);
        joinButton.setFocusPainted(false);
        
        prevButton.setBorder(BorderFactory.createRaisedBevelBorder());
        nextButton.setBorder(BorderFactory.createRaisedBevelBorder());
        joinButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        prevButton.addActionListener(e -> {
            if (!characterSelected) {
                playButtonSound();
                selectedCharacterIndex = (selectedCharacterIndex - 1 + characterPreviews.size()) % characterPreviews.size();
                repaint();
            }
        });
        
        nextButton.addActionListener(e -> {
            if (!characterSelected) {
                playButtonSound();
                selectedCharacterIndex = (selectedCharacterIndex + 1) % characterPreviews.size();
                repaint();
            }
        });
        
        joinButton.addActionListener(e -> {
            if (!characterSelected) {
                playButtonSound();
                characterSelected = true;
                gameStatus = "Character selected! Waiting for game to start...";
                joinGame();
                updateButtonVisibility();
            }
        });
        
        updateButtonVisibility();
        
        add(prevButton);
        add(nextButton);
        add(joinButton);
    }
    
    private void updateButtonVisibility() {
        if (!characterSelected) {
            prevButton.setVisible(true);
            nextButton.setVisible(true);
            joinButton.setVisible(true);
        } else {
            prevButton.setVisible(false);
            nextButton.setVisible(false);
            joinButton.setVisible(false);
        }
    }
    
    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(new File("assets/bg/bg_map.png"));
        } catch (IOException e) {
            backgroundImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = backgroundImage.createGraphics();
            g2d.setColor(new Color(135, 206, 235));
            g2d.fillRect(0, 0, 800, 600);
            g2d.dispose();
        }
    }
    
    private void loadDinoSprites() {
        try {
            dinoSpriteSheet = ImageIO.read(new File("assets/DinoSprites/DinoSprites - doux.png"));
        } catch (IOException e) {
            dinoSpriteSheet = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = dinoSpriteSheet.createGraphics();
            g2d.setColor(Color.GREEN);
            g2d.fillRect(0, 0, 100, 50);
            g2d.dispose();
        }
    }
    
    private void loadToiletImage() {
        try {
            toiletImage = ImageIO.read(new File("assets/obj/toilet.png"));
            toiletOpenImage = ImageIO.read(new File("assets/obj/toilet-o.png"));
        } catch (IOException e) {
            toiletImage = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = toiletImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 30, 30);
            g2d.dispose();
            
            toiletOpenImage = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d2 = toiletOpenImage.createGraphics();
            g2d2.setColor(Color.YELLOW);
            g2d2.fillRect(0, 0, 30, 30);
            g2d2.dispose();
        }
    }
    
    private void loadShitImage() {
        try {
            shitImage = ImageIO.read(new File("assets/obj/Shit.png"));
        } catch (IOException e) {
            shitImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = shitImage.createGraphics();
            g2d.setColor(new Color(139, 69, 19));
            g2d.fillOval(0, 0, 50, 50);
            g2d.dispose();
        }
    }
    
    private void loadCharacterPreviews() {
        try {
            BufferedImage dino1Stop = ImageIO.read(new File("assets/DinoSprites/d1/stop/1.png"));
            BufferedImage dino2Stop = ImageIO.read(new File("assets/DinoSprites/d2/stop/1.png"));
            BufferedImage dino3Stop = ImageIO.read(new File("assets/DinoSprites/d3/stop/1.png"));
            BufferedImage dino4Stop = ImageIO.read(new File("assets/DinoSprites/d4/stop/1.png"));
            characterPreviews.add(dino1Stop);
            characterPreviews.add(dino2Stop);
            characterPreviews.add(dino3Stop);
            characterPreviews.add(dino4Stop);
        } catch (IOException e) {
            BufferedImage placeholder1 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d1 = placeholder1.createGraphics();
            g2d1.setColor(Color.GREEN);
            g2d1.fillRect(0, 0, 50, 50);
            g2d1.dispose();
            
            BufferedImage placeholder2 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d2 = placeholder2.createGraphics();
            g2d2.setColor(Color.BLUE);
            g2d2.fillRect(0, 0, 50, 50);
            g2d2.dispose();
            
            BufferedImage placeholder3 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d3 = placeholder3.createGraphics();
            g2d3.setColor(Color.RED);
            g2d3.fillRect(0, 0, 50, 50);
            g2d3.dispose();
            
            BufferedImage placeholder4 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d4 = placeholder4.createGraphics();
            g2d4.setColor(Color.YELLOW);
            g2d4.fillRect(0, 0, 50, 50);
            g2d4.dispose();
            
            characterPreviews.add(placeholder1);
            characterPreviews.add(placeholder2);
            characterPreviews.add(placeholder3);
            characterPreviews.add(placeholder4);
        }
    }
    
    
    private void createPlayersForRace() {
        int[] yPositions = {110, 260, 400, 550};
        
        if (player == null) {
            int index = (myPlayerIndex >= 0 && myPlayerIndex < yPositions.length) ? myPlayerIndex : 0;
            int y = yPositions[index];
            player = new Player(30, y, selectedCharacterIndex);
            player.setId(networkManager != null ? networkManager.getPlayerId() : "Player1");
        }
    }
    
    private void createToiletObjects() {
        toiletObjects.add(new ToiletObject(1200, 110, toiletImage, toiletOpenImage));
        toiletObjects.add(new ToiletObject(1200, 260, toiletImage, toiletOpenImage));
        toiletObjects.add(new ToiletObject(1200, 400, toiletImage, toiletOpenImage));
        toiletObjects.add(new ToiletObject(1200, 550, toiletImage, toiletOpenImage));
    }
    
    private void setupKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                long currentTime = System.currentTimeMillis();
                
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (spacePressed) {
                        return;
                    }
                    spacePressed = true;
                    
                    if (!characterSelected) {
                        characterSelected = true;
                        gameStatus = "Character selected! Waiting for game to start...";
                        joinGame();
                    } else if (gameStarted && player != null && gameState.getWinner() == null) {
                        long comboTime = System.currentTimeMillis();
                        
                        if (comboTime - lastComboTime < 1000) {
                            comboCount++;
                        } else {
                            comboCount = 1;
                        }
                        lastComboTime = comboTime;
                        
                        if (comboCount >= 3) {
                            comboText = "COMBO x" + comboCount + "!";
                            
                            comboTextStartTime = System.currentTimeMillis();
                        } else {
                            comboText = "";
                        }
                        
                        playClickSound();
                        player.step();
                        if (networkManager != null) {
                            networkManager.sendRun();
                            networkManager.sendPosition(player.getX(), player.getY(), player.isRunning());
                            
                            for (int i = 0; i < 10; i++) {
                                networkManager.sendPosition(player.getX(), player.getY(), player.isRunning());
                            }
                        }
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    spacePressed = false;
                }
            }
        });
        
        setFocusable(true);
        requestFocusInWindow();
    }
    
    private void joinGame() {
        if (networkManager == null) {
            networkManager = new NetworkManager(gameState, dinoSpriteSheet);
            networkManager.setSelectedCharacterIndex(selectedCharacterIndex);
            networkManager.sendJoin();
        } else if (myPlayerIndex == -1 && !networkManager.isConnected()) {
            networkManager.sendJoin();
        }
        
        if (gameStarted && player == null) {
            if (myPlayerIndex == -1) {
                myPlayerIndex = 0;
            }
            createPlayersForRace();
        }
    }
    
    private void startAnimationTimer() {
        Timer animationTimer = new Timer(16, _ -> {
            updateGame();
            repaint();
        });
        animationTimer.start();
    }
    
    private void updateGame() {
        int newPlayerCount = gameState.getPlayerCount();
        boolean newGameStarted = gameState.isGameStarted();
        int newPlayerIndex = networkManager != null ? gameState.getPlayerIndex(networkManager.getPlayerId()) : -1;
        
        if (newPlayerCount != playerCount) {
            playerCount = newPlayerCount;
        }
        
        if (newPlayerIndex != -1 && myPlayerIndex == -1) {
            myPlayerIndex = newPlayerIndex;
        }
        
        if (newGameStarted != gameStarted) {
            gameStarted = newGameStarted;
            if (gameStarted) {
                gameStatus = "Game Started! Press SPACE to run!";
                if (player == null) {
                    if (myPlayerIndex == -1) {
                        myPlayerIndex = 0;
                    }
                    createPlayersForRace();
                }
            }
        }
        
        if (gameStarted) {
            for (ToiletObject toilet : toiletObjects) {
                toilet.update();
            }
            
            if (player != null) {
                boolean wasRunning = player.isRunning();
                player.update();
                boolean nowRunning = player.isRunning();
                
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastComboTime > 2000 && !comboText.isEmpty()) {
                    comboText = "";
                    comboCount = 0;
                }
                
                if (networkManager != null) {
                    networkManager.sendPosition(player.getX(), player.getY(), player.isRunning());
                    
                    if (player.getX() > 1000) {
                        for (int i = 0; i < 20; i++) {
                            networkManager.sendPosition(player.getX(), player.getY(), player.isRunning());
                        }
                    }
                }
                
                
                for (ToiletObject toilet : toiletObjects) {
                    if (player.checkCollision(toilet)) {
                        gameState.setWinner(player.getId());
                        gameStatus = "You Win!";
                        if (networkManager != null) {
                            networkManager.sendWin();
                            networkManager.sendPosition(player.getX(), player.getY(), player.isRunning());
                            
                            for (int i = 0; i < 50; i++) {
                                networkManager.sendPosition(player.getX(), player.getY(), player.isRunning());
                            }
                        }
                    }
                }
            }
            
            List<Player> otherPlayers = gameState.getOtherPlayers();
            for (Player otherPlayer : otherPlayers) {
                if (gameState.getWinner() == null) {
                    otherPlayer.updateWithInterpolation();
                }
                for (ToiletObject toilet : toiletObjects) {
                    if (otherPlayer.checkCollision(toilet)) {
                        gameState.setWinner(otherPlayer.getId());
                        gameStatus = otherPlayer.getId() + " Wins!";
                    }
                }
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        
        if (!characterSelected) {
            drawCharacterSelectionScreen(g2d);
        } else if (gameStarted) {
            for (ToiletObject toilet : toiletObjects) {
                toilet.render(g2d);
            }
            
            if (player != null) {
                player.render(g2d);
            }
            
            List<Player> otherPlayers = gameState.getOtherPlayers();
            for (Player otherPlayer : otherPlayers) {
                otherPlayer.render(g2d);
            }
            
            if (player != null) {
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                g2d.drawString(" YOU", player.getX() - 18, player.getY() - 20);
            }
            
            if (!comboText.isEmpty() && player != null) {
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - comboTextStartTime;
                
                float progress = Math.min(elapsed / 1000.0f, 1.0f);
                comboTextAngle = (float) (Math.sin(elapsed * 0.01) * 0.3);
                comboTextScale = (int) (100 + Math.sin(elapsed * 0.02) * 20);
                
                Graphics2D comboG2d = (Graphics2D) g2d.create();
                comboG2d.translate(player.getX(), player.getY() - 80);
                comboG2d.rotate(comboTextAngle);
                comboG2d.scale(comboTextScale / 100.0, comboTextScale / 100.0);
                
                Color[] colors = {Color.YELLOW, Color.ORANGE, Color.RED, Color.MAGENTA};
                Color currentColor = colors[(int)(elapsed / 200) % colors.length];
                
                comboG2d.setColor(new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), (int)(255 * (1.0f - progress))));
                comboG2d.setFont(new Font("Arial", Font.BOLD, 28));
                
                FontMetrics fm = comboG2d.getFontMetrics();
                int textWidth = fm.stringWidth(comboText);
                comboG2d.drawString(comboText, -textWidth/2, 0);
                
                comboG2d.setColor(new Color(255, 255, 255, (int)(128 * (1.0f - progress))));
                comboG2d.setFont(new Font("Arial", Font.BOLD, 30));
                fm = comboG2d.getFontMetrics();
                textWidth = fm.stringWidth(comboText);
                comboG2d.drawString(comboText, -textWidth/2, 0);
                
                comboG2d.dispose();
            }
            
            drawFinishLine(g2d);
            drawInstructions(g2d);
        } else {
            drawCharacterPreview(g2d);
            drawInstructions(g2d);
        }
        
        if (gameState.getWinner() == null && !gameStarted && characterSelected) {
            drawInstructions(g2d);
        }
        
        if (gameState.getWinner() != null) {
            String winnerId = gameState.getWinner();
            boolean isWinner = winnerId.equals(player != null ? player.getId() : "");
            
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            if (isWinner) {
                playWinSound();
                if (toiletOpenImage != null) {
                    g2d.drawImage(toiletOpenImage, centerX - 100, centerY - 100, 200, 200, null);
                }
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                g2d.drawString("You got the golden toilet!", centerX - 200, centerY + 150);
            } else {
                playLoseSound();
                if (shitImage != null) {
                    g2d.drawImage(shitImage, centerX - 100, centerY - 100, 200, 200, null);
                }
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                g2d.drawString("You shit yourself!", centerX - 180, centerY + 150);
            }
        }
        
        g2d.dispose();
    }
    
    private void drawCharacterPreview(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        


        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(centerX - 60, centerY - 60, 120, 120);
        
        if (selectedCharacterIndex < characterPreviews.size() && characterPreviews.get(selectedCharacterIndex) != null) {
            g2d.drawImage(characterPreviews.get(selectedCharacterIndex), centerX - 50, centerY - 50, 100, 100, null);
        }
        

        
        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Waiting for game to start!", centerX - 120, centerY + 150);
    }
    
    private void drawCharacterSelectionScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 42));
        g2d.drawString("SELECT CHARACTER", 420, 120);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.drawString("Choose your character and join the race!", 450, 160);

        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(centerX - 60, centerY - 60, 120, 120);
        
        g2d.setColor(Color.BLACK);
        g2d.fillRect(centerX - 50, centerY - 50, 100, 100);
        
        if (selectedCharacterIndex < characterPreviews.size() && characterPreviews.get(selectedCharacterIndex) != null) {
            g2d.drawImage(characterPreviews.get(selectedCharacterIndex), centerX - 50, centerY - 50, 100, 100, null);
        }
        


        
   
    }
    
    private void drawWaitingScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.drawString("TOILET RACE", 450, 200);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        g2d.drawString(gameStatus, 500, 300);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameStarted) {
            g2d.drawString("Press SPACE to run!", 480, 400);
            
        } else {

            g2d.drawString("Need at least 3 players to start", 460, 450);
        }
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("Now players: " + playerCount, 520, 500);
    }
    
    private void drawFinishLine(Graphics2D g2d) {

        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("FINISH", 1155, 30);
    }
    
    private void drawInstructions(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        
        String instruction1 = "Press SPACE to run!";
        String instruction2 = "First to reach the toilet wins!";
        
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth1 = fm.stringWidth(instruction1);
        int textWidth2 = fm.stringWidth(instruction2);
        
        int centerX = getWidth() / 2;
        int bottomY = getHeight() - 30;
        
        g2d.drawString(instruction1, centerX - textWidth1 / 2, bottomY - 20);
        g2d.drawString(instruction2, centerX - textWidth2 / 2, bottomY);
    }
}