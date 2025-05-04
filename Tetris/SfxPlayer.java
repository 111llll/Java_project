import javax.sound.sampled.*;
import java.io.File;

public class SfxPlayer {
    public static void play(String path) {
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(path));
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception e) {
            System.err.println("⚠ 無法播放音效：" + path);
            e.printStackTrace();
        }
    }
}
