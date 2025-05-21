// Save this as AttendanceSystem.java
// Updated UI version with modern styling using basic Swing components only

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class AttendanceSystem extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> dateCombo, courseCombo;
    private JTextField searchField;
    private ArrayList<Student> students = new ArrayList<>();
    private Map<String, ArrayList<AttendanceRecord>> attendanceRecords = new HashMap<>();

    static class Student implements Serializable {
        String id, name;
        Student(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class AttendanceRecord implements Serializable {
        String date, course, status;
        AttendanceRecord(String date, String course, String status) {
            this.date = date;
            this.course = course;
            this.status = status;
        }
    }

    public AttendanceSystem() {
        setTitle("Attendance System");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        add(cardPanel);

        createLoginPanel();
        createDashboardPanel();

        loadInitialStudents();
        loadAttendanceRecords();
    }

    private void createLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(230, 240, 250));
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel title = new JLabel("Admin Login");
        title.setFont(new Font("Verdana", Font.BOLD, 26));
        title.setForeground(new Color(40, 75, 135));

        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");
        styleButton(loginButton, new Color(30, 130, 76));

        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(title, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        loginPanel.add(loginButton, gbc);

        loginButton.addActionListener(e -> {
            if (usernameField.getText().equals("admin") && new String(passwordField.getPassword()).equals("admin123")) {
                cardLayout.show(cardPanel, "dashboard");
                loadStudents();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cardPanel.add(loginPanel, "login");
    }

    private void createDashboardPanel() {
        JPanel dashboard = new JPanel(new BorderLayout());

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(30, 50, 90));
        sidebar.setPreferredSize(new Dimension(180, getHeight()));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel sideTitle = new JLabel("Attendance");
        sideTitle.setForeground(Color.WHITE);
        sideTitle.setFont(new Font("Arial", Font.BOLD, 22));
        sideTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sideTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        sidebar.add(sideTitle);

        JButton saveButton = new JButton("Save");
        JButton reportButton = new JButton("Report");
        JButton addButton = new JButton("Add Student");
        styleButton(saveButton, new Color(52, 152, 219));
        styleButton(reportButton, new Color(243, 156, 18));
        styleButton(addButton, new Color(46, 204, 113));

        sidebar.add(saveButton);
        sidebar.add(reportButton);
        sidebar.add(addButton);

        dashboard.add(sidebar, BorderLayout.WEST);

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top controls
        JPanel topPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        dateCombo = new JComboBox<>(getRecentDates());
        courseCombo = new JComboBox<>(new String[]{"CS101", "CS202", "CS303"});
        searchField = new JTextField();

        topPanel.add(labeledPanel("Date", dateCombo));
        topPanel.add(labeledPanel("Course", courseCombo));
        topPanel.add(labeledPanel("Search", searchField));

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Student ID", "Name", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 2; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"Present", "Absent", "Late"})));

        JScrollPane tableScroll = new JScrollPane(table);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        dashboard.add(mainPanel, BorderLayout.CENTER);
        cardPanel.add(dashboard, "dashboard");

        // Actions
        saveButton.addActionListener(e -> saveAttendance());
        reportButton.addActionListener(e -> generateReport());
        addButton.addActionListener(e -> addNewStudent());
        dateCombo.addActionListener(e -> loadStudents());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
    }

    private JPanel labeledPanel(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void styleButton(JButton button, Color color) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(140, 35));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private String[] getRecentDates() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String[] dates = new String[7];
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            dates[i] = sdf.format(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        return dates;
    }

    private void loadInitialStudents() {
        students.add(new Student("1", "John Doe"));
        students.add(new Student("2", "Jane Smith"));
        students.add(new Student("3", "Ali Khan"));
        students.add(new Student("4", "Sara Ahmed"));
    }

    private void loadStudents() {
        tableModel.setRowCount(0);
        String date = (String) dateCombo.getSelectedItem();
        String course = (String) courseCombo.getSelectedItem();

        for (Student s : students) {
            String status = "Present";
            for (AttendanceRecord ar : attendanceRecords.getOrDefault(s.id, new ArrayList<>())) {
                if (ar.date.equals(date) && ar.course.equals(course)) {
                    status = ar.status;
                    break;
                }
            }
            tableModel.addRow(new Object[]{s.id, s.name, status});
        }
    }

    private void filter() {
        String q = searchField.getText().toLowerCase();
        tableModel.setRowCount(0);
        for (Student s : students) {
            if (s.name.toLowerCase().contains(q) || s.id.contains(q)) {
                tableModel.addRow(new Object[]{s.id, s.name, "Present"});
            }
        }
    }

    private void saveAttendance() {
        String date = (String) dateCombo.getSelectedItem();
        String course = (String) courseCombo.getSelectedItem();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String id = (String) tableModel.getValueAt(i, 0);
            String status = (String) tableModel.getValueAt(i, 2);
            ArrayList<AttendanceRecord> list = attendanceRecords.getOrDefault(id, new ArrayList<>());
            list.removeIf(r -> r.date.equals(date) && r.course.equals(course));
            list.add(new AttendanceRecord(date, course, status));
            attendanceRecords.put(id, list);
        }
        saveAttendanceToFile();
        JOptionPane.showMessageDialog(this, "Attendance saved!");
    }

    private void generateReport() {
        String date = (String) dateCombo.getSelectedItem();
        String course = (String) courseCombo.getSelectedItem();
        StringBuilder report = new StringBuilder("<html><h2>Attendance Report</h2>");
        report.append("<p><b>Date:</b> ").append(date).append("<br><b>Course:</b> ").append(course).append("</p>");
        report.append("<table border='1' cellpadding='5'><tr><th>ID</th><th>Name</th><th>Status</th></tr>");

        int present = 0, absent = 0, late = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String id = (String) tableModel.getValueAt(i, 0);
            String name = (String) tableModel.getValueAt(i, 1);
            String status = (String) tableModel.getValueAt(i, 2);
            report.append("<tr><td>").append(id).append("</td><td>").append(name).append("</td><td>").append(status).append("</td></tr>");
            switch (status) {
                case "Present" -> present++;
                case "Absent" -> absent++;
                case "Late" -> late++;
            }
        }
        report.append("</table><p><b>Summary:</b><br>Present: ").append(present)
                .append("<br>Absent: ").append(absent)
                .append("<br>Late: ").append(late).append("</p></html>");

        JEditorPane editorPane = new JEditorPane("text/html", report.toString());
        editorPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(this, scrollPane, "Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addNewStudent() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Student ID:"));
        panel.add(idField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);

        if (JOptionPane.showConfirmDialog(this, panel, "Add Student", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            if (!id.isEmpty() && !name.isEmpty()) {
                if (students.stream().noneMatch(s -> s.id.equals(id))) {
                    students.add(new Student(id, name));
                    loadStudents();
                } else {
                    JOptionPane.showMessageDialog(this, "Student ID already exists.");
                }
            }
        }
    }

    private void saveAttendanceToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("attendance.dat"))) {
            oos.writeObject(attendanceRecords);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving attendance.");
        }
    }

    private void loadAttendanceRecords() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("attendance.dat"))) {
            attendanceRecords = (Map<String, ArrayList<AttendanceRecord>>) ois.readObject();
        } catch (Exception ignored) {}
    }
}
