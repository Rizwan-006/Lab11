import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class TextEditor extends JFrame {
    private JTextPane textPane;
    private JLabel statusLabel;
    private int caretPosition = 0;
    private int wordCount = 0;

    public TextEditor() {
        setTitle("Text Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create components
        textPane = new JTextPane();
        textPane.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textPane);
        statusLabel = new JLabel("Position: 0 | Words: 0");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu formatMenu = new JMenu("Format");

        // File menu items
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        JMenuItem openItem = new JMenuItem("Open");
        fileMenu.add(openItem);
        fileMenu.add(saveItem);

        // Format menu items
        JMenuItem boldItem = new JMenuItem("Bold");
        boldItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
        JMenuItem italicItem = new JMenuItem("Italic");
        italicItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        JMenuItem underlineItem = new JMenuItem("Underline");
        underlineItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
        formatMenu.add(boldItem);
        formatMenu.add(italicItem);
        formatMenu.add(underlineItem);

        menuBar.add(fileMenu);
        menuBar.add(formatMenu);
        setJMenuBar(menuBar);

        // Create toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton boldButton = new JButton("B");
        boldButton.setFont(new Font("Arial", Font.BOLD, 12));
        JButton italicButton = new JButton("I");
        italicButton.setFont(new Font("Arial", Font.ITALIC, 12));
        JButton underlineButton = new JButton("U");
        underlineButton.setFont(new Font("Arial", Font.PLAIN, 12));
        toolBar.add(boldButton);
        toolBar.add(italicButton);
        toolBar.add(underlineButton);

        // Create popup menu
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        popupMenu.add(copyItem);
        popupMenu.add(pasteItem);
        popupMenu.add(cutItem);
        textPane.setComponentPopupMenu(popupMenu);

        // Event handling
        boldButton.addActionListener(e -> applyStyle("bold"));
        italicButton.addActionListener(e -> applyStyle("italic"));
        underlineButton.addActionListener(e -> applyStyle("underline"));
        boldItem.addActionListener(e -> applyStyle("bold"));
        italicItem.addActionListener(e -> applyStyle("italic"));
        underlineItem.addActionListener(e -> applyStyle("underline"));

        saveItem.addActionListener(e -> saveFile());
        openItem.addActionListener(e -> openFile());
        copyItem.addActionListener(e -> textPane.copy());
        pasteItem.addActionListener(e -> textPane.paste());
        cutItem.addActionListener(e -> textPane.cut());

        textPane.addCaretListener(e -> {
            caretPosition = textPane.getCaretPosition();
            updateStatus();
        });

        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateWordCount();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateWordCount();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateWordCount();
            }
        });

        // Add components
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void applyStyle(String style) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start == end) return;

        SimpleAttributeSet attrs = new SimpleAttributeSet();
        switch (style) {
            case "bold":
                StyleConstants.setBold(attrs, !StyleConstants.isBold(doc.getCharacterElement(start).getAttributes()));
                break;
            case "italic":
                StyleConstants.setItalic(attrs, !StyleConstants.isItalic(doc.getCharacterElement(start).getAttributes()));
                break;
            case "underline":
                StyleConstants.setUnderline(attrs, !StyleConstants.isUnderline(doc.getCharacterElement(start).getAttributes()));
                break;
        }
        doc.setCharacterAttributes(start, end - start, attrs, false);
    }

    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                writer.write(textPane.getText());
                JOptionPane.showMessageDialog(this, "File saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                textPane.setText("");
                String line;
                while ((line = reader.readLine()) != null) {
                    textPane.setText(textPane.getText() + line + "\n");
                }
                updateWordCount();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateWordCount() {
        String text = textPane.getText();
        wordCount = text.isEmpty() ? 0 : text.trim().split("\\s+").length;
        updateStatus();
    }

    private void updateStatus() {
        statusLabel.setText("Position: " + caretPosition + " | Words: " + wordCount);
    }
}