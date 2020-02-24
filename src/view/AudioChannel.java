package view;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioChannel {

    int currentFrame;
    Clip clip;
    Status status;
    AudioInputStream audioInputStream;
    String filePath;
    boolean canRestart;

    public AudioChannel(String file, boolean canRestart) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this.filePath = file;
        this.canRestart = canRestart;
        audioInputStream = AudioSystem.getAudioInputStream(new File(file).getAbsoluteFile());
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        status = Status.PAUSED;
    }

    public void pause() {
        if (status == Status.PLAYING) {
            this.currentFrame = (int) this.clip.getMicrosecondPosition();
            clip.stop();
            status = Status.PAUSED;
        }
    }


    public void play() {
        if (canRestart || clip.getMicrosecondPosition() >= clip.getMicrosecondLength() || clip.getMicrosecondPosition() == 0) {
            status = Status.PLAYING;
            clip.stop();
            currentFrame = (int) 0L;
            clip.setMicrosecondPosition(0);
            clip.start();
        }
    }

    public void loop() {
        if (status == Status.PAUSED) {
            status = Status.PLAYING;
            clip.stop();
            currentFrame = (int) 0L;
            clip.setMicrosecondPosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        currentFrame = (int) 0L;
        clip.stop();
        clip.close();
    }
}

enum Status {
    PLAYING,
    PAUSED
}