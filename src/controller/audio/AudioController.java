package controller.audio;

import modele.Menu;
import modele.game.Game;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class AudioController implements Observer {

    private Map<AudioID, AudioChannel> audioChannels;
    private boolean canPlayIntro = true;
    private boolean canPlayEnd = true;

    public AudioController() {
        audioChannels = new HashMap<>();

        addChannel(AudioID.DEATH, "resources/audio/death.wav", false);
        addChannel(AudioID.EATING, "resources/audio/eating.wav", false);
        addChannel(AudioID.EATING_FRUIT, "resources/audio/eating-fruit.wav", true);
        addChannel(AudioID.EATING_GHOST, "resources/audio/eating-ghost.wav", true);
        addChannel(AudioID.EXTRA_LIFE, "resources/audio/extra-life.wav", false);
        addChannel(AudioID.GHOST_SIREN,"resources/audio/ghost-siren.wav", false);
        addChannel(AudioID.GHOST_HOME,"resources/audio/ghost-home.wav", false);
        addChannel(AudioID.GHOST_FRIGHTENED, "resources/audio/ghost-frightened.wav", false);
        addChannel(AudioID.INTRO, "resources/audio/ready.wav", false);
        addChannel(AudioID.END, "resources/audio/end.wav", false);
        addChannel(AudioID.WARNING,"resources/audio/NO!.wav", false);

        Game.getInstance().addObserver(this);
        Menu.getInstance().addObserver(this);
    }

    /**
     * Set whether or not the controller can play the Intro Channel
     * @param can can the controller play the Intro Channel
     */
    public synchronized void canPlayIntro(boolean can) {
        this.canPlayIntro = can;
    }

    /**
     * Add a new AudioChannel to the controller
     * @param id the AudioID of the channel
     * @param file the audio file to be loaded
     * @param canRestart can the channel be restarted mid-play
     */
    public synchronized void addChannel(AudioID id, String file, boolean canRestart) {
        try {
            audioChannels.put(id, new AudioChannel(file, canRestart));
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Play a warning sound
     */
    public synchronized void warning() {
        playChannel(AudioID.WARNING);
    }

    /**
     * Play a specified AudioChannel
     * @param id the AudioChannel's AudioID
     */
    private void playChannel(AudioID id) {
        audioChannels.get(id).play();
    }

    /**
     * Loop a specified AudioChannel
     * @param id the AudioChannel's AudioID
     */
    private void loopChannel(AudioID id) {
        audioChannels.get(id).loop();
    }

    /**
     * Pause a specified AudioChannel
     * @param id the AudioChannel's AudioID
     */
    private void pauseChannel(AudioID id) {
        audioChannels.get(id).pause();
    }

    /**
     * Update the controller and play the appropriated AudioChannel
     */
    @Override
    public void update(Observable o, Object arg) {
        Game game = Game.getInstance();
        switch (game.getGameState()) {
            case GAME_SCREEN:
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
                    if (!game.canStart() &&  game.getLives() >= 0 && canPlayIntro) {
                        playChannel(AudioID.INTRO);
                        canPlayIntro = false;
                    }
                }
                if (game.isGameFinished() && game.isPlayerDead() && canPlayIntro && canPlayEnd) {
                    loopChannel(AudioID.END);
                    pauseChannel(AudioID.GHOST_HOME);
                    pauseChannel(AudioID.GHOST_SIREN);
                    pauseChannel(AudioID.GHOST_FRIGHTENED);
                    canPlayEnd = false;
                }
                if (!game.isPlayerDead())
                    canPlayIntro(false);
                break;
            case MENU_SCREEN:
                pauseChannel(AudioID.END);
                break;
            case LEVEL_EDITOR:
                break;
        }
    }
}
