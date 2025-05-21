//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.awt.event.*;
//import java.util.Random;
//
//public class InteractiveDashboard extends JFrame {
//    private JPanel chartPanel;
//    private JTable dataTable;
//    private DefaultTableModel tableModel;
//    private Timer updateTimer;
//    private JLabel memoryLabel;
//    private JLabel statusLabel;
//
//    public InteractiveDashboard() {
//        setTitle("Interactive Dashboard");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setSize(800, 600);
//        setLocationRelativeTo(null);
//
//        // Navigation tabs
//        JTabbedPane tabbedPane = new JTabbedPane();
//        chartPanel = new JPanel();
//        tableModel = new DefaultTableModel(new String[]{"Metric", "Value"}, 0);
//        dataTable = new JTable(tableModel);
//        JPanel tablePanel = new JPanel(new BorderLayout());
//        tablePanel.add(new JScrollPane(dataTable), BorderLayout.CENTER);
//
//        tabbedPane.addTab("Charts", chartPanel);
//        tabbedPane.addTab("Data", tablePanel);
//
//        // Control panel
//        JPanel controlPanel = new JPanel(new FlowLayout());
//        JSlider filterSlider = new JSlider(0, 100, 50);
//        JCheckBox autoUpdateCheck = new JCheckBox("Auto Update");
//        controlPanel.add(new JLabel("Filter:"));
//        controlPanel.add(filterSlider);
//        controlPanel.add(autoUpdateCheck);
//
//        // Status panel
//        JPanel statusPanel = new JPanel(new BorderLayout());
//        memoryLabel = new JLabel("Memory: 0 MB");
//        statusLabel = new JLabel("Status: Running");
//        statusPanel.add(memoryLabel, BorderLayout.WEST);
//        statusPanel.add(statusLabel, BorderLayout.EAST);
//
//        // Chart
//        updateChart();
//
//        // Timer for updates
//        updateTimer = new Timer(5000, e -> {
//            if (autoUpdateCheck.isSelected()) {
//                updateChart();
//                updateTable();
//                updateMemory();
//            }
//        });
//        updateTimer.start();
//
//        // Event handling
//        filterSlider.addChangeListener(e -> updateChart());
//        dataTable.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() == 2) {
//                    int row = dataTable.getSelectedRow();
//                    JOptionPane.showMessageDialog(InteractiveDashboard.this,
//                            "Details for " + tableModel.getValueAt(row, 0) + ": " + tableModel.getValueAt(row, 1));
//                }
//            }
//        });
//
//        add(tabbedPane, BorderLayout.CENTER);
//        add(controlPanel, BorderLayout.NORTH);
//        add(statusPanel, BorderLayout.SOUTH);
//    }
//
//    private void updateChart() {
//        Random rand = new Random();
//        int[] data = new int[5];
//        for (int i = 0; i < 5; i++) {
//            data[i] = rand.nextInt(100);
//        }
//
//        ```chartjs
//        {
//            "type": "bar",
//                "data": {
//            "labels": ["Metric 1", "Metric 2", "Metric 3", "Metric 4", "Metric 5"],
//            "datasets": [{
//                "label": "Performance",
//                        "data": [${data[0]}, ${data[1]}, ${data[2]}, ${data[3]}, ${data[4]}],
//                "backgroundColor": ["#2196F3", "#4CAF50", "#F44336", "#FFC107", "#9C27B0"],
//                "borderColor": ["#1976D2", "#388E3C", "#D32F2F", "#FFA000", "#7B1FA2"],
//                "borderWidth": 1
//            }]
//        },
//            "options": {
//            "scales": {
//                "y": {
//                    "beginAtZero": true
//                }
//            }
//        }
//        }
//    }
//
//    private void updateTable() {
//        Random rand = new Random();
//        tableModel.setRowCount(0);
//        for (int i = 1; i <= 5; i++) {
//            tableModel.addRow(new Object[]{"Metric " + i, rand.nextInt(100)});
//        }
//    }
//
//    private void updateMemory() {
//        Runtime runtime = Runtime.getRuntime();
//        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
//        memoryLabel.setText("Memory: " + usedMemory + " MB");
//    }
//}