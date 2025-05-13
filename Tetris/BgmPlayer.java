import javax.sound.sampled.*;
import java.io.File;
import java.util.List;
import java.util.Arrays;

public class BgmPlayer {
    private int currentIndex = 0;
    private Clip clip;
    private String bgmFile;
    private List<String> tracks = Arrays.asList(
            "SoundTrack/Minecraft.wav",
            "SoundTrack/Wet Hands.wav"
    );
    public BgmPlayer(String filePath) {
        this.bgmFile = filePath;
    }
    public BgmPlayer() {
        this.tracks = Arrays.asList(
            "SoundTrack/Minecraft.wav",
            "SoundTrack/Wet Hands.wav"

        );
    }
    public void playLooped() {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(bgmFile));
            clip = AudioSystem.getClip();
            clip.open(stream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void playAllLooped() {
        playTrack(currentIndex);
    }

    private void playTrack(int index) {
    try {
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(tracks.get(index)));
        clip = AudioSystem.getClip();
        clip.open(stream);
        clip.start();  // ✅ 播放一次，不重播、不跳下一首
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
