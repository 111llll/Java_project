
// Main class: Tetris1
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.Arrays;
import javax.sound.sampled.*;
import java.io.File;

public class Tetris1 extends JPanel implements ActionListener, KeyListener {
    private final int BOARD_WIDTH = 10, BOARD_HEIGHT = 18, CELL_SIZE = 30;
    private Timer timer;
    private Piece currentPiece;
    private int pieceX = 4, pieceY = 0;
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private int score = 0, level = 1, linesClearedTotal = 0;
    private boolean gameOver = false, isPaused = false;

    private final int[] levelSpeeds = {500, 450, 400, 350, 300, 250, 200, 150, 100, 80, 60};
    private final Color[] levelBackgrounds = {
        Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.BLUE, Color.CYAN,
        Color.GREEN.darker(), Color.ORANGE.darker(), Color.MAGENTA.darker(),
        Color.RED.darker(), Color.YELLOW.darker(), Color.WHITE
    };

    private BgmPlayer bgm;

    public Tetris1() {
        setPreferredSize(new Dimension((BOARD_WIDTH + 5) * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        bgm = new BgmPlayer();
        bgm.changeTrack(level);

        spawnNewPiece();
        timer = new Timer(levelSpeeds[level - 1], this);
        timer.start();
    }

    private void spawnNewPiece() {
        currentPiece = Piece.getRandomPiece();
        pieceX = 4; pieceY = 0;
        if (isCollision()) {
            gameOver = true;
            timer.stop();
            SfxPlayer.play("Sfx/gg.wav");
            new Thread(() -> {
                try { Thread.sleep(3800); } catch (Exception ex) {}
                SwingUtilities.invokeLater(this::showRestartDialog);
            }).start();
        }
    }

    private void showRestartDialog() {
        int option = JOptionPane.showOptionDialog(
                this, "得分:" + score + "\n是否重新遊戲?", "遊戲結束",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new String[]{"重新", "退出"}, "Restart");

        if (option == JOptionPane.YES_OPTION) resetGame();
        else System.exit(0);
    }

    private void resetGame() {
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        score = 0; level = 1; linesClearedTotal = 0;
        gameOver = false; isPaused = false;
        bgm.changeTrack(level);
        timer.setDelay(levelSpeeds[level - 1]);
        spawnNewPiece();
        timer.start();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(levelBackgrounds[Math.min(level - 1, levelBackgrounds.length - 1)]);

        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i <= BOARD_WIDTH; i++) g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE);
        for (int i = 0; i <= BOARD_HEIGHT; i++) g.drawLine(0, i * CELL_SIZE, BOARD_WIDTH * CELL_SIZE, i * CELL_SIZE);

        for (int y = 0; y < BOARD_HEIGHT; y++)
            for (int x = 0; x < BOARD_WIDTH; x++)
                if (board[y][x] != 0)
                    draw3DBlock(g, x * CELL_SIZE, y * CELL_SIZE, new Color(board[y][x]));

        if (!gameOver) {
            g.setColor(currentPiece.color);
            for (Point p : currentPiece.shape)
                draw3DBlock(g, (pieceX + p.x) * CELL_SIZE, (pieceY + p.y) * CELL_SIZE, currentPiece.color);
        }

        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, BOARD_WIDTH * CELL_SIZE + 20, 30);
        g.drawString("Level: " + level, BOARD_WIDTH * CELL_SIZE + 20, 50);
        if (isPaused && !gameOver) {
            g.setColor(Color.YELLOW);
            g.drawString("PAUSED", BOARD_WIDTH * CELL_SIZE + 20, 80);
        }
        if (gameOver) {
            g.setColor(Color.RED);
            g.drawString("GAME OVER", BOARD_WIDTH * CELL_SIZE + 20, 80);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isPaused) {
            pieceY++;
            if (isCollision()) {
                pieceY--;
                fixPiece();
                clearLines();
                spawnNewPiece();
            }
            repaint();
        }
    }

    private boolean isCollision() {
        for (Point p : currentPiece.shape) {
            int x = pieceX + p.x, y = pieceY + p.y;
            if (x < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT) return true;
            if (y >= 0 && board[y][x] != 0) return true;
        }
        return false;
    }

    private void fixPiece() {
        for (Point p : currentPiece.shape) {
            int x = pieceX + p.x, y = pieceY + p.y;
            if (y >= 0 && x >= 0 && x < BOARD_WIDTH && y < BOARD_HEIGHT)
                board[y][x] = currentPiece.color.getRGB();
        }
    }

    private void draw3DBlock(Graphics g, int x, int y, Color baseColor) {
        int s = CELL_SIZE;
        g.setColor(baseColor); g.fillRect(x, y, s, s);
        g.setColor(baseColor.brighter()); g.fillRect(x, y, s, s / 6); g.fillRect(x, y, s / 6, s);
        g.setColor(baseColor.darker()); g.fillRect(x, y + s - s / 6, s, s / 6); g.fillRect(x + s - s / 6, y, s / 6, s);
        g.setColor(Color.BLACK); g.drawRect(x, y, s, s);
    }

    private void clearLines() {
        int cleared = 0;
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            boolean full = true;
            for (int x = 0; x < BOARD_WIDTH; x++)
                if (board[y][x] == 0) { full = false; break; }
            if (full) {
                cleared++;
                for (int i = y; i > 0; i--)
                    System.arraycopy(board[i - 1], 0, board[i], 0, BOARD_WIDTH);
                Arrays.fill(board[0], 0);
            }
        }

        switch (cleared) {
            case 1 -> score += 100;
            case 2 -> score += 300;
            case 3 -> score += 500;
            case 4 -> score += 800;
        }

        linesClearedTotal += cleared;
        int newLevel = 1 + linesClearedTotal / 10;
        if (newLevel > level && newLevel <= levelSpeeds.length) {
            level = newLevel;
            timer.setDelay(levelSpeeds[level - 1]);
            bgm.changeTrack(level);
        }
    }

   private void rotate(boolean clockwise) {
        Point[] rotated = new Point[4];
    
        // Step 1: 找出中心點（使用平均座標法）
        double centerX = 0, centerY = 0;
        for (Point p : currentPiece.shape) {
            centerX += p.x;
            centerY += p.y;
        }
        centerX /= 4.0;
        centerY /= 4.0;
    
        for (int i = 0; i < 4; i++) {
            Point p = currentPiece.shape[i];
    
            // Step 2~4: 平移到原點 → 旋轉 → 移回中心
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
            SfxPlayer.play("Sfx/rotate.wav");
        }
    }

    @Override public void keyPressed(KeyEvent e) {
        if (gameOver) return;
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_P) {
            isPaused = !isPaused;
            if (isPaused) { timer.stop(); bgm.stop(); }
            else { timer.start(); bgm.changeTrack(level); }
            repaint(); return;
        }

        if (isPaused) return;

        if (key == KeyEvent.VK_LEFT) {
            pieceX--; if (isCollision()) pieceX++;
        } else if (key == KeyEvent.VK_RIGHT) {
            pieceX++; if (isCollision()) pieceX--;
        } else if (key == KeyEvent.VK_DOWN) {
            pieceY++;
            if (isCollision()) {
                pieceY--; fixPiece(); clearLines(); spawnNewPiece();
            }
        } else if (key == KeyEvent.VK_UP) rotate(true);
        else if (key == KeyEvent.VK_SLASH) rotate(false);
        else if (key == KeyEvent.VK_SPACE) {
            while (!isCollision()) pieceY++;
            pieceY--; fixPiece(); clearLines(); spawnNewPiece();
        }
        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame f = new JFrame("Tetris");
        Tetris1 t = new Tetris1();
        f.add(t); f.pack(); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null); f.setVisible(true);
    }
}

// Piece 類別
class Piece {
    public Point[] shape;
    public Color color;
    private static final Point[][] shapes = {
        {new Point(0,0), new Point(1,0), new Point(2,0), new Point(3,0)},
        {new Point(0,0), new Point(0,1), new Point(1,1), new Point(2,1)},
        {new Point(2,0), new Point(0,1), new Point(1,1), new Point(2,1)},
        {new Point(0,0), new Point(1,0), new Point(0,1), new Point(1,1)},
        {new Point(1,0), new Point(2,0), new Point(0,1), new Point(1,1)},
        {new Point(1,0), new Point(0,1), new Point(1,1), new Point(2,1)},
        {new Point(0,0), new Point(1,0), new Point(1,1), new Point(2,1)}
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

// BgmPlayer 類別
class BgmPlayer {
    private Clip clip;
    private final String[] tracks = {
        "SoundTrack/Minecraft.wav",
        "SoundTrack/Living Mice.wav",
        "SoundTrack/Key.wav"
    };

    public void changeTrack(int level) {
        stop();
        int index = (level - 1) % tracks.length;
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(tracks[index]));
            clip = AudioSystem.getClip();
            clip.open(stream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
}

// SfxPlayer 類別
class SfxPlayer {
    public static void play(String path) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(path));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.out.println("Failed to play sound: " + path);
        }
    }
}
