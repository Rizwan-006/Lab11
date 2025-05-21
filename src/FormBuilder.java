import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.util.ArrayList;
import java.util.List;

public class FormBuilder extends JFrame {
    private JPanel componentPalette;
    private JPanel formDesignArea;
    private JPanel propertyEditor;
    private JPanel livePreview;
    private List<FormComponent> components;
    private FormComponent selectedComponent;

    public FormBuilder() {
        // Initialize the frame
        setTitle("Interactive Form Builder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        // Initialize data
        components = new ArrayList<>();

        // Create components
        createComponents();

        // Set up event listeners
        setupEventListeners();

        // Display the frame
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createComponents() {
        // Create split pane for main content
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(300);

        // Left panel - component palette and property editor
        JPanel leftPanel = new JPanel(new BorderLayout());

        // Component palette
        componentPalette = new JPanel();
        componentPalette.setLayout(new BoxLayout(componentPalette, BoxLayout.Y_AXIS));
        componentPalette.setBorder(BorderFactory.createTitledBorder("Components"));

        // Add draggable components to palette
        addPaletteComponent("Label", "JLabel");
        addPaletteComponent("Text Field", "JTextField");
        addPaletteComponent("Button", "JButton");
        addPaletteComponent("Check Box", "JCheckBox");
        addPaletteComponent("Radio Button", "JRadioButton");
        addPaletteComponent("Combo Box", "JComboBox");

        JScrollPane paletteScrollPane = new JScrollPane(componentPalette);
        paletteScrollPane.setPreferredSize(new Dimension(280, 200));

        // Property editor
        propertyEditor = new JPanel();
        propertyEditor.setLayout(new BoxLayout(propertyEditor, BoxLayout.Y_AXIS));
        propertyEditor.setBorder(BorderFactory.createTitledBorder("Properties"));

        JScrollPane propertyScrollPane = new JScrollPane(propertyEditor);

        leftPanel.add(paletteScrollPane, BorderLayout.NORTH);
        leftPanel.add(propertyScrollPane, BorderLayout.CENTER);

        // Right panel - form design area and live preview
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Form design area
        formDesignArea = new JPanel();
        formDesignArea.setLayout(null);
        formDesignArea.setBackground(Color.WHITE);
        formDesignArea.setBorder(BorderFactory.createTitledBorder("Design Area"));

        // Enable drag and drop for form design area
        new DropTarget(formDesignArea, new FormDropTargetListener());

        JScrollPane designScrollPane = new JScrollPane(formDesignArea);

        // Live preview
        livePreview = new JPanel();
        livePreview.setLayout(new BoxLayout(livePreview, BoxLayout.Y_AXIS));
        livePreview.setBorder(BorderFactory.createTitledBorder("Live Preview"));
        livePreview.setBackground(Color.WHITE);

        JTabbedPane rightTabbedPane = new JTabbedPane();
        rightTabbedPane.addTab("Design", designScrollPane);
        rightTabbedPane.addTab("Preview", new JScrollPane(livePreview));

        rightPanel.add(rightTabbedPane, BorderLayout.CENTER);

        // Add panels to main split pane
        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(rightPanel);

        add(mainSplitPane, BorderLayout.CENTER);
    }

    private void addPaletteComponent(String label, String type) {
        JLabel componentLabel = new JLabel(label);
        componentLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        componentLabel.setOpaque(true);
        componentLabel.setBackground(Color.WHITE);
        componentLabel.setPreferredSize(new Dimension(250, 30));
        componentLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Make component draggable
        componentLabel.setTransferHandler(new TransferHandler("text"));
        componentLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, TransferHandler.COPY);
            }
        });

        // Store component type as client property
        componentLabel.putClientProperty("componentType", type);

        componentPalette.add(componentLabel);
        componentPalette.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private void setupEventListeners() {
        // Add keyboard shortcuts
        InputMap inputMap = formDesignArea.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = formDesignArea.getActionMap();

        // Delete selected component
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        actionMap.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedComponent != null) {
                    removeComponent(selectedComponent);
                }
            }
        });

        // Copy component
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copy");
        actionMap.put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedComponent != null) {
                    copyComponent(selectedComponent);
                }
            }
        });
    }

    private class FormDropTargetListener extends DropTargetAdapter {
        @Override
        public void drop(DropTargetDropEvent dtde) {
            try {
                Transferable transferable = dtde.getTransferable();
                if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);

                    // Get the drop location
                    Point dropPoint = dtde.getLocation();

                    // Get the component type from the dragged label
                    String labelText = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                    Component draggedComponent = null;

                    // Find the component in the palette that matches the label text
                    for (Component comp : componentPalette.getComponents()) {
                        if (comp instanceof JLabel && ((JLabel) comp).getText().equals(labelText)) {
                            draggedComponent = comp;
                            break;
                        }
                    }

                    if (draggedComponent != null) {
                        String componentType = (String) draggedComponent.getClientProperty("componentType");
                        addNewComponent(componentType, dropPoint.x, dropPoint.y);
                    }

                    dtde.dropComplete(true);
                } else {
                    dtde.rejectDrop();
                }
            } catch (Exception e) {
                e.printStackTrace();
                dtde.rejectDrop();
            }
        }
    }

    private void addNewComponent(String type, int x, int y) {
        FormComponent component = new FormComponent(type);
        component.setBounds(x, y, 150, 30);

        // Add mouse listener for selection
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectComponent(component);
                e.consume();
            }
        });

        // Add mouse motion listener for dragging
        component.addMouseMotionListener(new MouseMotionAdapter() {
            private Point offset;

            @Override
            public void mousePressed(MouseEvent e) {
                selectComponent(component);
                offset = new Point(e.getX(), e.getY());
                component.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (offset != null) {
                    int newX = component.getX() + e.getX() - offset.x;
                    int newY = component.getY() + e.getY() - offset.y;
                    component.setLocation(newX, newY);
                    updatePropertyEditor();
                    updateLivePreview();
                    formDesignArea.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                offset = null;
                component.setCursor(Cursor.getDefaultCursor());
            }
        });

        components.add(component);
        formDesignArea.add(component);
        selectComponent(component);
        updateLivePreview();
        formDesignArea.repaint();
    }

    private void selectComponent(FormComponent component) {
        // Deselect previously selected component
        if (selectedComponent != null) {
            selectedComponent.setSelected(false);
        }

        // Select new component
        selectedComponent = component;
        selectedComponent.setSelected(true);

        // Update property editor
        updatePropertyEditor();

        // Repaint
        formDesignArea.repaint();
    }

    private void updatePropertyEditor() {
        propertyEditor.removeAll();

        if (selectedComponent == null) {
            propertyEditor.add(new JLabel("No component selected"));
        } else {
            // Add property fields based on component type
            JLabel typeLabel = new JLabel("Type: " + selectedComponent.getType());
            propertyEditor.add(typeLabel);

            // Common properties for all components
            addPropertyField("Text", selectedComponent.getText(), newValue -> {
                selectedComponent.setText(newValue);
                updateLivePreview();
            });

            addPropertyField("X", String.valueOf(selectedComponent.getX()), newValue -> {
                try {
                    int x = Integer.parseInt(newValue);
                    selectedComponent.setLocation(x, selectedComponent.getY());
                    formDesignArea.repaint();
                    updateLivePreview();
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            });

            addPropertyField("Y", String.valueOf(selectedComponent.getY()), newValue -> {
                try {
                    int y = Integer.parseInt(newValue);
                    selectedComponent.setLocation(selectedComponent.getX(), y);
                    formDesignArea.repaint();
                    updateLivePreview();
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            });

            addPropertyField("Width", String.valueOf(selectedComponent.getWidth()), newValue -> {
                try {
                    int width = Integer.parseInt(newValue);
                    selectedComponent.setSize(width, selectedComponent.getHeight());
                    formDesignArea.repaint();
                    updateLivePreview();
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            });

            addPropertyField("Height", String.valueOf(selectedComponent.getHeight()), newValue -> {
                try {
                    int height = Integer.parseInt(newValue);
                    selectedComponent.setSize(selectedComponent.getWidth(), height);
                    formDesignArea.repaint();
                    updateLivePreview();
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            });

            // Type-specific properties
            if (selectedComponent.getType().equals("JLabel")) {
                addPropertyField("Font Style", "Plain", newValue -> {
                    int style = Font.PLAIN;
                    if (newValue.equals("Bold")) style = Font.BOLD;
                    else if (newValue.equals("Italic")) style = Font.ITALIC;
                    else if (newValue.equals("Bold Italic")) style = Font.BOLD | Font.ITALIC;

                    selectedComponent.setFontStyle(style);
                    updateLivePreview();
                });
            }
        }

        propertyEditor.revalidate();
        propertyEditor.repaint();
    }

    private void addPropertyField(String label, String initialValue, PropertyChangeListener listener) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label), BorderLayout.WEST);

        JTextField textField = new JTextField(initialValue);
        textField.addActionListener(e -> {
            listener.onPropertyChange(textField.getText());
        });
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                listener.onPropertyChange(textField.getText());
            }
        });

        panel.add(textField, BorderLayout.CENTER);
        propertyEditor.add(panel);
        propertyEditor.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private void updateLivePreview() {
        livePreview.removeAll();
        livePreview.setLayout(new BoxLayout(livePreview, BoxLayout.Y_AXIS));

        for (FormComponent comp : components) {
            JComponent previewComp = comp.createPreviewComponent();
            previewComp.setAlignmentX(Component.LEFT_ALIGNMENT);
            livePreview.add(previewComp);
            livePreview.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        livePreview.revalidate();
        livePreview.repaint();
    }

    private void removeComponent(FormComponent component) {
        components.remove(component);
        formDesignArea.remove(component);
        selectedComponent = null;
        updatePropertyEditor();
        updateLivePreview();
        formDesignArea.repaint();
    }

    private void copyComponent(FormComponent component) {
        FormComponent newComp = new FormComponent(component);
        newComp.setLocation(component.getX() + 20, component.getY() + 20);
        components.add(newComp);
        formDesignArea.add(newComp);
        selectComponent(newComp);
        updateLivePreview();
        formDesignArea.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormBuilder());
    }

    // Helper interfaces and classes
    interface PropertyChangeListener {
        void onPropertyChange(String newValue);
    }

    class FormComponent extends JComponent {
        private String type;
        private String text;
        private boolean selected;
        private int fontStyle;

        public FormComponent(String type) {
            this.type = type;
            this.text = type.replace("J", "");
            this.selected = false;
            this.fontStyle = Font.PLAIN;
            setOpaque(false);
        }

        public FormComponent(FormComponent other) {
            this.type = other.type;
            this.text = other.text;
            this.selected = false;
            this.fontStyle = other.fontStyle;
            setBounds(other.getBounds());
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            // Draw selection border
            if (selected) {
                g2d.setColor(Color.BLUE);
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));
                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }

            // Draw component preview
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));

            switch (type) {
                case "JLabel":
                    g2d.setFont(new Font("Arial", fontStyle, 12));
                    g2d.drawString(text, 5, getHeight() / 2 + 4);
                    break;
                case "JTextField":
                    g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                    g2d.drawString(text, 5, getHeight() / 2 + 4);
                    break;
                case "JButton":
                    g2d.setColor(new Color(240, 240, 240));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.GRAY);
                    g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                    g2d.drawString(text, getWidth() / 2 - text.length() * 3, getHeight() / 2 + 4);
                    break;
                case "JCheckBox":
                    g2d.drawRect(5, 5, 15, 15);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                    g2d.drawString(text, 25, getHeight() / 2 + 4);
                    break;
                case "JRadioButton":
                    g2d.drawOval(5, 5, 15, 15);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                    g2d.drawString(text, 25, getHeight() / 2 + 4);
                    break;
                case "JComboBox":
                    g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                    g2d.drawString(text, 5, getHeight() / 2 + 4);
                    g2d.fillPolygon(new int[]{getWidth() - 15, getWidth() - 10, getWidth() - 20},
                            new int[]{getHeight() / 2 - 5, getHeight() / 2 + 5, getHeight() / 2 + 5}, 3);
                    break;
            }
        }

        public JComponent createPreviewComponent() {
            switch (type) {
                case "JLabel":
                    JLabel label = new JLabel(text);
                    label.setFont(new Font("Arial", fontStyle, 12));
                    label.setPreferredSize(new Dimension(getWidth(), getHeight()));
                    return label;
                case "JTextField":
                    JTextField textField = new JTextField(text);
                    textField.setPreferredSize(new Dimension(getWidth(), getHeight()));
                    return textField;
                case "JButton":
                    JButton button = new JButton(text);
                    button.setPreferredSize(new Dimension(getWidth(), getHeight()));
                    return button;
                case "JCheckBox":
                    JCheckBox checkBox = new JCheckBox(text);
                    checkBox.setPreferredSize(new Dimension(getWidth(), getHeight()));
                    return checkBox;
                case "JRadioButton":
                    JRadioButton radioButton = new JRadioButton(text);
                    radioButton.setPreferredSize(new Dimension(getWidth(), getHeight()));
                    return radioButton;
                case "JComboBox":
                    JComboBox<String> comboBox = new JComboBox<>();
                    comboBox.addItem(text);
                    comboBox.setPreferredSize(new Dimension(getWidth(), getHeight()));
                    return comboBox;
                default:
                    return new JLabel(type);
            }
        }

        // Getters and setters
        public String getType() { return type; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; repaint(); }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; repaint(); }
        public void setFontStyle(int style) { this.fontStyle = style; repaint(); }
    }
}