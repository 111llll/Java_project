import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.Arrays;

public class TetrisSpeed extends JPanel implements ActionListener, KeyListener {
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 18;
    private final int CELL_SIZE = 30;

    private Timer timer;
    private int speedLevel = 0; // 0~5
    private final int[] speeds = {500, 400, 300, 200, 100, 50};
    private final Color[] bgColors = {
        new Color(0, 0, 0),
        new Color(30, 30, 30),
        new Color(50, 50, 50),
        new Color(70, 70, 70),
        new Color(100, 100, 100),
        new Color(130, 130, 130)
    };

    private Piece currentPiece;
    private Piece holdPiece = null;
    private boolean holdUsed = false;
    private Piece[] nextQueue = new Piece[3];
    private int pieceX = 4;
    private int pieceY = 0;
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private int score = 0;
    private boolean gameOver = false;
    private SpeedBgmPlayer speedBgm = new SpeedBgmPlayer();
    private Random random = new Random();
    private boolean isPaused = false;

    public TetrisSpeed() {
        setPreferredSize(new Dimension((BOARD_WIDTH + 5) * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE));
        setBackground(bgColors[0]);
        setFocusable(true);
        addKeyListener(this);

        speedBgm.play(speedLevel);

        spawnNewPiece();
        timer = new Timer(speeds[speedLevel], this);
        timer.start();
    }

    private void updateSpeed() {
        int level = Math.min(score / 1000, speeds.length - 1);
        if (level != speedLevel) {
            speedLevel = level;
            timer.setDelay(speeds[speedLevel]);
            setBackground(bgColors[speedLevel]);

            speedBgm.play(speedLevel);
        }
    }

    private void spawnNewPiece() {
    if (nextQueue[0] == null) {
        for (int i = 0; i < 3; i++) {
            nextQueue[i] = Piece.getRandomPiece();
        }
    }

    currentPiece = nextQueue[0];
    nextQueue[0] = nextQueue[1];
    nextQueue[1] = nextQueue[2];
    nextQueue[2] = Piece.getRandomPiece();

    pieceX = 4;
    pieceY = 0;
    holdUsed = false;

    if (isCollision()) {
        gameOver = true;
        timer.stop();
        SfxPlayer.play("Sfx/gg.wav");
        new Thread(() -> {
            try {
                speedBgm.stop();
                Thread.sleep(3800);
            } catch (InterruptedException ex) {}
            SwingUtilities.invokeLater(this::showRestartDialog);
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
            new String[]{"重新", "回到主選單"},
            "重新"
        );

        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            speedBgm.stop();
            topFrame.dispose();
            new MainMenu().setVisible(true);
        }
    }
    private void holdCurrentPiece() {
    if (holdUsed) return;

    Piece temp = holdPiece;
    holdPiece = new Piece(currentPiece); // clone
    if (temp == null) {
        spawnNewPiece();
    } else {
        currentPiece = new Piece(temp);
        pieceX = 4;
        pieceY = 0;
        if (isCollision()) {
            gameOver = true;
            timer.stop();
        }
    }
    holdUsed = true;
}

    private void resetGame() {
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        score = 0;
        speedLevel = 0;
        setBackground(bgColors[speedLevel]);
        gameOver = false;
        spawnNewPiece();
        timer.setDelay(speeds[speedLevel]);
        timer.start();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Hold 顯示
        g.setColor(Color.WHITE);
        g.drawString("HOLD", BOARD_WIDTH * CELL_SIZE + 20, 120);
        g.drawString("NEXT", BOARD_WIDTH * CELL_SIZE + 20, 220);
        if (holdPiece != null) {
            drawMiniPiece(g, holdPiece, BOARD_WIDTH * CELL_SIZE + 20, 140, 15);
        }

        // Next 顯示
        for (int i = 0; i < 3; i++) {
            Piece next = nextQueue[i];
            if (next != null) {
                int previewX = BOARD_WIDTH * CELL_SIZE + 20;
                int previewY = 240 + i * 60;
                drawMiniPiece(g, next, previewX, previewY, 15);
            }
        }

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
        g.drawString("Level: " + (speedLevel+1), BOARD_WIDTH * CELL_SIZE + 20, 50);
        if (gameOver) {
            g.setColor(Color.RED);
            g.drawString("GAME OVER", BOARD_WIDTH * CELL_SIZE + 20, 60);
        }
        if (isPaused && !gameOver) {
            g.setColor(Color.YELLOW);
            g.drawString("PAUSED", BOARD_WIDTH * CELL_SIZE + 20, 80);
        }
    }
    private void drawMiniPiece(Graphics g, Piece piece, int startX, int startY, int size) {
        for (Point p : piece.shape) {
            int x = startX + p.x * size;
            int y = startY + p.y * size;
            drawMiniBlock(g, x, y, size, piece.color);
        }
    }

    private void drawMiniBlock(Graphics g, int x, int y, int size, Color baseColor) {
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

    @Override
    public void actionPerformed(ActionEvent e) {
        pieceY++;
        if (isCollision()) {
            pieceY--;
            fixPiece();
            clearLines();
            updateSpeed(); // 更新速度
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
        if (currentPiece.type == 3) return; // O 不旋轉

        Point[] rotated = new Point[4];
        double centerX = 0, centerY = 0;
        for (Point p : currentPiece.shape) {
            centerX += p.x;
            centerY += p.y;
        }
        centerX /= 4.0;
        centerY /= 4.0;

        for (int i = 0; i < 4; i++) {
            Point p = currentPiece.shape[i];
            int dx = p.x - (int)Math.round(centerX);
            int dy = p.y - (int)Math.round(centerY);
            int newX = clockwise ? -dy : dy;
            int newY = clockwise ? dx : -dx;
            rotated[i] = new Point(newX + (int)Math.round(centerX), newY + (int)Math.round(centerY));
        }

        int newPieceX = pieceX;
        if (clockwise && (currentPiece.type == 0 || currentPiece.type == 4 || currentPiece.type == 6)) {
            currentPiece.rotationCount++;
            if (currentPiece.rotationCount % 2 == 0) {
                newPieceX = pieceX - 1;
            }
        }

        boolean collision = false;
        for (Point p : rotated) {
            int x = newPieceX + p.x;
            int y = pieceY + p.y;
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT || board[y][x] != 0) {
                collision = true;
                break;
            }
        }

        if (!collision) {
            currentPiece.shape = rotated;
            pieceX = newPieceX;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) return;
        if (isPaused) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> {
                pieceX--;
                if (isCollision()) pieceX++;
            }
            case KeyEvent.VK_RIGHT -> {
                pieceX++;
                if (isCollision()) pieceX--;
            }
            case KeyEvent.VK_C -> holdCurrentPiece();
            case KeyEvent.VK_DOWN -> {
                pieceY++;
                if (isCollision()) {
                    pieceY--;
                    fixPiece();
                    clearLines();
                    updateSpeed();
                    spawnNewPiece();
                }
            }
            case KeyEvent.VK_UP -> rotate(true);
            case KeyEvent.VK_SLASH -> rotate(false);
            case KeyEvent.VK_SPACE -> {
                while (!isCollision()) pieceY++;
                pieceY--;
                fixPiece();
                clearLines();
                updateSpeed();
                spawnNewPiece();
            }
            case KeyEvent.VK_P -> {
                isPaused = !isPaused;
                if (isPaused) timer.stop();
                else timer.start();
                repaint();
            }
        }

        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
