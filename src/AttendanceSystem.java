//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.awt.event.*;
//import com.toedter.calendar.JDateChooser;
//import java.util.ArrayList;
//import java.util.Date;
//import java.text.SimpleDateFormat;
//
//public class AttendanceSystem extends JFrame {
//    private JTable studentTable;
//    private DefaultTableModel tableModel;
//    private JDateChooser dateChooser;
//    private JComboBox<String> courseCombo;
//    private ArrayList<String[]> attendanceRecords = new ArrayList<>();
//
//    public AttendanceSystem() {
//        setTitle("Student Attendance System");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setSize(800, 600);
//        setLocationRelativeTo(null);
//
//        // Login panel
//        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 5, 5));
//        JTextField usernameField = new JTextField();
//        JPasswordField passwordField = new JPasswordField();
//        JButton loginButton = new JButton("Login");
//        loginPanel.add(new JLabel("Username:"));
//        loginPanel.add(usernameField);
//        loginPanel.add(new JLabel("Password:"));
//        loginPanel.add(passwordField);
//        loginPanel.add(new JLabel());
//        loginPanel.add(loginButton);
//
//        // Main panel
//        JPanel mainPanel = new JPanel(new BorderLayout());
//        tableModel = new DefaultTableModel(new String[]{"Student ID", "Name", "Status"}, 0);
//        studentTable = new JTable(tableModel);
//        dateChooser = new JDateChooser();
//        courseCombo = new JComboBox<>(new String[]{"Course 1", "Course 2", "Course 3"});
//        JTextField searchField = new JTextField(20);
//
//        // Control panel
//        JPanel controlPanel = new JPanel(new FlowLayout());
//        controlPanel.add(new JLabel("Date:"));
//        controlPanel.add(dateChooser);
//        controlPanel.add(new JLabel("Course:"));
//        controlPanel.add(courseCombo);
//        controlPanel.add(new JLabel("Search:"));
//        controlPanel.add(searchField);
//        JButton reportButton = new JButton("Generate Report");
//        controlPanel.add(reportButton);
//
//        // Card layout
//        CardLayout cardLayout = new CardLayout();
//        JPanel cardPanel = new JPanel(cardLayout);
//        cardPanel.add(loginPanel, "login");
//        cardPanel.add(mainPanel, "main");
//
//        // Event handling
//        loginButton.addActionListener(e -> {
//            if (usernameField.getText().equals("admin") && new String(passwordField.getPassword()).equals("admin")) {
//                cardLayout.show(cardPanel, "main");
//                loadStudents();
//            } else {
//                JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        });
//
//        dateChooser.addPropertyChangeListener("date", e -> {
//            if (dateChooser.getDate() != null && dateChooser.getDate().after(new Date())) {
//                JOptionPane.showMessageDialog(this, "Cannot select future dates", "Error", JOptionPane.ERROR_MESSAGE);
//                dateChooser.setDate(new Date());
//            }
//        });
//
//        reportButton.addActionListener(e -> generateReport());
//
//        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
//            @Override
//            public void insertUpdate(javax.swing.event.DocumentEvent e) {
//                filterStudents(searchField.getText());
//            }
//
//            @Override
//            public void removeUpdate(javax.swing.event.DocumentEvent e) {
//                filterStudents(searchField.getText());
//            }
//
//            @Override
//            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
//        });
//
//        mainPanel.add(controlPanel, BorderLayout.NORTH);
//        mainPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);
//
//        add(cardPanel);
//    }
//
//    private void loadStudents() {
//        tableModel.setRowCount(0);
//        tableModel.addRow(new Object[]{"1", "John Doe", "Present"});
//        tableModel.addRow(new Object[]{"2", "Jane Smith", "Present"});
//        tableModel.addRow(new Object[]{"3", "Bob Johnson", "Present"});
//    }
//
//    private void filterStudents(String query) {
//        tableModel.setRowCount(0);
//        String[] students = {"John Doe", "Jane Smith", "Bob Johnson"};
//        for (int i = 0; i < students.length; i++) {
//            if (students[i].toLowerCase().contains(query.toLowerCase())) {
//                tableModel.addRow(new Object[]{String.valueOf(i + 1), students[i], "Present"});
//            }
//        }
//    }
//
//    private void generateReport() {
//        StringBuilder report = new StringBuilder("Attendance Report\n\n");
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        report.append("Date: ").append(sdf.format(dateChooser.getDate())).append("\n");
//        report.append("Course: ").append(courseCombo.getSelectedItem()).append("\n\n");
//        for (int i = 0; i < tableModel.getRowCount(); i++) {
//            report.append("Student: ").append(tableModel.getValueAt(i, 1))
//                    .append(", Status: ").append(tableModel.getValueAt(i, 2)).append("\n");
//        }
//        JOptionPane.showMessageDialog(this, report.toString());
//    }
//}