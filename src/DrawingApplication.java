import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class DrawingApplication extends JFrame {
    private JPanel canvas;
    private Color currentColor = Color.BLACK;
    private String currentTool = "pencil";
    private int strokeWidth = 2;
    private ArrayList<Shape> shapes = new ArrayList<>();
    private ArrayList<Shape> undoneShapes = new ArrayList<>();
    private Point startPoint;

    static class Shape {
        String type;
        Point start, end;
        Color color;
        int stroke;

        Shape(String type, Point start, Point end, Color color, int stroke) {
            this.type = type;
            this.start = start;
            this.end = end;
            this.color = color;
            this.stroke = stroke;
        }
    }

    public DrawingApplication() {
        setTitle("Drawing Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton pencilButton = new JButton("Pencil");
        JButton lineButton = new JButton("Line");
        JButton rectangleButton = new JButton("Rectangle");
        JButton ellipseButton = new JButton("Ellipse");
        JSlider strokeSlider = new JSlider(1, 10, 2);
        JButton undoButton = new JButton("Undo");
        JButton redoButton = new JButton("Redo");
        JButton colorButton = new JButton("Color");

        toolBar.add(pencilButton);
        toolBar.add(lineButton);
        toolBar.add(rectangleButton);
        toolBar.add(ellipseButton);
        toolBar.add(new JLabel("Stroke:"));
        toolBar.add(strokeSlider);
        toolBar.add(undoButton);
        toolBar.add(redoButton);
        toolBar.add(colorButton);

        // Create canvas
        canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (Shape shape : shapes) {
                    g2d.setColor(shape.color);
                    g2d.setStroke(new BasicStroke(shape.stroke));
                    drawShape(g2d, shape);
                }
            }
        };
        canvas.setBackground(Color.WHITE);

        // Mouse listeners
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (startPoint != null) {
                    shapes.add(new Shape(currentTool, startPoint, e.getPoint(), currentColor, strokeWidth));
                    canvas.repaint();
                    undoneShapes.clear();
                    startPoint = null;
                }
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentTool.equals("pencil")) {
                    shapes.add(new Shape("pencil", e.getPoint(), e.getPoint(), currentColor, strokeWidth));
                    canvas.repaint();
                }
            }
        });

        // Action listeners
        pencilButton.addActionListener(e -> currentTool = "pencil");
        lineButton.addActionListener(e -> currentTool = "line");
        rectangleButton.addActionListener(e -> currentTool = "rectangle");
        ellipseButton.addActionListener(e -> currentTool = "ellipse");
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Color", currentColor);
            if (newColor != null) currentColor = newColor;
        });
        strokeSlider.addChangeListener(e -> strokeWidth = strokeSlider.getValue());
        undoButton.addActionListener(e -> {
            if (!shapes.isEmpty()) {
                undoneShapes.add(shapes.remove(shapes.size() - 1));
                canvas.repaint();
            }
        });
        redoButton.addActionListener(e -> {
            if (!undoneShapes.isEmpty()) {
                shapes.add(undoneShapes.remove(undoneShapes.size() - 1));
                canvas.repaint();
            }
        });

        // Keyboard shortcuts
        canvas.setFocusable(true);
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_P -> currentTool = "pencil";
                    case KeyEvent.VK_L -> currentTool = "line";
                    case KeyEvent.VK_R -> currentTool = "rectangle";
                    case KeyEvent.VK_E -> currentTool = "ellipse";
                    case KeyEvent.VK_Z -> {
                        if (e.isControlDown() && !shapes.isEmpty()) {
                            undoneShapes.add(shapes.remove(shapes.size() - 1));
                            canvas.repaint();
                        }
                    }
                    case KeyEvent.VK_Y -> {
                        if (e.isControlDown() && !undoneShapes.isEmpty()) {
                            shapes.add(undoneShapes.remove(undoneShapes.size() - 1));
                            canvas.repaint();
                        }
                    }
                }
            }
        });

        add(toolBar, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
    }

    private void drawShape(Graphics2D g2d, Shape shape) {
        int x = Math.min(shape.start.x, shape.end.x);
        int y = Math.min(shape.start.y, shape.end.y);
        int width = Math.abs(shape.start.x - shape.end.x);
        int height = Math.abs(shape.start.y - shape.end.y);

        switch (shape.type) {
            case "pencil":
                g2d.fillOval(shape.start.x - shape.stroke / 2, shape.start.y - shape.stroke / 2, shape.stroke, shape.stroke);
                break;
            case "line":
                g2d.drawLine(shape.start.x, shape.start.y, shape.end.x, shape.end.y);
                break;
            case "rectangle":
                g2d.drawRect(x, y, width, height);
                break;
            case "ellipse":
                g2d.drawOval(x, y, width, height);
                break;
        }
    }
}