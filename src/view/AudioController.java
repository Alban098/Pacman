package view;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import modele.PacMan;
import modele.StaticEntity;
import modele.entities.EntityPlayer;
import modele.entities.MoveableEntity;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class AudioController {

    private PacMan game;
    private Map<AudioID, AudioChannel> audioChannels;
    private boolean canPlayIntro = true;

    public AudioController(PacMan instance) {
        audioChannels = new HashMap<>();

        addChannel(AudioID.DEATH, getClass().getResource("../resources/audio/death.wav").getFile(), false);
        addChannel(AudioID.EATING, getClass().getResource("../resources/audio/eating.wav").getFile(), false);
        addChannel(AudioID.EATING_FRUIT, getClass().getResource("../resources/audio/eating-fruit.wav").getFile(), true);
        addChannel(AudioID.EATING_GHOST, getClass().getResource("../resources/audio/eating-ghost.wav").getFile(), true);
        addChannel(AudioID.EXTRA_LIFE, getClass().getResource("../resources/audio/extra-life.wav").getFile(), false);
        addChannel(AudioID.GHOST_SIREN, getClass().getResource("../resources/audio/ghost-siren.wav").getFile(), false);
        addChannel(AudioID.GHOST_HOME, getClass().getResource("../resources/audio/ghost-home.wav").getFile(), false);
        addChannel(AudioID.GHOST_FRIGHTENED, getClass().getResource("../resources/audio/ghost-frightened.wav").getFile(), false);
        addChannel(AudioID.INTRO, getClass().getResource("../resources/audio/ready.wav").getFile(), false);

        this.game = instance;
        Observer o = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                if (game.hasPlayerDied())
                    playChannel(AudioID.DEATH);
                if (game.hasEatenFruit())
                    playChannel(AudioID.EATING_FRUIT);
                if (game.hasEatenGhost())
                    playChannel(AudioID.EATING_GHOST);
                if (game.hasExtraLife())
                    playChannel(AudioID.EXTRA_LIFE);
                if (game.isGameStarted()) {
                    if (game.areGhostEaten())
                        loopChannel(AudioID.GHOST_HOME);
                    else
                        pauseChannel(AudioID.GHOST_HOME);
                    if (game.areGhostFrightened()) {
                        pauseChannel(AudioID.GHOST_SIREN);
                        loopChannel(AudioID.GHOST_FRIGHTENED);
                    } else {
                        pauseChannel(AudioID.GHOST_FRIGHTENED);
                        loopChannel(AudioID.GHOST_SIREN);
                    }
                    if (game.hasEatenGum())
                        playChannel(AudioID.EATING);
                } else {
                    pauseChannel(AudioID.GHOST_HOME);
                    pauseChannel(AudioID.GHOST_SIREN);
                    pauseChannel(AudioID.GHOST_FRIGHTENED);
                    if (!game.canStart() && game.getLives() >= 0 && canPlayIntro) {
                        playChannel(AudioID.INTRO);
                        canPlayIntro = false;
                    }
                }
            }
        };
        game.addObserver(o);
    }

    public void canPlayIntro() {
        this.canPlayIntro = true;
    }

    public void addChannel(AudioID id, String file, boolean canRestart) {
        try {
            audioChannels.put(id, new AudioChannel(file, canRestart));
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
            System.exit(-5);
        }
    }

    public void playChannel(AudioID id) {
        audioChannels.get(id).play();
    }

    public void loopChannel(AudioID id) {
        audioChannels.get(id).loop();
    }

    public void pauseChannel(AudioID id) {
        audioChannels.get(id).pause();
    }
}
