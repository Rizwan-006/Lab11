import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
//import java.awt.event.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.io.*;

public class AttendanceSystem extends JFrame {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> dateCombo;
    private JComboBox<String> courseCombo;
    private ArrayList<Student> students = new ArrayList<>();
    private Map<String, ArrayList<AttendanceRecord>> attendanceRecords = new HashMap<>();
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JTextField searchField;

    // Student class to hold student data
    static class Student implements Serializable {
        String id;
        String name;

        Student(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    // Attendance record class
    static class AttendanceRecord implements Serializable {
        String date;
        String course;
        String status;

        AttendanceRecord(String date, String course, String status) {
            this.date = date;
            this.course = course;
            this.status = status;
        }
    }

    public AttendanceSystem() {
        setTitle("Student Attendance System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));

        // Initialize UI with modern look
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Card layout for switching between login and main panels
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        add(cardPanel);

        // Create panels
        createLoginPanel();
        createMainPanel();

        // Load initial data
        loadInitialStudents();
        loadAttendanceRecords();
    }

    private void createLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        loginPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Attendance System Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(titleLabel, gbc);

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JButton loginButton = new JButton("Login");
        styleButton(loginButton);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 3;
        loginPanel.add(loginButton, gbc);

        loginButton.addActionListener(e -> {
            if (usernameField.getText().equals("admin") && new String(passwordField.getPassword()).equals("admin123")) {
                cardLayout.show(cardPanel, "main");
                loadStudents();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cardPanel.add(loginPanel, "login");
    }

    private void createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(255, 255, 255));

        // Control panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize date combo with last 7 days
        dateCombo = new JComboBox<>(getRecentDates());
        dateCombo.setSelectedIndex(0); // Select today's date
        courseCombo = new JComboBox<>(new String[]{"CS101", "CS202", "CS303"});
        searchField = new JTextField(20);
        JButton saveButton = new JButton("Save Attendance");
        JButton reportButton = new JButton("Generate Report");
        JButton addStudentButton = new JButton("Add Student");
        styleButton(saveButton);
        styleButton(reportButton);
        styleButton(addStudentButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        controlPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(dateCombo, gbc);
        gbc.gridx = 2;
        controlPanel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 3;
        controlPanel.add(courseCombo, gbc);
        gbc.gridx = 4;
        controlPanel.add(new JLabel("Search:"), gbc);
        gbc.gridx = 5;
        controlPanel.add(searchField, gbc);
        gbc.gridx = 6;
        controlPanel.add(saveButton, gbc);
        gbc.gridx = 7;
        controlPanel.add(reportButton, gbc);
        gbc.gridx = 8;
        controlPanel.add(addStudentButton, gbc);

        // Table setup
        String[] columns = {"Student ID", "Name", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Only status column is editable
            }
        };
        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(25);
        studentTable.setGridColor(new Color(200, 200, 200));
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Present", "Absent", "Late"});
        studentTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusCombo));

        // Event listeners
        dateCombo.addActionListener(e -> {
            String selectedDate = (String) dateCombo.getSelectedItem();
            if (isFutureDate(selectedDate)) {
                JOptionPane.showMessageDialog(this, "Future dates are not allowed", "Error", JOptionPane.ERROR_MESSAGE);
                dateCombo.setSelectedIndex(0); // Reset to today
            } else {
                loadStudents();
            }
        });

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterStudents(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterStudents(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        saveButton.addActionListener(e -> saveAttendance());
        reportButton.addActionListener(e -> generateReport());
        addStudentButton.addActionListener(e -> addNewStudent());

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);
        cardPanel.add(mainPanel, "main");
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

    private boolean isFutureDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date selectedDate = sdf.parse(dateStr);
            Date today = new Date();
            return selectedDate.after(today);
        } catch (Exception e) {
            return false;
        }
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(66, 135, 245));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private void loadInitialStudents() {
        students.add(new Student("1", "John Doe"));
        students.add(new Student("2", "Jane Smith"));
        students.add(new Student("3", "Bob Johnson"));
        students.add(new Student("4", "Alice Brown"));
    }

    private void loadStudents() {
        tableModel.setRowCount(0);
        String selectedDate = (String) dateCombo.getSelectedItem();
        String selectedCourse = (String) courseCombo.getSelectedItem();

        for (Student student : students) {
            String status = "Present";
            String key = student.id + "_" + selectedDate + "_" + selectedCourse;
            ArrayList<AttendanceRecord> records = attendanceRecords.getOrDefault(student.id, new ArrayList<>());
            for (AttendanceRecord record : records) {
                if (record.date.equals(selectedDate) && record.course.equals(selectedCourse)) {
                    status = record.status;
                    break;
                }
            }
            tableModel.addRow(new Object[]{student.id, student.name, status});
        }
    }

    private void filterStudents() {
        String query = searchField.getText().toLowerCase();
        tableModel.setRowCount(0);
        for (Student student : students) {
            if (student.name.toLowerCase().contains(query) || student.id.contains(query)) {
                tableModel.addRow(new Object[]{student.id, student.name, "Present"});
            }
        }
    }

    private void saveAttendance() {
        String selectedDate = (String) dateCombo.getSelectedItem();
        String selectedCourse = (String) courseCombo.getSelectedItem();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String studentId = (String) tableModel.getValueAt(i, 0);
            String status = (String) tableModel.getValueAt(i, 2);
            String key = studentId + "_" + selectedDate + "_" + selectedCourse;

            ArrayList<AttendanceRecord> records = attendanceRecords.getOrDefault(studentId, new ArrayList<>());
            records.removeIf(r -> r.date.equals(selectedDate) && r.course.equals(selectedCourse));
            records.add(new AttendanceRecord(selectedDate, selectedCourse, status));
            attendanceRecords.put(studentId, records);
        }

        saveAttendanceToFile();
        JOptionPane.showMessageDialog(this, "Attendance saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void generateReport() {
        String selectedDate = (String) dateCombo.getSelectedItem();
        String selectedCourse = (String) courseCombo.getSelectedItem();
        StringBuilder report = new StringBuilder("<html><h2>Attendance Report</h2>");
        report.append("<p><b>Date:</b> ").append(selectedDate).append("</p>");
        report.append("<p><b>Course:</b> ").append(selectedCourse).append("</p><br>");
        report.append("<table border='1' cellpadding='5'>");
        report.append("<tr><th>Student ID</th><th>Name</th><th>Status</th></tr>");

        int present = 0, absent = 0, late = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String status = (String) tableModel.getValueAt(i, 2);
            report.append("<tr><td>")
                    .append(tableModel.getValueAt(i, 0))
                    .append("</td><td>")
                    .append(tableModel.getValueAt(i, 1))
                    .append("</td><td>")
                    .append(status)
                    .append("</td></tr>");
            switch (status) {
                case "Present": present++; break;
                case "Absent": absent++; break;
                case "Late": late++; break;
            }
        }
        report.append("</table><br>");
        report.append("<p><b>Summary:</b><br>");
        report.append("Present: ").append(present).append("<br>");
        report.append("Absent: ").append(absent).append("<br>");
        report.append("Late: ").append(late).append("</p></html>");

        JEditorPane reportPane = new JEditorPane("text/html", report.toString());
        reportPane.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(reportPane), "Attendance Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addNewStudent() {
        JTextField idField = new JTextField(10);
        JTextField nameField = new JTextField(20);
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Student ID:"));
        panel.add(idField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Student", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            if (!id.isEmpty() && !name.isEmpty()) {
                if (students.stream().noneMatch(s -> s.id.equals(id))) {
                    students.add(new Student(id, name));
                    saveStudentsToFile();
                    loadStudents();
                    JOptionPane.showMessageDialog(this, "Student added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Student ID already exists", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveAttendanceToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("attendance.dat"))) {
            oos.writeObject(attendanceRecords);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAttendanceRecords() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("attendance.dat"))) {
            attendanceRecords = (Map<String, ArrayList<AttendanceRecord>>) ois.readObject();
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, which is fine for first run
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading attendance: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveStudentsToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("students.dat"))) {
            oos.writeObject(students);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving students: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadStudentsFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("students.dat"))) {
            students = (ArrayList<Student>) ois.readObject();
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, which is fine for first run
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}