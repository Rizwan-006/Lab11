import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InteractiveChat extends JFrame {
    private JTextPane chatArea;
    private JTextField messageField;
    private JComboBox<String> userCombo;
    private JLabel typingLabel;
    private Timer typingTimer;

    public InteractiveChat() {
        setTitle("Interactive Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Create components
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        messageField = new JTextField(30);
        userCombo = new JComboBox<>(new String[]{"User1", "User2", "User3"});
        JButton sendButton = new JButton("Send");
        JButton emojiButton = new JButton("😊");
        typingLabel = new JLabel(" ");
        typingLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Emoji panel
        JPanel emojiPanel = new JPanel(new GridLayout(2, 5));
        String[] emojis = {"😊", "😂", "😍", "😢", "😎", "👍", "👎", "🎉", "🔥", "💪"};
        for (String emoji : emojis) {
            JButton emojiBtn = new JButton(emoji);
            emojiBtn.addActionListener(e -> {
                messageField.setText(messageField.getText() + emoji);
                messageField.requestFocus();
            });
            emojiPanel.add(emojiBtn);
        }
        JPopupMenu emojiPopup = new JPopupMenu();
        emojiPopup.add(emojiPanel);

        // Layout
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(userCombo);
        inputPanel.add(messageField);
        inputPanel.add(sendButton);
        inputPanel.add(emojiButton);

        // Event handling
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        emojiButton.addActionListener(e -> emojiPopup.show(emojiButton, 0, emojiButton.getHeight()));

        typingTimer = new Timer(1000, e -> typingLabel.setText(" "));
        typingTimer.setRepeats(false);
        messageField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                typingLabel.setText(userCombo.getSelectedItem() + " is typing...");
                typingTimer.restart();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                typingLabel.setText(userCombo.getSelectedItem() + " is typing...");
                typingTimer.restart();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        // File attachment
        JButton attachButton = new JButton("Attach");
        inputPanel.add(attachButton);
        attachButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                appendMessage("[" + userCombo.getSelectedItem() + "] attached: " + fileChooser.getSelectedFile().getName());
            }
        });

        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(typingLabel, BorderLayout.NORTH);
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            appendMessage("[" + userCombo.getSelectedItem() + "] " + message);
            messageField.setText("");
        }
    }

    private void appendMessage(String message) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timestamp = sdf.format(new Date());
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setForeground(attrs, getUserColor((String) userCombo.getSelectedItem()));
            doc.insertString(doc.getLength(), "[" + timestamp + "] " + message + "\n", attrs);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private Color getUserColor(String user) {
        return switch (user) {
            case "User1" -> Color.BLUE;
            case "User2" -> Color.GREEN;
            case "User3" -> Color.RED;
            default -> Color.BLACK;
        };
    }
}