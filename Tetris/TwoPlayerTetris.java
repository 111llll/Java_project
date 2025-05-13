import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.KeyboardFocusManager;

public class TwoPlayerTetris extends JFrame {
    private TetrisBoard player1Board;
    private TetrisBoard player2Board;

    public TwoPlayerTetris() {
        setTitle("雙人俄羅斯方塊");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new GridLayout(1, 2));

        // 建立左右玩家面板
        player1Board = new TetrisBoard("玩家一", true);
        player2Board = new TetrisBoard("玩家二", false);

        // 雙向設定彼此為對手
        player1Board.setOpponent(player2Board);
        player2Board.setOpponent(player1Board);

        add(player1Board);
        add(player2Board);

        // 加入全域鍵盤事件監聽器
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    player1Board.dispatchKeyEvent(e);
                    player2Board.dispatchKeyEvent(e);
                }
                return false;
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
