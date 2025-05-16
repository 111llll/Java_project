import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Random;

public class TetrisBoard extends JPanel implements ActionListener {
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 18;
    private final int CELL_SIZE = 30;
    
    private boolean isFrozen = false;
    private Timer timer;
    private Piece currentPiece;
    private Piece holdPiece = null;
    private boolean holdUsed = false;
    private Piece[] nextQueue = new Piece[3];
    private int pieceX = 4;
    private int pieceY = 0;
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private boolean gameOver = false;
    private boolean isPlayerOne;
    private String playerName;
    private int rotationCount = 0;

    private TetrisBoard opponent;
    private BgmPlayer bgm;

    public TetrisBoard(String name, boolean isPlayerOne) {
        this.playerName = name;
        this.isPlayerOne = isPlayerOne;
        setPreferredSize(new Dimension((BOARD_WIDTH + 4) * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);

        bgm = new BgmPlayer();
        bgm.playAllLooped();

        spawnNewPiece();
        timer = new Timer(500, this);
        timer.start();
    }

    public void setOpponent(TetrisBoard opponent) {
        this.opponent = opponent;
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
            SfxPlayer.play("Sfx/gg.wav");
            triggerGameOver(opponent.playerName);
        }
    }
    private void holdCurrentPiece() {
        if (holdUsed) return;

        Piece temp = holdPiece;
        holdPiece = new Piece(currentPiece);
        if (temp == null) {
            spawnNewPiece();
        } else {
            currentPiece = new Piece(temp);
            pieceX = 4;
            pieceY = 0;
            if (isCollision()) {
                gameOver = true;
                timer.stop();
                return;
            }
        }
        holdUsed = true;
    }

    public void dispatchKeyEvent(KeyEvent e) {
        if (gameOver || isFrozen) return;

        int key = e.getKeyCode();
        if (isPlayerOne) {
            if (key == KeyEvent.VK_A) {
                pieceX--;
                if (isCollision()) pieceX++;
            } else if (key == KeyEvent.VK_D) {
                pieceX++;
                if (isCollision()) pieceX--;
            } else if (key == KeyEvent.VK_S) {
                pieceY++;
                if (isCollision()) {
                    pieceY--;
                    fixPiece();
                    clearLines();
                    spawnNewPiece();
                }
            } else if (key == KeyEvent.VK_C) {
                holdCurrentPiece();

            } else if (key == KeyEvent.VK_W) {
                rotate(true);
            } else if (key == KeyEvent.VK_E) {
                rotate(false);
            } else if (key == KeyEvent.VK_X) {
                while (!isCollision()) pieceY++;
                pieceY--;
                fixPiece();
                clearLines();
                spawnNewPiece();
            }
        } else {
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
            } else if (key == KeyEvent.VK_COMMA) {
                holdCurrentPiece();
            } else if (key == KeyEvent.VK_UP) {
                rotate(true);
            } else if (key == KeyEvent.VK_SLASH) {
                rotate(false);
            } else if (key == KeyEvent.VK_PERIOD) {
                while (!isCollision()) pieceY++;
                pieceY--;
                fixPiece();
                clearLines();
                spawnNewPiece();
            }
        }
        repaint();
    }

    private void rotate(boolean clockwise) {
        if (currentPiece.type == 3) return;

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

        Point[] old = currentPiece.shape;
        currentPiece.shape = rotated;

        if (isCollision()) {
            currentPiece.shape = old;
        } else {
            rotationCount++;
            if ((currentPiece.type == 0 || currentPiece.type == 4 || currentPiece.type == 6) && clockwise && rotationCount % 2 == 0) {
                pieceX--;
            }
        }
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
    }

    public void triggerGameOver(String winner) {
        if (opponent != null) {
            this.timer.stop();
            opponent.timer.stop();
            this.isFrozen = true;
            opponent.isFrozen = true;
            this.timer.stop();
            opponent.timer.stop();
            new Thread(() -> {
            try {
                Thread.sleep(3800); // 延遲 3.8 秒
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            SwingUtilities.invokeLater(() -> {
            int option = JOptionPane.showOptionDialog(
                this.getParent(),
                winner + " 獲勝！是否重新開始？",
                "遊戲結束",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"重新", "回到主選單"},
                "重新"
            );

            if (option == JOptionPane.YES_OPTION) {
                this.resetGame();
                opponent.resetGame();
                this.timer.start();
                opponent.timer.start();
            } else {
                // ✅ 停止雙方 BGM（在回主選單前）
                if (this.bgm != null) this.bgm.stop();
                if (opponent.bgm != null) opponent.bgm.stop();

                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                topFrame.dispose(); // 關閉雙人視窗
                new MainMenu().setVisible(true); // 回到主選單
            }
        });


        }).start();

        }
    }

    public void resetGame() {
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        gameOver = false;
        isFrozen = false;
        spawnNewPiece();
        repaint();
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.white);
        g.drawString("NEXT", BOARD_WIDTH * CELL_SIZE + 10, 300);
        g.drawString("HOLD", BOARD_WIDTH * CELL_SIZE + 10, 80);
        for (int i = 0; i < 3; i++) {
            Piece next = nextQueue[i];
            if (next != null) {
                drawMiniPiece(g, next, BOARD_WIDTH * CELL_SIZE + 10, 320 + i * 60, 15);
            }
        }
        if (holdPiece != null) {
            drawMiniPiece(g, holdPiece, BOARD_WIDTH * CELL_SIZE + 10, 100, 15);
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
        g.drawString(playerName , BOARD_WIDTH * CELL_SIZE + 10, 20);
        if (gameOver) {
            g.setColor(Color.RED);
            g.drawString("GAME OVER", BOARD_WIDTH * CELL_SIZE + 10, 50);
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
}
