import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JFrame implements ActionListener {
    private JButton singlePlayerButton;
    private JButton twoPlayerButton;
    private JButton instructionsButton;
    private JButton exitButton;
    private BgmPlayer bgm;

    public MainMenu() {
        setTitle("俄羅斯方塊選單");
        setSize(300, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1, 10, 10));

        bgm = new BgmPlayer("SoundTrack/Living Mice.wav");
        bgm.playLooped(); // 播放一首並循環

        singlePlayerButton = new JButton("單人遊戲");
        twoPlayerButton = new JButton("雙人遊戲");
        instructionsButton = new JButton("操作說明");
        exitButton = new JButton("離開");

        singlePlayerButton.addActionListener(this);
        twoPlayerButton.addActionListener(this);
        instructionsButton.addActionListener(this);
        exitButton.addActionListener(this);

        add(singlePlayerButton);
        add(twoPlayerButton);
        add(instructionsButton);
        add(exitButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == singlePlayerButton) {
            launchSinglePlayer();
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

    private void launchTwoPlayer() {
        bgm.stop();
        dispose();
        new TwoPlayerTetris();
    }

    private void showInstructions() {
        String message = """
            操作說明：
            
            玩家一：
            W/E：順時針/逆時針旋轉
            A/D：左/右移動
            S：加速下降
            X：硬掉落

            玩家二：
            ↑/／：順時針/逆時針旋轉
            ←/→：左/右移動
            ↓：加速下降
            Space：硬掉落
        """;
        JOptionPane.showMessageDialog(this, message, "操作方法", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainMenu().setVisible(true);
        });
    }
}
