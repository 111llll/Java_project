import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.Arrays;

public class Tetris extends JPanel implements ActionListener, KeyListener {
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 18;
    private final int CELL_SIZE = 30;

    private Timer timer;
    private Piece currentPiece;
    private int pieceX = 4;
    private int pieceY = 0;
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private int score = 0;
    private boolean gameOver = false;
    private BgmPlayer bgm;
    private Random random = new Random();
    private boolean isPaused = false;


    public Tetris() {
        setPreferredSize(new Dimension((BOARD_WIDTH + 5) * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        bgm = new BgmPlayer();
        bgm.playAllLooped();

        spawnNewPiece();

        timer = new Timer(700, this);
        timer.start();
    }

    private void spawnNewPiece() {
        currentPiece = Piece.getRandomPiece();
        pieceX = 4;
        pieceY = 0;
        if (isCollision()) {
            gameOver = true;
            timer.stop();
            SfxPlayer.play("Sfx/gg.wav");

            new Thread(() -> {
                try {
                    Thread.sleep(3800);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                SwingUtilities.invokeLater(() -> showRestartDialog());
            }).start();
        }
    }

    private void showRestartDialog() {
        int option = JOptionPane.showOptionDialog(
                this,
                "得分:" + score + "\n是否重新遊戲?",
                "遊戲結束",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"重新", "退出"},
                "Restart"
        );

        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }

    private void resetGame() {
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        score = 0;
        gameOver = false;
        spawnNewPiece();
        timer.start();
        repaint();
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

        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] != 0) {
                    g.setColor(new Color(board[y][x]));
                    draw3DBlock(g, x * CELL_SIZE, y * CELL_SIZE, new Color(board[y][x]));
                }
            }
        }

        if (!gameOver) {
            g.setColor(currentPiece.color);
            for (Point p : currentPiece.shape) {
                draw3DBlock(g, (pieceX + p.x) * CELL_SIZE, (pieceY + p.y) * CELL_SIZE, currentPiece.color);
            }
        }

        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, BOARD_WIDTH * CELL_SIZE + 20, 30);
        if (gameOver) {
            g.setColor(Color.RED);
            g.drawString("GAME OVER", BOARD_WIDTH * CELL_SIZE + 20, 60);
        }
        if (isPaused && !gameOver) {
            g.setColor(Color.YELLOW);
            g.drawString("PAUSED", BOARD_WIDTH * CELL_SIZE + 20, 80);
        }
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        pieceY++;
        if (isCollision()) {
            pieceY--;
            fixPiece();
            clearLines();
            spawnNewPiece();
        }
        repaint();
    }

    private boolean isCollision() {
        for (Point p : currentPiece.shape) {
            int x = pieceX + p.x;
            int y = pieceY + p.y;
            if (x < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT) return true;
            if (y >= 0 && board[y][x] != 0) return true;
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

    private void draw3DBlock(Graphics g, int x, int y, Color baseColor) {
        int size = CELL_SIZE;

        g.setColor(baseColor);
        g.fillRect(x, y, size, size);

        g.setColor(baseColor.brighter());
        g.fillRect(x, y, size, size / 6);
        g.fillRect(x, y, size / 6, size);

        g.setColor(baseColor.darker());
        g.fillRect(x, y + size - size / 6, size, size / 6);
        g.fillRect(x + size - size / 6, y, size / 6, size);

        g.setColor(Color.BLACK);
        g.drawRect(x, y, size, size);
    }

    private void clearLines() {
        int linesCleared = 0;
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            boolean full = true;
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                linesCleared++;
                for (int i = y; i > 0; i--) {
                    System.arraycopy(board[i - 1], 0, board[i], 0, BOARD_WIDTH);
                }
                Arrays.fill(board[0], 0);
            }
        }
        switch (linesCleared) {
            case 1 -> {
                score += 100;
                SfxPlayer.play("Sfx/1.wav");
            }
            case 2 -> {
                score += 300;
                SfxPlayer.play("Sfx/2.wav");
            }
            case 3 -> {
                score += 500;
                SfxPlayer.play("Sfx/3.wav");
            }
            case 4 -> {
                score += 800;
                SfxPlayer.play("Sfx/4.wav");
            }
        }
    }

    private void rotate(boolean clockwise) {
        Point[] rotated = new Point[4];
        for (int i = 0; i < 4; i++) {
            Point p = currentPiece.shape[i];
            rotated[i] = clockwise ? new Point(-p.y, p.x) : new Point(p.y, -p.x);
        }
        Point[] old = currentPiece.shape;
        currentPiece.shape = rotated;
        if (isCollision()) {
            currentPiece.shape = old;
        } else {
            SfxPlayer.play("Sfx/rotate.wav");
        }
    }
    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) return;

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_P) {
            if (!isPaused) {
                isPaused = true;
                timer.stop();
                int option = JOptionPane.showOptionDialog(
                    this,
                    "暫停中，是否繼續遊戲？",
                    "遊戲暫停",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"繼續", "退出"},
                    "繼續"
                );
                if (option == JOptionPane.YES_OPTION) {
                    isPaused = false;
                    timer.start();
                } else {
                    System.exit(0);
                }
            }
            repaint();
            return;
        }

        if (isPaused) return;

        if (key == KeyEvent.VK_P) {
            if (!isPaused) {
                isPaused = true;
                timer.stop();
                int option = JOptionPane.showOptionDialog(
                    this,
                    "暫停中，是否繼續遊戲？",
                    "遊戲暫停",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"繼續", "退出"},
                    "繼續"
                );
                if (option == JOptionPane.YES_OPTION) {
                    isPaused = false;
                    timer.start();
                } else {
                    System.exit(0);
                }
            }
            repaint();
            return;
        }

        if (isPaused) return;

        if (key == KeyEvent.VK_LEFT) {
        pieceX--;
        if (isCollision()) pieceX++;
        } else if (key == KeyEvent.VK_RIGHT) {
            pieceX++;
            if (isCollision()) pieceX--;
        } else if (key == KeyEvent.VK_DOWN) {
            pieceY++;
            if (isCollision()) {
             pieceY--;
                fixPiece();
                clearLines();
             spawnNewPiece();
            }
        } else if (key == KeyEvent.VK_UP) {
            rotate(true);
        } else if (key == KeyEvent.VK_SLASH) {
            rotate(false);
        } else if (key == KeyEvent.VK_SPACE) {
            while (!isCollision()) {
                pieceY++;
            }
            pieceY--;
            fixPiece();
            clearLines();
            spawnNewPiece();
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
        p.shape = Arrays.stream(shapes[idx]).map(pt -> new Point(pt.x, pt.y)).toArray(Point[]::new);
        p.color = colors[idx];
        return p;
    }
}