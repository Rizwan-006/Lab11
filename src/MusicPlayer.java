import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;

public class MusicPlayer extends JFrame {
    private JList<String> playlist;
    private DefaultListModel<String> playlistModel;
    private JSlider volumeSlider;
    private JSlider progressSlider;
    private JLabel trackInfo;
    private boolean isDarkMode = false;

    public MusicPlayer() {
        setTitle("Music Player");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Create components
        playlistModel = new DefaultListModel<>();
        playlistModel.addElement("Track 1");
        playlistModel.addElement("Track 2");
        playlistModel.addElement("Track 3");
        playlist = new JList<>(playlistModel);
        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        progressSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        trackInfo = new JLabel("No track selected");

        // Control panel
        JPanel controlPanel = new JPanel();
        JButton playButton = new JButton("Play");
        JButton pauseButton = new JButton("Pause");
        JButton stopButton = new JButton("Stop");
        JButton nextButton = new JButton("Next");
        JButton prevButton = new JButton("Previous");
        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stopButton);
        controlPanel.add(prevButton);
        controlPanel.add(nextButton);

        // Playlist management
        JPanel managementPanel = new JPanel();
        JButton addButton = new JButton("Add Track");
        JButton removeButton = new JButton("Remove Track");
        JButton sortButton = new JButton("Sort");
        JButton themeButton = new JButton("Toggle Theme");
        managementPanel.add(addButton);
        managementPanel.add(removeButton);
        managementPanel.add(sortButton);
        managementPanel.add(themeButton);

        // Layout
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(controlPanel, BorderLayout.NORTH);
        southPanel.add(new JLabel("Volume:"), BorderLayout.WEST);
        southPanel.add(volumeSlider, BorderLayout.CENTER);
        southPanel.add(progressSlider, BorderLayout.SOUTH);

        add(new JScrollPane(playlist), BorderLayout.CENTER);
        add(managementPanel, BorderLayout.NORTH);
        add(southPanel, BorderLayout.SOUTH);
        add(trackInfo, BorderLayout.WEST);

        // Event handling
        playButton.addActionListener(e -> trackInfo.setText("Playing: " + playlist.getSelectedValue()));
        pauseButton.addActionListener(e -> trackInfo.setText("Paused: " + playlist.getSelectedValue()));
        stopButton.addActionListener(e -> trackInfo.setText("Stopped"));
        nextButton.addActionListener(e -> {
            int index = playlist.getSelectedIndex();
            if (index < playlistModel.size() - 1) {
                playlist.setSelectedIndex(index + 1);
                trackInfo.setText("Playing: " + playlist.getSelectedValue());
            }
        });
        prevButton.addActionListener(e -> {
            int index = playlist.getSelectedIndex();
            if (index > 0) {
                playlist.setSelectedIndex(index - 1);
                trackInfo.setText("Playing: " + playlist.getSelectedValue());
            }
        });

        addButton.addActionListener(e -> {
            String track = JOptionPane.showInputDialog(this, "Enter track name:");
            if (track != null && !track.trim().isEmpty()) {
                playlistModel.addElement(track);
            }
        });
        removeButton.addActionListener(e -> {
            int index = playlist.getSelectedIndex();
            if (index >= 0) {
                playlistModel.remove(index);
            }
        });
        sortButton.addActionListener(e -> {
            ArrayList<String> tracks = Collections.list(playlistModel.elements());
            Collections.sort(tracks);
            playlistModel.clear();
            tracks.forEach(playlistModel::addElement);
        });
        themeButton.addActionListener(e -> {
            isDarkMode = !isDarkMode;
            updateTheme();
        });

        playlist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    trackInfo.setText("Playing: " + playlist.getSelectedValue());
                }
            }
        });

        // Context menu
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem playItem = new JMenuItem("Play");
        popupMenu.add(playItem);
        playlist.setComponentPopupMenu(popupMenu);
        playItem.addActionListener(e -> trackInfo.setText("Playing: " + playlist.getSelectedValue()));
    }

    private void updateTheme() {
        Color bgColor = isDarkMode ? Color.DARK_GRAY : Color.WHITE;
        Color fgColor = isDarkMode ? Color.WHITE : Color.BLACK;
        getContentPane().setBackground(bgColor);
        playlist.setBackground(bgColor);
        playlist.setForeground(fgColor);
        trackInfo.setForeground(fgColor);
    }
}