import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.File;

public class InteractiveGame extends JFrame {
    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel statusLabel;
    private Timer timer;
    private int score = 0;
    private Point targetPosition;
    private boolean isPaused = false;

    public InteractiveGame() {
        setTitle("Interactive Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create components
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel = new JLabel("Click the red square!");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (targetPosition != null) {
                    g.setColor(Color.RED);
                    g.fillRect(targetPosition.x, targetPosition.y, 50, 50);
                }
            }
        };
        gamePanel.setBackground(Color.WHITE);

        // Create control panel
        JPanel controlPanel = new JPanel();
        JButton startButton = new JButton("Start");
        JButton pauseButton = new JButton("Pause");
        controlPanel.add(startButton);
        controlPanel.add(pauseButton);
        controlPanel.add(scoreLabel);

        // Mouse listener
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isPaused && targetPosition != null) {
                    if (e.getX() >= targetPosition.x && e.getX() <= targetPosition.x + 50 &&
                            e.getY() >= targetPosition.y && e.getY() <= targetPosition.y + 50) {
                        score += 10;
                        scoreLabel.setText("Score: " + score);
                        playSound("click.wav");
                        moveTarget();
                    }
                }
            }
        });

        // Timer for animation
        timer = new Timer(2000, e -> {
            if (!isPaused) moveTarget();
        });

        // Action listeners
        startButton.addActionListener(e -> {
            isPaused = false;
            timer.start();
            statusLabel.setText("Click the red square!");
            moveTarget();
        });

        pauseButton.addActionListener(e -> {
            isPaused = !isPaused;
            statusLabel.setText(isPaused ? "Game Paused" : "Click the red square!");
            if (isPaused) timer.stop();
            else timer.start();
        });

        // Keyboard shortcuts
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    isPaused = !isPaused;
                    statusLabel.setText(isPaused ? "Game Paused" : "Click the red square!");
                    if (isPaused) timer.stop();
                    else timer.start();
                }
            }
        });

        add(controlPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void moveTarget() {
        int maxX = gamePanel.getWidth() - 50;
        int maxY = gamePanel.getHeight() - 50;
        targetPosition = new Point(
                (int) (Math.random() * maxX),
                (int) (Math.random() * maxY)
        );
        gamePanel.repaint();
    }

    private void playSound(String soundFile) {
        try {
            File file = new File(soundFile);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.err.println("Sound file not found: " + soundFile);
        }
    }
}