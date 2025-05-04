import javax.swing.*;
import java.awt.*;

public class TetrisBlockPanel extends JPanel {
    private static final int CELL_SIZE = 30;
    private static final Color[] COLORS = {
            Color.CYAN, Color.BLUE, Color.ORANGE, Color.YELLOW,
            Color.GREEN, Color.MAGENTA, Color.RED
    };

    private static final Point[][] SHAPES = {
            {new Point(0,0), new Point(1,0), new Point(2,0), new Point(3,0)},       // I
            {new Point(0,0), new Point(0,1), new Point(1,1), new Point(2,1)},       // J
            {new Point(2,0), new Point(0,1), new Point(1,1), new Point(2,1)},       // L
            {new Point(0,0), new Point(1,0), new Point(0,1), new Point(1,1)},       // O
            {new Point(1,0), new Point(2,0), new Point(0,1), new Point(1,1)},       // S
            {new Point(1,0), new Point(0,1), new Point(1,1), new Point(2,1)},       // T
            {new Point(0,0), new Point(1,0), new Point(1,1), new Point(2,1)}        // Z
    };

    public TetrisBlockPanel() {
        setPreferredSize(new Dimension(7 * CELL_SIZE * 2, 3 * CELL_SIZE));
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        for (int i = 0; i < SHAPES.length; i++) {
            Point[] shape = SHAPES[i];
            Color color = COLORS[i];
            int offsetX = i * CELL_SIZE * 2;
            int offsetY = CELL_SIZE;
            for (Point p : shape) {
                draw3DBlock(g2d, offsetX + p.x * CELL_SIZE, offsetY + p.y * CELL_SIZE, color);
            }
        }
    }

    private void draw3DBlock(Graphics g, int x, int y, Color baseColor) {
        int size = CELL_SIZE;

        // Main fill
        g.setColor(baseColor);
        g.fillRect(x, y, size, size);

        // Highlight
        g.setColor(baseColor.brighter());
        g.fillRect(x, y, size, size / 6); // top
        g.fillRect(x, y, size / 6, size); // left

        // Shadow
        g.setColor(baseColor.darker());
        g.fillRect(x, y + size - size / 6, size, size / 6); // bottom
        g.fillRect(x + size - size / 6, y, size / 6, size); // right

        // Border (optional)
        g.setColor(Color.BLACK);
        g.drawRect(x, y, size, size);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris Block Styles");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new TetrisBlockPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
