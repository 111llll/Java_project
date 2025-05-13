import javax.sound.sampled.*;
import java.io.File;
import java.util.List;
import java.util.Arrays;

public class SpeedBgmPlayer {
    private Clip clip;
    private String bgmFile;
    private List<String> tracks = Arrays.asList(
            "LevelBgm/Level0.wav",
            "LevelBgm/Level1.wav",
            "LevelBgm/Level2.wav",
            "LevelBgm/Level3.wav",
            "LevelBgm/Level4.wav",
            "LevelBgm/Level5.wav"
    );
    public SpeedBgmPlayer(String filePath) {
        this.bgmFile = filePath;
    }
    public SpeedBgmPlayer() {
        this.tracks = Arrays.asList(
            "LevelBgm/Level0.wav",
            "LevelBgm/Level1.wav",
            "LevelBgm/Level2.wav",
            "LevelBgm/Level3.wav",
            "LevelBgm/Level4.wav",
            "LevelBgm/Level5.wav"
        );
    }
    public void play(int level) {
    stop(); // 停止前一首
    try {
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(tracks.get(level)));
        clip = AudioSystem.getClip();
        clip.open(stream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
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
