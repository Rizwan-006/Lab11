import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatApplication extends JFrame {
    private JTextPane chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JComboBox<String> userComboBox;
    private JButton emojiButton;
    private JPopupMenu emojiMenu;
    private JLabel typingLabel;
    private Timer typingTimer;

    private Map<String, Color> userColors;
    private SimpleDateFormat timeFormat;

    public ChatApplication() {
        // Initialize the frame
        setTitle("Interactive Chat Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(new BorderLayout());

        // Initialize data structures
        userColors = new HashMap<>();
        userColors.put("Alice", new Color(70, 130, 180));   // SteelBlue
        userColors.put("Bob", new Color(60, 179, 113));     // MediumSeaGreen
        userColors.put("Charlie", new Color(218, 165, 32)); // GoldenRod
        timeFormat = new SimpleDateFormat("HH:mm:ss");

        // Create components
        createComponents();

        // Set up event listeners
        setupEventListeners();

        // Display the frame
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createComponents() {
        // Create chat area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setContentType("text/html");
        chatArea.setText("<html><body style='font-family: Arial; font-size: 12px;'></body></html>");
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        // Create message panel
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Send");

        // Create user selection
        userComboBox = new JComboBox<>(new String[]{"Alice", "Bob", "Charlie"});
        userComboBox.setSelectedIndex(0);

        // Create emoji button and menu
        emojiButton = new JButton("😊");
        createEmojiMenu();

        // Create typing indicator
        typingLabel = new JLabel(" ");
        typingLabel.setForeground(Color.GRAY);

        // Create control panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        JPanel topControlPanel = new JPanel(new BorderLayout());
        topControlPanel.add(userComboBox, BorderLayout.WEST);
        topControlPanel.add(emojiButton, BorderLayout.EAST);

        controlPanel.add(topControlPanel, BorderLayout.NORTH);
        controlPanel.add(messageField, BorderLayout.CENTER);
        controlPanel.add(sendButton, BorderLayout.EAST);
        controlPanel.add(typingLabel, BorderLayout.SOUTH);

        // Add components to the frame
        add(chatScrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        // Create toolbar with formatting options
        JToolBar toolBar = new JToolBar();
        JButton boldButton = new JButton("B");
        boldButton.setFont(new Font("Arial", Font.BOLD, 12));
        JButton italicButton = new JButton("I");
        italicButton.setFont(new Font("Arial", Font.ITALIC, 12));
        JButton underlineButton = new JButton("U");
        underlineButton.setFont(new Font("Arial", Font.PLAIN, 12));

        toolBar.add(boldButton);
        toolBar.add(italicButton);
        toolBar.add(underlineButton);
        toolBar.addSeparator();

        JButton attachButton = new JButton("Attach");
        toolBar.add(attachButton);

        add(toolBar, BorderLayout.NORTH);
    }

    private void createEmojiMenu() {
        emojiMenu = new JPopupMenu();
        String[] emojis = {"😀", "😂", "😍", "😎", "👍", "❤️", "🔥", "🎉", "🙏", "🤔"};

        for (String emoji : emojis) {
            JMenuItem menuItem = new JMenuItem(emoji);
            menuItem.addActionListener(e -> {
                messageField.setText(messageField.getText() + emoji);
                messageField.requestFocus();
            });
            emojiMenu.add(menuItem);
        }
    }

    private void setupEventListeners() {
        // Send message on button click
        sendButton.addActionListener(e -> sendMessage());

        // Send message on Enter key
        messageField.addActionListener(e -> sendMessage());

        // Show emoji menu on button click
        emojiButton.addActionListener(e -> {
            emojiMenu.show(emojiButton, 0, emojiButton.getHeight());
        });

        // Typing indicator
        messageField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                showTypingIndicator();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                showTypingIndicator();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                showTypingIndicator();
            }

            private void showTypingIndicator() {
                String currentUser = (String) userComboBox.getSelectedItem();
                typingLabel.setText(currentUser + " is typing...");

                if (typingTimer != null) {
                    typingTimer.stop();
                }

                typingTimer = new Timer(2000, ev -> {
                    typingLabel.setText(" ");
                });
                typingTimer.setRepeats(false);
                typingTimer.start();
            }
        });

        // Context menu for chat area
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(e -> chatArea.copy());
        contextMenu.add(copyItem);

        JMenuItem clearItem = new JMenuItem("Clear Chat");
        clearItem.addActionListener(e -> chatArea.setText(""));
        contextMenu.add(clearItem);

        chatArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        String user = (String) userComboBox.getSelectedItem();
        Color userColor = userColors.get(user);
        String time = timeFormat.format(new Date());

        // Format message with HTML
        String hexColor = String.format("#%02x%02x%02x",
                userColor.getRed(), userColor.getGreen(), userColor.getBlue());

        String formattedMessage = String.format(
                "<div style='margin: 5px 0;'><b style='color: %s;'>%s</b> " +
                        "<span style='color: gray; font-size: 0.8em;'>[%s]</span><br>%s</div>",
                hexColor, user, time, message);

        // Append to chat area
        try {
            chatArea.getDocument().insertString(
                    chatArea.getDocument().getLength(),
                    formattedMessage, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Scroll to bottom
        chatArea.setCaretPosition(chatArea.getDocument().getLength());

        // Clear message field
        messageField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatApplication());
    }
}