import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class TextEditor extends JFrame {
    private JTabbedPane tabbedPane;
    private List<EditorTab> openTabs = new ArrayList<>();
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JLabel statusBar;
    private JComboBox<String> fontSizeCombo;
    private JComboBox<String> fontFamilyCombo;
    private JButton themeButton;
    private boolean darkTheme = false;
    private File currentDirectory = new File(System.getProperty("user.home"));
    private RecentFilesManager recentFilesManager = new RecentFilesManager();

    public TextEditor() {
        setTitle("Multi-Document Text Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }


        createMenuBar();
        createToolBar();
        createTabbedPane();
        createStatusBar();


        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);


        addNewTab(null);


        recentFilesManager.loadRecentFiles();
        updateRecentFilesMenu();


        applyTheme();
    }

    private void createMenuBar() {
        menuBar = new JMenuBar();


        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newItem = createMenuItem("New", KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newItem.addActionListener(e -> addNewTab(null));

        JMenuItem openItem = createMenuItem("Open...", KeyEvent.VK_O, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> openFile());

        JMenu recentMenu = new JMenu("Open Recent");
        recentMenu.setMnemonic(KeyEvent.VK_R);

        JMenuItem saveItem = createMenuItem("Save", KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> saveCurrentTab());

        JMenuItem saveAsItem = createMenuItem("Save As...", KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        saveAsItem.addActionListener(e -> saveAsCurrentTab());

        JMenuItem saveAllItem = createMenuItem("Save All", KeyEvent.VK_L, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        saveAllItem.addActionListener(e -> saveAllTabs());

        JMenuItem closeItem = createMenuItem("Close", KeyEvent.VK_W, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
        closeItem.addActionListener(e -> closeCurrentTab());

        JMenuItem exitItem = createMenuItem("Exit", KeyEvent.VK_X, null);
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(recentMenu);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(saveAllItem);
        fileMenu.addSeparator();
        fileMenu.add(closeItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);


        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);







        JMenuItem cutItem = createMenuItem("Cut", KeyEvent.VK_T, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        cutItem.addActionListener(e -> getCurrentEditor().cut());

        JMenuItem copyItem = createMenuItem("Copy", KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        copyItem.addActionListener(e -> getCurrentEditor().copy());

        JMenuItem pasteItem = createMenuItem("Paste", KeyEvent.VK_P, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        pasteItem.addActionListener(e -> getCurrentEditor().paste());

        JMenuItem selectAllItem = createMenuItem("Select All", KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        selectAllItem.addActionListener(e -> getCurrentEditor().selectAll());

        JMenuItem findItem = createMenuItem("Find...", KeyEvent.VK_F, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        findItem.addActionListener(e -> showFindDialog());

        JMenuItem replaceItem = createMenuItem("Replace...", KeyEvent.VK_E, KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
        replaceItem.addActionListener(e -> showReplaceDialog());



        editMenu.addSeparator();
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(selectAllItem);
        editMenu.addSeparator();
        editMenu.add(findItem);
        editMenu.add(replaceItem);


        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenuItem zoomInItem = createMenuItem("Zoom In", KeyEvent.VK_I, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
        zoomInItem.addActionListener(e -> zoomIn());

        JMenuItem zoomOutItem = createMenuItem("Zoom Out", KeyEvent.VK_O, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
        zoomOutItem.addActionListener(e -> zoomOut());

        JMenuItem resetZoomItem = createMenuItem("Reset Zoom", KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));
        resetZoomItem.addActionListener(e -> resetZoom());

        JMenuItem toggleThemeItem = createMenuItem("Toggle Theme", KeyEvent.VK_T, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        toggleThemeItem.addActionListener(e -> toggleTheme());

        viewMenu.add(zoomInItem);
        viewMenu.add(zoomOutItem);
        viewMenu.add(resetZoomItem);
        viewMenu.addSeparator();
        viewMenu.add(toggleThemeItem);


        JMenu formatMenu = new JMenu("Format");
        formatMenu.setMnemonic(KeyEvent.VK_O);

        JMenuItem wordWrapItem = createMenuItem("Word Wrap", KeyEvent.VK_W, null);
        wordWrapItem.addActionListener(e -> toggleWordWrap());

        JMenu syntaxMenu = new JMenu("Syntax Highlighting");
        ButtonGroup syntaxGroup = new ButtonGroup();

        JRadioButtonMenuItem plainTextItem = new JRadioButtonMenuItem("Plain Text");
        plainTextItem.addActionListener(e -> setSyntaxHighlighting(null));
        syntaxMenu.add(plainTextItem);
        syntaxGroup.add(plainTextItem);
        plainTextItem.setSelected(true);

        JRadioButtonMenuItem javaItem = new JRadioButtonMenuItem("Java");
        javaItem.addActionListener(e -> setSyntaxHighlighting("java"));
        syntaxMenu.add(javaItem);
        syntaxGroup.add(javaItem);

        JRadioButtonMenuItem xmlItem = new JRadioButtonMenuItem("XML/HTML");
        xmlItem.addActionListener(e -> setSyntaxHighlighting("xml"));
        syntaxMenu.add(xmlItem);
        syntaxGroup.add(xmlItem);

        JRadioButtonMenuItem cssItem = new JRadioButtonMenuItem("CSS");
        cssItem.addActionListener(e -> setSyntaxHighlighting("css"));
        syntaxMenu.add(cssItem);
        syntaxGroup.add(cssItem);

        formatMenu.add(wordWrapItem);
        formatMenu.addSeparator();
        formatMenu.add(syntaxMenu);


        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = createMenuItem("About", KeyEvent.VK_A, null);
        aboutItem.addActionListener(e -> showAboutDialog());

        helpMenu.add(aboutItem);


        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(formatMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private JMenuItem createMenuItem(String text, int mnemonic, KeyStroke accelerator) {
        JMenuItem item = new JMenuItem(text, mnemonic);
        if (accelerator != null) {
            item.setAccelerator(accelerator);
        }
        return item;
    }

    private void createToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);


        JButton newButton = createToolBarButton("New", "icons/new.png");
        newButton.addActionListener(e -> addNewTab(null));
        toolBar.add(newButton);


        JButton openButton = createToolBarButton("Open", "icons/open.png");
        openButton.addActionListener(e -> openFile());
        toolBar.add(openButton);


        JButton saveButton = createToolBarButton("Save", "icons/save.png");
        saveButton.addActionListener(e -> saveCurrentTab());
        toolBar.add(saveButton);

        toolBar.addSeparator();











        toolBar.addSeparator();


        JButton cutButton = createToolBarButton("Cut", "icons/cut.png");
        cutButton.addActionListener(e -> getCurrentEditor().cut());
        toolBar.add(cutButton);


        JButton copyButton = createToolBarButton("Copy", "icons/copy.png");
        copyButton.addActionListener(e -> getCurrentEditor().copy());
        toolBar.add(copyButton);


        JButton pasteButton = createToolBarButton("Paste", "icons/paste.png");
        pasteButton.addActionListener(e -> getCurrentEditor().paste());
        toolBar.add(pasteButton);

        toolBar.addSeparator();


        fontFamilyCombo = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        fontFamilyCombo.setSelectedItem("Monospaced");
        fontFamilyCombo.addActionListener(e -> updateCurrentEditorFont());
        toolBar.add(fontFamilyCombo);


        fontSizeCombo = new JComboBox<>(new String[]{"8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24"});
        fontSizeCombo.setSelectedItem("12");
        fontSizeCombo.setEditable(true);
        fontSizeCombo.addActionListener(e -> updateCurrentEditorFont());
        toolBar.add(fontSizeCombo);

        toolBar.addSeparator();


        themeButton = createToolBarButton("Theme", "icons/theme.png");
        themeButton.addActionListener(e -> toggleTheme());
        toolBar.add(themeButton);
    }

    private JButton createToolBarButton(String tooltip, String iconPath) {
        JButton button = new JButton();
        button.setToolTipText(tooltip);


        button.setText(tooltip.substring(0, 1));
        button.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        return button;
    }

    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(e -> updateStatusBar());


        tabbedPane.setComponentPopupMenu(createTabPopupMenu());
    }

    private JPopupMenu createTabPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem closeTabItem = new JMenuItem("Close Tab");
        closeTabItem.addActionListener(e -> closeCurrentTab());
        popup.add(closeTabItem);

        JMenuItem closeOtherTabsItem = new JMenuItem("Close Other Tabs");
        closeOtherTabsItem.addActionListener(e -> closeOtherTabs());
        popup.add(closeOtherTabsItem);

        JMenuItem closeAllTabsItem = new JMenuItem("Close All Tabs");
        closeAllTabsItem.addActionListener(e -> closeAllTabs());
        popup.add(closeAllTabsItem);

        return popup;
    }

    private void createStatusBar() {
        statusBar = new JLabel("Ready");
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
    }

    private void addNewTab(File file) {
        EditorTab tab = new EditorTab(file);
        openTabs.add(tab);

        String title = file != null ? file.getName() : "Untitled";
        tabbedPane.addTab(title, tab.getScrollPane());
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);

        if (file != null) {
            tab.loadFile();
            recentFilesManager.addRecentFile(file.getAbsolutePath());
            updateRecentFilesMenu();
        }

        updateStatusBar();
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser(currentDirectory);
        fileChooser.setMultiSelectionEnabled(true);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentDirectory = fileChooser.getCurrentDirectory();
            for (File file : fileChooser.getSelectedFiles()) {
                boolean alreadyOpen = false;
                for (EditorTab tab : openTabs) {
                    if (file.equals(tab.getFile())) {
                        tabbedPane.setSelectedIndex(openTabs.indexOf(tab));
                        alreadyOpen = true;
                        break;
                    }
                }

                if (!alreadyOpen) {
                    addNewTab(file);
                }
            }
        }
    }

    private void openRecentFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            boolean alreadyOpen = false;
            for (EditorTab tab : openTabs) {
                if (file.equals(tab.getFile())) {
                    tabbedPane.setSelectedIndex(openTabs.indexOf(tab));
                    alreadyOpen = true;
                    break;
                }
            }

            if (!alreadyOpen) {
                addNewTab(file);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "The file " + filePath + " does not exist.",
                    "File Not Found",
                    JOptionPane.WARNING_MESSAGE);
            recentFilesManager.removeRecentFile(filePath);
            updateRecentFilesMenu();
        }
    }

    private boolean saveCurrentTab() {
        EditorTab tab = getCurrentEditorTab();
        if (tab.getFile() == null) {
            return saveAsCurrentTab();
        } else {
            tab.saveFile();
            updateTabTitle(tab);
            recentFilesManager.addRecentFile(tab.getFile().getAbsolutePath());
            updateRecentFilesMenu();
            return true;
        }
    }

    private boolean saveAsCurrentTab() {
        EditorTab tab = getCurrentEditorTab();
        JFileChooser fileChooser = new JFileChooser(currentDirectory);

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            currentDirectory = fileChooser.getCurrentDirectory();


            if (!file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }

            tab.setFile(file);
            tab.saveFile();
            updateTabTitle(tab);
            recentFilesManager.addRecentFile(file.getAbsolutePath());
            updateRecentFilesMenu();
            return true;
        }
        return false;
    }

    private void saveAllTabs() {
        for (EditorTab tab : openTabs) {
            if (tab.isDirty()) {
                tabbedPane.setSelectedIndex(openTabs.indexOf(tab));
                if (tab.getFile() == null) {
                    if (!saveAsCurrentTab()) {
                        return;
                    }
                } else {
                    tab.saveFile();
                    updateTabTitle(tab);
                }
            }
        }
    }

    private void closeCurrentTab() {
        EditorTab tab = getCurrentEditorTab();
        if (promptToSaveIfNeeded(tab)) {
            int index = tabbedPane.getSelectedIndex();
            tabbedPane.remove(index);
            openTabs.remove(index);

            if (tabbedPane.getTabCount() == 0) {
                addNewTab(null);
            }

            updateStatusBar();
        }
    }

    private void closeOtherTabs() {
        EditorTab currentTab = getCurrentEditorTab();

        for (int i = openTabs.size() - 1; i >= 0; i--) {
            if (openTabs.get(i) != currentTab) {
                if (promptToSaveIfNeeded(openTabs.get(i))) {
                    tabbedPane.remove(i);
                    openTabs.remove(i);
                }
            }
        }

        if (tabbedPane.getTabCount() == 0) {
            addNewTab(null);
        }
    }

    private void closeAllTabs() {
        for (int i = openTabs.size() - 1; i >= 0; i--) {
            if (promptToSaveIfNeeded(openTabs.get(i))) {
                tabbedPane.remove(i);
                openTabs.remove(i);
            }
        }

        if (tabbedPane.getTabCount() == 0) {
            addNewTab(null);
        }
    }

    private boolean promptToSaveIfNeeded(EditorTab tab) {
        if (tab.isDirty()) {
            String message = "Save changes to " + (tab.getFile() != null ? tab.getFile().getName() : "Untitled") + "?";
            int option = JOptionPane.showConfirmDialog(this, message, "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                if (tab.getFile() == null) {
                    return saveAsCurrentTab();
                } else {
                    tab.saveFile();
                    return true;
                }
            } else if (option == JOptionPane.NO_OPTION) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    private void exitApplication() {
        for (EditorTab tab : openTabs) {
            if (tab.isDirty()) {
                tabbedPane.setSelectedIndex(openTabs.indexOf(tab));
                if (!promptToSaveIfNeeded(tab)) {
                    return;
                }
            }
        }

        recentFilesManager.saveRecentFiles();
        dispose();
        new Main().main(new String[0]);
    }

    private void showFindDialog() {
        EditorTab tab = getCurrentEditorTab();
        FindReplaceDialog dialog = new FindReplaceDialog(this, tab.getEditor(), false);
        dialog.setVisible(true);
    }

    private void showReplaceDialog() {
        EditorTab tab = getCurrentEditorTab();
        FindReplaceDialog dialog = new FindReplaceDialog(this, tab.getEditor(), true);
        dialog.setVisible(true);
    }

    private void zoomIn() {
        EditorTab tab = getCurrentEditorTab();
        Font currentFont = tab.getEditor().getFont();
        tab.getEditor().setFont(new Font(currentFont.getName(), currentFont.getStyle(), currentFont.getSize() + 1));
        updateStatusBar();
    }

    private void zoomOut() {
        EditorTab tab = getCurrentEditorTab();
        Font currentFont = tab.getEditor().getFont();
        if (currentFont.getSize() > 1) {
            tab.getEditor().setFont(new Font(currentFont.getName(), currentFont.getStyle(), currentFont.getSize() - 1));
            updateStatusBar();
        }
    }

    private void resetZoom() {
        EditorTab tab = getCurrentEditorTab();
        Font currentFont = tab.getEditor().getFont();
        tab.getEditor().setFont(new Font(currentFont.getName(), currentFont.getStyle(), 12));
        updateStatusBar();
    }

    private void toggleTheme() {
        darkTheme = !darkTheme;
        applyTheme();
    }

    private void applyTheme() {
        if (darkTheme) {

            Color bgColor = new Color(45, 45, 45);
            Color fgColor = new Color(220, 220, 220);
            Color caretColor = Color.WHITE;
            Color selectionColor = new Color(65, 65, 65);
            Color lineColor = new Color(60, 60, 60);

            UIManager.put("TextArea.background", bgColor);
            UIManager.put("TextArea.foreground", fgColor);
            UIManager.put("TextArea.caretForeground", caretColor);
            UIManager.put("TextArea.selectionBackground", selectionColor);
            UIManager.put("TextArea.inactiveForeground", fgColor.darker());

            UIManager.put("EditorPane.background", bgColor);
            UIManager.put("EditorPane.foreground", fgColor);
            UIManager.put("EditorPane.caretForeground", caretColor);
            UIManager.put("EditorPane.selectionBackground", selectionColor);

            UIManager.put("TextField.background", bgColor);
            UIManager.put("TextField.foreground", fgColor);
            UIManager.put("TextField.caretForeground", caretColor);
            UIManager.put("TextField.selectionBackground", selectionColor);

            UIManager.put("TabbedPane.background", bgColor);
            UIManager.put("TabbedPane.foreground", fgColor);
            UIManager.put("TabbedPane.contentAreaColor", bgColor);
            UIManager.put("TabbedPane.selected", bgColor.brighter());

            UIManager.put("MenuBar.background", bgColor);
            UIManager.put("MenuBar.foreground", fgColor);

            UIManager.put("Menu.background", bgColor);
            UIManager.put("Menu.foreground", fgColor);

            UIManager.put("MenuItem.background", bgColor);
            UIManager.put("MenuItem.foreground", fgColor);

            UIManager.put("ToolBar.background", bgColor);
            UIManager.put("ToolBar.foreground", fgColor);

            UIManager.put("ComboBox.background", bgColor);
            UIManager.put("ComboBox.foreground", fgColor);
            UIManager.put("ComboBox.selectionBackground", selectionColor);
            UIManager.put("ComboBox.selectionForeground", fgColor);

            UIManager.put("Label.foreground", fgColor);

            themeButton.setText("☀");
        } else {

            UIManager.put("TextArea.background", Color.WHITE);
            UIManager.put("TextArea.foreground", Color.BLACK);
            UIManager.put("TextArea.caretForeground", Color.BLACK);
            UIManager.put("TextArea.selectionBackground", new Color(184, 207, 229));
            UIManager.put("TextArea.inactiveForeground", Color.GRAY);

            UIManager.put("EditorPane.background", Color.WHITE);
            UIManager.put("EditorPane.foreground", Color.BLACK);
            UIManager.put("EditorPane.caretForeground", Color.BLACK);
            UIManager.put("EditorPane.selectionBackground", new Color(184, 207, 229));

            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", Color.BLACK);
            UIManager.put("TextField.caretForeground", Color.BLACK);
            UIManager.put("TextField.selectionBackground", new Color(184, 207, 229));

            UIManager.put("TabbedPane.background", null);
            UIManager.put("TabbedPane.foreground", null);
            UIManager.put("TabbedPane.contentAreaColor", null);
            UIManager.put("TabbedPane.selected", null);

            UIManager.put("MenuBar.background", null);
            UIManager.put("MenuBar.foreground", null);

            UIManager.put("Menu.background", null);
            UIManager.put("Menu.foreground", null);

            UIManager.put("MenuItem.background", null);
            UIManager.put("MenuItem.foreground", null);

            UIManager.put("ToolBar.background", null);
            UIManager.put("ToolBar.foreground", null);

            UIManager.put("ComboBox.background", null);
            UIManager.put("ComboBox.foreground", null);
            UIManager.put("ComboBox.selectionBackground", null);
            UIManager.put("ComboBox.selectionForeground", null);

            UIManager.put("Label.foreground", null);

            themeButton.setText("☽");
        }


        SwingUtilities.updateComponentTreeUI(this);


        for (EditorTab tab : openTabs) {
            tab.applyTheme(darkTheme);
        }
    }

    private void toggleWordWrap() {
        EditorTab tab = getCurrentEditorTab();
        tab.setWordWrap(!tab.isWordWrap());
    }

    private void setSyntaxHighlighting(String language) {
        EditorTab tab = getCurrentEditorTab();
        tab.setSyntaxHighlighting(language);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Multi-Document Text Editor\nVersion 1.0\n\nA Java Swing application",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateCurrentEditorFont() {
        EditorTab tab = getCurrentEditorTab();
        if (tab != null) {
            try {
                int size = Integer.parseInt(fontSizeCombo.getSelectedItem().toString());
                String family = fontFamilyCombo.getSelectedItem().toString();
                tab.getEditor().setFont(new Font(family, Font.PLAIN, size));
                updateStatusBar();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid font size",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateTabTitle(EditorTab tab) {
        int index = openTabs.indexOf(tab);
        if (index >= 0) {
            String title = tab.getFile() != null ? tab.getFile().getName() : "Untitled";
            if (tab.isDirty()) {
                title = "*" + title;
            }
            tabbedPane.setTitleAt(index, title);
        }
    }

    private void updateStatusBar() {
        EditorTab tab = getCurrentEditorTab();
        if (tab != null) {
            JTextArea editor = tab.getEditor();
            int line = 1;
            int column = 1;
            try {
                int caretPos = editor.getCaretPosition();
                line = editor.getLineOfOffset(caretPos) + 1;
                column = caretPos - editor.getLineStartOffset(line - 1) + 1;
            } catch (BadLocationException e) {

            }

            String fileInfo = tab.getFile() != null ? tab.getFile().getAbsolutePath() : "Untitled";
            String fontInfo = editor.getFont().getName() + ", " + editor.getFont().getSize() + "pt";
            String positionInfo = "Ln " + line + ", Col " + column;

            statusBar.setText(fileInfo + " | " + fontInfo + " | " + positionInfo);
        } else {
            statusBar.setText("Ready");
        }
    }

    private void updateRecentFilesMenu() {
        JMenu recentMenu = (JMenu) menuBar.getMenu(0).getMenuComponent(2);
        recentMenu.removeAll();

        List<String> recentFiles = recentFilesManager.getRecentFiles();
        if (recentFiles.isEmpty()) {
            JMenuItem emptyItem = new JMenuItem("No recent files");
            emptyItem.setEnabled(false);
            recentMenu.add(emptyItem);
        } else {
            for (String filePath : recentFiles) {
                JMenuItem fileItem = new JMenuItem(new File(filePath).getName());
                fileItem.setToolTipText(filePath);
                fileItem.addActionListener(e -> openRecentFile(filePath));
                recentMenu.add(fileItem);
            }

            recentMenu.addSeparator();
            JMenuItem clearItem = new JMenuItem("Clear Recent Files");
            clearItem.addActionListener(e -> {
                recentFilesManager.clearRecentFiles();
                updateRecentFilesMenu();
            });
            recentMenu.add(clearItem);
        }
    }

    private EditorTab getCurrentEditorTab() {
        int index = tabbedPane.getSelectedIndex();
        return index >= 0 ? openTabs.get(index) : null;
    }

    private JTextArea getCurrentEditor() {
        EditorTab tab = getCurrentEditorTab();
        return tab != null ? tab.getEditor() : null;
    }

    private class EditorTab {
        private File file;
        private JTextArea editor;
        private JScrollPane scrollPane;
        private boolean wordWrap = false;
        private boolean dirty = false;
        private String syntaxHighlighting = null;

        public EditorTab(File file) {
            this.file = file;

            editor = new JTextArea();
            editor.setFont(new Font("Monospaced", Font.PLAIN, 12));
            editor.setTabSize(4);


            editor.getDocument().addUndoableEditListener(new UndoableEditListener() {
                private final UndoManager undoManager = new UndoManager();

                @Override
                public void undoableEditHappened(UndoableEditEvent e) {
                    undoManager.addEdit(e.getEdit());
                    setDirty(true);
                }
            });

            editor.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    setDirty(true);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    setDirty(true);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    setDirty(true);
                }
            });

            editor.addCaretListener(e -> updateStatusBar());

            scrollPane = new JScrollPane(editor);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }

        public void loadFile() {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                editor.setText(content);
                dirty = false;


                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".java")) {
                    setSyntaxHighlighting("java");
                } else if (fileName.endsWith(".xml") || fileName.endsWith(".html")) {
                    setSyntaxHighlighting("xml");
                } else if (fileName.endsWith(".css")) {
                    setSyntaxHighlighting("css");
                } else {
                    setSyntaxHighlighting(null);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(TextEditor.this,
                        "Error reading file: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        public void saveFile() {
            try {
                Files.write(file.toPath(), editor.getText().getBytes());
                dirty = false;
                updateTabTitle(this);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(TextEditor.this,
                        "Error saving file: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        public void undo() {

            try {
                editor.getDocument().remove(0, editor.getDocument().getLength());
                editor.getDocument().insertString(0, "Undo functionality would be implemented here", null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        public void redo() {

            try {
                editor.getDocument().remove(0, editor.getDocument().getLength());
                editor.getDocument().insertString(0, "Redo functionality would be implemented here", null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        public void cut() {
            editor.cut();
        }

        public void copy() {
            editor.copy();
        }

        public void paste() {
            editor.paste();
        }

        public void selectAll() {
            editor.selectAll();
        }

        public void setWordWrap(boolean wordWrap) {
            this.wordWrap = wordWrap;
            editor.setLineWrap(wordWrap);
            editor.setWrapStyleWord(wordWrap);
            scrollPane.setHorizontalScrollBarPolicy(wordWrap ?
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER :
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }

        public boolean isWordWrap() {
            return wordWrap;
        }

        public void setSyntaxHighlighting(String language) {
            this.syntaxHighlighting = language;
            applyTheme(darkTheme);
        }

        public void applyTheme(boolean darkTheme) {
            if (darkTheme) {
                editor.setBackground(new Color(45, 45, 45));
                editor.setForeground(Color.WHITE);
                editor.setCaretColor(Color.WHITE);
                editor.setSelectionColor(new Color(65, 65, 65));
            } else {
                editor.setBackground(Color.WHITE);
                editor.setForeground(Color.BLACK);
                editor.setCaretColor(Color.BLACK);
                editor.setSelectionColor(new Color(184, 207, 229));
            }


            if (syntaxHighlighting != null) {


                editor.setFont(new Font("Monospaced", Font.PLAIN, editor.getFont().getSize()));
            }
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
            updateTabTitle(this);
        }

        public JTextArea getEditor() {
            return editor;
        }

        public JScrollPane getScrollPane() {
            return scrollPane;
        }

        public boolean isDirty() {
            return dirty;
        }

        public void setDirty(boolean dirty) {
            this.dirty = dirty;
            updateTabTitle(this);
        }
    }

    private class FindReplaceDialog extends JDialog {
        private JTextArea editor;
        private JTextField findField;
        private JTextField replaceField;
        private JCheckBox matchCaseCheck;
        private JCheckBox wholeWordCheck;
        private JCheckBox regexCheck;
        private JButton findNextBtn;
        private JButton replaceBtn;
        private JButton replaceAllBtn;
        private int lastFindPos = -1;
        private boolean replaceMode;

        public FindReplaceDialog(Frame owner, JTextArea editor, boolean replaceMode) {
            super(owner, replaceMode ? "Replace" : "Find", false);
            this.editor = editor;
            this.replaceMode = replaceMode;
            setSize(400, replaceMode ? 250 : 200);
            setLocationRelativeTo(owner);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));


            JPanel findPanel = new JPanel(new GridLayout(replaceMode ? 2 : 1, 2, 5, 5));
            findPanel.add(new JLabel("Find:"));
            findField = new JTextField();
            findPanel.add(findField);

            if (replaceMode) {
                findPanel.add(new JLabel("Replace with:"));
                replaceField = new JTextField();
                findPanel.add(replaceField);
            }

            panel.add(findPanel, BorderLayout.NORTH);


            JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
            matchCaseCheck = new JCheckBox("Match case");
            wholeWordCheck = new JCheckBox("Whole words only");
            regexCheck = new JCheckBox("Regular expression");
            optionsPanel.add(matchCaseCheck);
            optionsPanel.add(wholeWordCheck);
            optionsPanel.add(regexCheck);
            panel.add(optionsPanel, BorderLayout.CENTER);


            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
            findNextBtn = new JButton("Find Next");
            findNextBtn.addActionListener(e -> findNext());
            buttonPanel.add(findNextBtn);

            if (replaceMode) {
                replaceBtn = new JButton("Replace");
                replaceBtn.addActionListener(e -> replace());
                buttonPanel.add(replaceBtn);

                replaceAllBtn = new JButton("Replace All");
                replaceAllBtn.addActionListener(e -> replaceAll());
                buttonPanel.add(replaceAllBtn);
            }

            JButton closeBtn = new JButton("Close");
            closeBtn.addActionListener(e -> dispose());
            buttonPanel.add(closeBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            add(panel);


            findField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateButtonStates();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateButtonStates();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateButtonStates();
                }
            });

            updateButtonStates();
        }

        private void updateButtonStates() {
            boolean hasText = !findField.getText().isEmpty();
            findNextBtn.setEnabled(hasText);
            if (replaceMode) {
                replaceBtn.setEnabled(hasText);
                replaceAllBtn.setEnabled(hasText);
            }
        }

        private void findNext() {
            String text = editor.getText();
            String search = findField.getText();
            int startPos = editor.getCaretPosition();

            if (lastFindPos != -1 && lastFindPos != startPos) {
                startPos = lastFindPos;
            }

            boolean found = false;
            int foundPos = -1;

            if (!matchCaseCheck.isSelected()) {
                text = text.toLowerCase();
                search = search.toLowerCase();
            }

            if (regexCheck.isSelected()) {


                foundPos = text.indexOf(search, startPos);
                if (foundPos == -1 && startPos > 0) {
                    foundPos = text.indexOf(search);
                }
                found = foundPos != -1;
            } else {
                if (wholeWordCheck.isSelected()) {


                    foundPos = text.indexOf(search, startPos);
                    if (foundPos == -1 && startPos > 0) {
                        foundPos = text.indexOf(search);
                    }
                    found = foundPos != -1;
                } else {
                    foundPos = text.indexOf(search, startPos);
                    if (foundPos == -1 && startPos > 0) {
                        foundPos = text.indexOf(search);
                    }
                    found = foundPos != -1;
                }
            }

            if (found) {
                editor.setSelectionStart(foundPos);
                editor.setSelectionEnd(foundPos + search.length());
                editor.getCaret().setDot(foundPos + search.length());
                lastFindPos = foundPos + search.length();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Text not found",
                        "Find",
                        JOptionPane.INFORMATION_MESSAGE);
                lastFindPos = -1;
            }
        }

        private void replace() {
            if (editor.getSelectedText() != null &&
                    editor.getSelectedText().equals(findField.getText())) {
                editor.replaceSelection(replaceField.getText());
            }
            findNext();
        }

        private void replaceAll() {
            int count = 0;
            editor.setCaretPosition(0);
            lastFindPos = 0;

            while (true) {
                findNext();
                if (editor.getSelectedText() == null) break;
                if (!editor.getSelectedText().equals(findField.getText())) break;

                editor.replaceSelection(replaceField.getText());
                count++;
            }

            JOptionPane.showMessageDialog(this,
                    "Replaced " + count + " occurrences",
                    "Replace All",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class RecentFilesManager {
        private static final String RECENT_FILES_FILE = "recent_files.txt";
        private static final int MAX_RECENT_FILES = 10;
        private List<String> recentFiles = new ArrayList<>();

        public void addRecentFile(String filePath) {
            recentFiles.remove(filePath);
            recentFiles.add(0, filePath);

            if (recentFiles.size() > MAX_RECENT_FILES) {
                recentFiles = recentFiles.subList(0, MAX_RECENT_FILES);
            }

            saveRecentFiles();
        }

        public void removeRecentFile(String filePath) {
            recentFiles.remove(filePath);
            saveRecentFiles();
        }

        public void clearRecentFiles() {
            recentFiles.clear();
            saveRecentFiles();
        }

        public List<String> getRecentFiles() {
            return new ArrayList<>(recentFiles);
        }

        public void loadRecentFiles() {
            File file = new File(RECENT_FILES_FILE);
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    recentFiles.clear();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (new File(line).exists()) {
                            recentFiles.add(line);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void saveRecentFiles() {
            try (PrintWriter writer = new PrintWriter(RECENT_FILES_FILE)) {
                for (String filePath : recentFiles) {
                    writer.println(filePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}