import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JFrame implements ActionListener {
    private JButton singlePlayerButton;
    private JButton twoPlayerButton;
    private JButton instructionsButton;
    private JButton exitButton;
    private JButton fastSinglePlayerButton;
    private BgmPlayer bgm;

    public MainMenu() {
        setTitle("俄羅斯方塊選單");
        setSize(300, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 1, 10, 10));

        bgm = new BgmPlayer("SoundTrack/Wet Hands.wav");
        bgm.playLooped(); // 播放一首並循環

        singlePlayerButton = new JButton("單人遊戲");
        fastSinglePlayerButton = new JButton("單人加速");
        twoPlayerButton = new JButton("雙人遊戲");
        instructionsButton = new JButton("操作說明");
        exitButton = new JButton("離開");

        singlePlayerButton.addActionListener(this);
        fastSinglePlayerButton.addActionListener(this);
        twoPlayerButton.addActionListener(this);
        instructionsButton.addActionListener(this);
        exitButton.addActionListener(this);
        

        add(singlePlayerButton);
        add(fastSinglePlayerButton);
        add(twoPlayerButton);
        add(instructionsButton);
        add(exitButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == singlePlayerButton) {
            launchSinglePlayer();
        } else if (source == fastSinglePlayerButton) {
            launchFastSinglePlayer();
        } else if (source == twoPlayerButton) {
            launchTwoPlayer();
        } else if (source == instructionsButton) {
            showInstructions();
        } else if (source == exitButton) {
            System.exit(0);
        }
    }

    private void launchSinglePlayer() {
        bgm.stop();
        dispose();  // 關閉選單視窗
        JFrame gameFrame = new JFrame("Tetris 單人模式");
        Tetris tetris = new Tetris();
        gameFrame.add(tetris);
        gameFrame.pack();
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);
    }
    private void launchFastSinglePlayer() {
        bgm.stop();
        dispose();  // 關閉選單
        JFrame gameFrame = new JFrame("Tetris 單人加速模式");
        TetrisSpeed speedGame = new TetrisSpeed(); // 新增的 TetrisSpeed 類別
        gameFrame.add(speedGame);
        gameFrame.pack();
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);
    }

    private void launchTwoPlayer() {
        bgm.stop();
        dispose();
        new TwoPlayerTetris();
    }

    private void showInstructions() {
    showSinglePlayerInstructions();
}

private void showSinglePlayerInstructions() {
    String single = """
        單人模式操作說明：

        ↑：順時針旋轉
        ／：逆時針旋轉
        ←／→：左／右移動
        ↓：加速下降
        C：儲存當前方塊
        空白鍵：硬掉落
        P：暫停
    """;

    int option = JOptionPane.showOptionDialog(
        this,
        single,
        "單人操作說明",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        null,
        new String[]{"下一頁（雙人）", "關閉"},
        "下一頁（雙人）"
    );

    if (option == JOptionPane.YES_OPTION) {
        showTwoPlayerInstructions();
    }
}

private void showTwoPlayerInstructions() {
    String dual = """
        雙人模式操作說明：

        玩家一（左側）：
        W／E：順時針／逆時針旋轉
        A／D：左／右移動
        S：加速下降
        X：硬掉落
        C：保存方塊

        玩家二（右側）：
        ↑：順時針旋轉
        ／：逆時針旋轉
        ←／→：左／右移動
        ↓：加速下降
        .：硬掉落
        ,：保存方塊

        任一玩家遊戲結束即結束對局
    """;

    int option = JOptionPane.showOptionDialog(
        this,
        dual,
        "雙人操作說明",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        null,
        new String[]{"上一頁（單人）", "關閉"},
        "上一頁（單人）"
    );

    if (option == JOptionPane.YES_OPTION) {
        showSinglePlayerInstructions();
    }
}



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainMenu().setVisible(true);
        });
    }
}
