import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.regex.Pattern;

public class FormWizard extends JFrame {
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private HashMap<String, String> formData = new HashMap<>();
    private JProgressBar progressBar;
    private int currentStep = 1;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public FormWizard() {
        setTitle("Form Wizard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Step 1: Personal Information
        JPanel step1 = createPersonalInfoPanel();
        cardPanel.add(step1, "Step 1");

        // Step 2: Contact Information
        JPanel step2 = createContactInfoPanel();
        cardPanel.add(step2, "Step 2");

        // Step 3: Summary
        JPanel step3 = createSummaryPanel();
        cardPanel.add(step3, "Step 3");

        // Navigation panel
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        progressBar = new JProgressBar(0, 3);
        progressBar.setValue(1);
        progressBar.setStringPainted(true);
        progressBar.setString("Step 1 of 3");

        navPanel.add(prevButton);
        navPanel.add(progressBar);
        navPanel.add(nextButton);

        prevButton.addActionListener(e -> {
            if (currentStep > 1) {
                currentStep--;
                cardLayout.previous(cardPanel);
                updateProgress();
            }
        });

        nextButton.addActionListener(e -> {
            if (validateStep(currentStep)) {
                saveStepData(currentStep);
                if (currentStep < 3) {
                    currentStep++;
                    cardLayout.next(cardPanel);
                    updateProgress();
                } else {
                    displaySummary();
                }
            }
        });

        add(cardPanel, BorderLayout.CENTER);
        add(navPanel, BorderLayout.SOUTH);
    }

    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JTextField ageField = new JTextField(20);
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JLabel nameError = new JLabel();
        nameError.setForeground(Color.RED);
        JLabel ageError = new JLabel();
        ageError.setForeground(Color.RED);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        gbc.gridx = 2;
        panel.add(nameError, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1;
        panel.add(ageField, gbc);
        gbc.gridx = 2;
        panel.add(ageError, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1;
        panel.add(genderCombo, gbc);

        nameField.setName("name");
        ageField.setName("age");
        genderCombo.setName("gender");
        nameError.setName("nameError");
        ageError.setName("ageError");

        return panel;
    }

    private JPanel createContactInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextArea addressArea = new JTextArea(4, 20);
        JLabel emailError = new JLabel();
        emailError.setForeground(Color.RED);
        JLabel phoneError = new JLabel();
        phoneError.setForeground(Color.RED);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);
        gbc.gridx = 2;
        panel.add(emailError, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);
        gbc.gridx = 2;
        panel.add(phoneError, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(new JScrollPane(addressArea), gbc);

        emailField.setName("email");
        phoneField.setName("phone");
        addressArea.setName("address");
        emailError.setName("emailError");
        phoneError.setName("phoneError");

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        panel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
        summaryArea.setName("summary");
        return panel;
    }

    private boolean validateStep(int step) {
        JPanel panel = (JPanel) cardPanel.getComponent(step - 1);
        boolean isValid = true;

        if (step == 1) {
            JTextField nameField = (JTextField) panel.getComponents()[1];
            JTextField ageField = (JTextField) panel.getComponents()[4];
            JLabel nameError = (JLabel) panel.getComponents()[2];
            JLabel ageError = (JLabel) panel.getComponents()[5];

            if (nameField.getText().trim().isEmpty()) {
                nameError.setText("Name is required");
                isValid = false;
            }
            try {
                int age = Integer.parseInt(ageField.getText().trim());
                if (age < 0 || age > 150) {
                    ageError.setText("Invalid age");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                ageError.setText("Age must be a number");
                isValid = false;
            }
        } else if (step == 2) {
            JTextField emailField = (JTextField) panel.getComponents()[1];
            JTextField phoneField = (JTextField) panel.getComponents()[4];
            JLabel emailError = (JLabel) panel.getComponents()[2];
            JLabel phoneError = (JLabel) panel.getComponents()[5];

            if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
                emailError.setText("Invalid email format");
                isValid = false;
            }
            if (!phoneField.getText().trim().matches("\\d{11}")) {
                phoneError.setText("Must be 11 digits");
                isValid = false;
            }
        }
        return isValid;
    }

    private void saveStepData(int step) {
        JPanel panel = (JPanel) cardPanel.getComponent(step - 1);
        if (step == 1) {
            JTextField nameField = (JTextField) panel.getComponents()[1];
            JTextField ageField = (JTextField) panel.getComponents()[4];
            JComboBox<?> genderCombo = (JComboBox<?>) panel.getComponents()[7];
            formData.put("name", nameField.getText().trim());
            formData.put("age", ageField.getText().trim());
            formData.put("gender", (String) genderCombo.getSelectedItem());
        } else if (step == 2) {
            JTextField emailField = (JTextField) panel.getComponents()[1];
            JTextField phoneField = (JTextField) panel.getComponents()[4];
            JTextArea addressArea = (JTextArea) ((JScrollPane) panel.getComponents()[7]).getViewport().getView();
            formData.put("email", emailField.getText().trim());
            formData.put("phone", phoneField.getText().trim());
            formData.put("address", addressArea.getText().trim());
        }
    }

    private void displaySummary() {
        JPanel panel = (JPanel) cardPanel.getComponent(2);
        JTextArea summaryArea = (JTextArea) ((JScrollPane) panel.getComponent(0)).getViewport().getView();
        StringBuilder summary = new StringBuilder("Form Summary:\n\n");
        formData.forEach((key, value) -> summary.append(key.substring(0, 1).toUpperCase())
                .append(key.substring(1)).append(": ").append(value).append("\n"));
        summaryArea.setText(summary.toString());
    }

    private void updateProgress() {
        progressBar.setValue(currentStep);
        progressBar.setString("Step " + currentStep + " of 3");
    }
}