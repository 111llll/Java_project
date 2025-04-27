import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Tetris extends JPanel implements ActionListener, KeyListener {
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 18;
    private final int CELL_SIZE = 30;

    private Timer timer;
    private Piece currentPiece;
    private int pieceX = 4;
    private int pieceY = 0;
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];

    private Random random = new Random();

    public Tetris() {
        setPreferredSize(new Dimension(BOARD_WIDTH * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        spawnNewPiece();

        timer = new Timer(500, this);
        timer.start();
    }

    private void spawnNewPiece() {
        currentPiece = Piece.getRandomPiece();
        pieceX = 4;
        pieceY = 0;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i <= BOARD_WIDTH; i++) {
            g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE);
        }
        for (int i = 0; i <= BOARD_HEIGHT; i++) {
            g.drawLine(0, i * CELL_SIZE, BOARD_WIDTH * CELL_SIZE, i * CELL_SIZE);
        }

        // Draw fixed blocks
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] != 0) {
                    g.setColor(new Color(board[y][x]));
                    g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // Draw current falling piece
        g.setColor(currentPiece.color);
        for (Point p : currentPiece.shape) {
            g.fillRect((pieceX + p.x) * CELL_SIZE, (pieceY + p.y) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        pieceY++;
        if (isCollision()) {
            pieceY--;
            fixPiece();
            spawnNewPiece();
        }
        repaint();
    }

    private boolean isCollision() {
        for (Point p : currentPiece.shape) {
            int x = pieceX + p.x;
            int y = pieceY + p.y;
            if (x < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT) {
                return true;
            }
            if (y >= 0 && board[y][x] != 0) {
                return true;
            }
        }
        return false;
    }

    private void fixPiece() {
        for (Point p : currentPiece.shape) {
            int x = pieceX + p.x;
            int y = pieceY + p.y;
            if (y >= 0 && x >= 0 && x < BOARD_WIDTH && y < BOARD_HEIGHT) {
                board[y][x] = currentPiece.color.getRGB();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            pieceX--;
            if (isCollision()) {
                pieceX++;
            }
        } else if (key == KeyEvent.VK_RIGHT) {
            pieceX++;
            if (isCollision()) {
                pieceX--;
            }
        } else if (key == KeyEvent.VK_DOWN) {
            pieceY++;
            if (isCollision()) {
                pieceY--;
                fixPiece();
                spawnNewPiece();
            }
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris");
        Tetris tetris = new Tetris();
        frame.add(tetris);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Piece {
    public Point[] shape;
    public Color color;

    private static final Point[][] shapes = {
            {new Point(0,0), new Point(1,0), new Point(2,0), new Point(3,0)}, // I
            {new Point(0,0), new Point(0,1), new Point(1,1), new Point(2,1)}, // J
            {new Point(2,0), new Point(0,1), new Point(1,1), new Point(2,1)}, // L
            {new Point(0,0), new Point(1,0), new Point(0,1), new Point(1,1)}, // O
            {new Point(1,0), new Point(2,0), new Point(0,1), new Point(1,1)}, // S
            {new Point(1,0), new Point(0,1), new Point(1,1), new Point(2,1)}, // T
            {new Point(0,0), new Point(1,0), new Point(1,1), new Point(2,1)}  // Z
    };

    private static final Color[] colors = {
            Color.CYAN, Color.BLUE, Color.ORANGE, Color.YELLOW,
            Color.GREEN, Color.MAGENTA, Color.RED
    };

    public static Piece getRandomPiece() {
        int idx = new Random().nextInt(shapes.length);
        Piece p = new Piece();
        p.shape = shapes[idx];
        p.color = colors[idx];
        return p;
    }
}
