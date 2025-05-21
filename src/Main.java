import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create the main menu frame with modern styling
            JFrame mainMenu = new JFrame("Java GUI Lab 11 - Event Handling");
            mainMenu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainMenu.setSize(600, 700);
            mainMenu.setLocationRelativeTo(null);

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Create main panel with gradient background
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    GradientPaint gp = new GradientPaint(0, 0, new Color(30, 136, 229), getWidth(), getHeight(), new Color(0, 172, 193));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 15, 15, 15);

            // Title label
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            JLabel titleLabel = new JLabel("Java GUI Lab 11 - Event Handling");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            titleLabel.setForeground(Color.WHITE);
            panel.add(titleLabel, gbc);

            // Activity buttons
            gbc.gridwidth = 1;
            gbc.gridy = 1;
            addStyledButton(panel, gbc, "Activity 1: Text Editor", e -> {
                mainMenu.dispose();
                new TextEditor().setVisible(true);
            });

            gbc.gridy = 2;
            addStyledButton(panel, gbc, "Activity 2: Drawing Application", e -> {
                mainMenu.dispose();
                new DrawingApplication().setVisible(true);
            });

            gbc.gridy = 3;
            addStyledButton(panel, gbc, "Activity 3: Form Wizard", e -> {
                mainMenu.dispose();
                new FormWizard().setVisible(true);
            });

            gbc.gridy = 4;
            addStyledButton(panel, gbc, "Activity 4: Interactive Game", e -> {
                mainMenu.dispose();
                new InteractiveGame().setVisible(true);
            });

            // Task buttons
            gbc.gridy = 5;
            addStyledButton(panel, gbc, "Task 1: Interactive Chat", e -> {
                mainMenu.dispose();
                new InteractiveChat().setVisible(true);
            });

            gbc.gridy = 6;
            addStyledButton(panel, gbc, "Task 2: Form Builder", e -> {
                mainMenu.dispose();
                new FormBuilder().setVisible(true);
            });

            gbc.gridy = 7;
            addStyledButton(panel, gbc, "Task 3: Music Player", e -> {
                mainMenu.dispose();
                new MusicPlayer().setVisible(true);
            });

            gbc.gridy = 8;
            addStyledButton(panel, gbc, "Task 4: Attendance System", e -> {
                mainMenu.dispose();
                new AttendanceSystem().setVisible(true);
            });
//
//            gbc.gridy = 9;
//            addStyledButton(panel, gbc, "Task 5: Interactive Dashboard", e -> {
//                mainMenu.dispose();
//                new InteractiveDashboard().setVisible(true);
//            });

            gbc.gridy = 10;
            addStyledButton(panel, gbc, "Exit", e -> System.exit(0));

            mainMenu.add(panel);
            mainMenu.setVisible(true);
        });
    }

    private static void addStyledButton(JPanel panel, GridBagConstraints gbc, String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setBackground(new Color(255, 255, 255, 150));
        button.setForeground(new Color(50, 50, 50));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 200), 2),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(button, gbc);
    }
}