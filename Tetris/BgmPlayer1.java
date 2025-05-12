import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class BgmPlayer1 {
    private Clip clip;

    public void changeTrack(int level) {
        stop();

        try {
            String relativePath = "SoundTrack/Bgm" + level + ".wav";
            File file = new File(relativePath).getCanonicalFile();

            System.out.println("[BGM DEBUG] 嘗試讀取音樂路徑: " + file.getAbsolutePath());

            if (!file.exists() || !file.isFile()) {
                System.err.println("[BGM 錯誤] 找不到檔案：" + file.getAbsolutePath());
                return;
            }

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(stream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            System.out.println("[BGM 播放中] Level " + level);

        } catch (UnsupportedAudioFileException e) {
            System.err.println("[BGM 錯誤] 音訊格式不支援！");
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("[BGM 錯誤] 音訊設備不可用！");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[BGM 錯誤] 無法讀取音檔！");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[BGM 錯誤] 未知錯誤！");
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
