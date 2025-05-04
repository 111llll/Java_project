import javax.sound.sampled.*;
import java.io.File;
import java.util.List;
import java.util.Arrays;

public class BgmPlayer {
    private int currentIndex = 0;
    private Clip clip;
    private List<String> tracks = Arrays.asList(
            "SoundTrack/Minecraft.wav",
            "SoundTrack/Living Mice.wav",
            "SoundTrack/Key.wav"
    );

    public void playAllLooped() {
        playTrack(currentIndex);
    }

    private void playTrack(int index) {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(tracks.get(index)));
            clip = AudioSystem.getClip();
            clip.open(stream);

            // 當音樂結束，播放下一首
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    currentIndex = (currentIndex + 1) % tracks.size(); // 取下一首（循環）
                    playTrack(currentIndex);
                }
            });

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
