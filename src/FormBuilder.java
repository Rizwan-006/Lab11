import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class FormBuilder extends JFrame {
    private JPanel previewPanel;
    private ArrayList<JComponent> formComponents = new ArrayList<>();
    private JComponent selectedComponent;

    public FormBuilder() {
        setTitle("Interactive Form Builder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);

        // Properties panel
        JPanel propertiesPanel = new JPanel(new BorderLayout());
        JPanel componentPalette = new JPanel(new GridLayout(5, 1, 5, 5));
        JButton labelButton = new JButton("Add Label");
        JButton textFieldButton = new JButton("Add Text Field");
        JButton checkboxButton = new JButton("Add Checkbox");
        JButton radioButton = new JButton("Add Radio Button");
        JButton comboBoxButton = new JButton("Add Dropdown");
        componentPalette.add(labelButton);
        componentPalette.add(textFieldButton);
        componentPalette.add(checkboxButton);
        componentPalette.add(radioButton);
        componentPalette.add(comboBoxButton);

        JPanel propertiesConfig = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(15);
        JCheckBox requiredCheck = new JCheckBox("Required");
        JTextField regexField = new JTextField(15);
        JButton applyButton = new JButton("Apply Changes");

        gbc.gridx = 0;
        gbc.gridy = 0;
        propertiesConfig.add(new JLabel("Component Name:"), gbc);
        gbc.gridx = 1;
        propertiesConfig.add(nameField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        propertiesConfig.add(requiredCheck, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        propertiesConfig.add(new JLabel("Validation Regex:"), gbc);
        gbc.gridx = 1;
        propertiesConfig.add(regexField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        propertiesConfig.add(applyButton, gbc);

        propertiesPanel.add(componentPalette, BorderLayout.NORTH);
        propertiesPanel.add(propertiesConfig, BorderLayout.CENTER);

        // Preview panel
        previewPanel = new JPanel(null);
        previewPanel.setBackground(Color.WHITE);

        // Drag and drop
        previewPanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return true;
            }

            @Override
            public boolean importData(TransferSupport support) {
                Point p = support.getDropLocation().getDropPoint();
                JComponent comp = createComponent(currentTool);
                comp.setBounds(p.x, p.y, 150, 30);
                previewPanel.add(comp);
                formComponents.add(comp);
                previewPanel.revalidate();
                previewPanel.repaint();
                return true;
            }
        });

        // Component selection and movement
        previewPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedComponent = (JComponent) previewPanel.getComponentAt(e.getPoint());
                if (selectedComponent != null) {
                    nameField.setText(selectedComponent.getName());
                    requiredCheck.setSelected(Boolean.parseBoolean((String) selectedComponent.getClientProperty("required")));
                    regexField.setText((String) selectedComponent.getClientProperty("regex"));
                }
            }
        });

        previewPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedComponent != null) {
                    selectedComponent.setLocation(e.getX() - selectedComponent.getWidth() / 2,
                            e.getY() - selectedComponent.getHeight() / 2);
                    previewPanel.repaint();
                }
            }
        });

        // Component creation
        labelButton.addActionListener(e -> currentTool = "label");
        textFieldButton.addActionListener(e -> currentTool = "textField");
        checkboxButton.addActionListener(e -> currentTool = "checkbox");
        radioButton.addActionListener(e -> currentTool = "radio");
        comboBoxButton.addActionListener(e -> currentTool = "comboBox");

        applyButton.addActionListener(e -> {
            if (selectedComponent != null) {
                selectedComponent.setName(nameField.getText());
                selectedComponent.putClientProperty("required", requiredCheck.isSelected());
                selectedComponent.putClientProperty("regex", regexField.getText());
            }
        });

        splitPane.setLeftComponent(propertiesPanel);
        splitPane.setRightComponent(new JScrollPane(previewPanel));
        add(splitPane);
    }

    private String currentTool = "label";

    private JComponent createComponent(String type) {
        return switch (type) {
            case "label" -> new JLabel("Label");
            case "textField" -> new JTextField();
            case "checkbox" -> new JCheckBox("Checkbox");
            case "radio" -> new JRadioButton("Radio");
            case "comboBox" -> new JComboBox<>(new String[]{"Option 1", "Option 2", "Option 3"});
            default -> new JLabel("Unknown");
        };
    }
}