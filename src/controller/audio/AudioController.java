package controller.audio;

import modele.Menu;
import modele.game.Game;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
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

        addChannel(AudioID.DEATH, getClass().getResource("../../resources/audio/death.wav").getFile(), false);
        addChannel(AudioID.EATING, getClass().getResource("../../resources/audio/eating.wav").getFile(), false);
        addChannel(AudioID.EATING_FRUIT, getClass().getResource("../../resources/audio/eating-fruit.wav").getFile(), true);
        addChannel(AudioID.EATING_GHOST, getClass().getResource("../../resources/audio/eating-ghost.wav").getFile(), true);
        addChannel(AudioID.EXTRA_LIFE, getClass().getResource("../../resources/audio/extra-life.wav").getFile(), false);
        addChannel(AudioID.GHOST_SIREN, getClass().getResource("../../resources/audio/ghost-siren.wav").getFile(), false);
        addChannel(AudioID.GHOST_HOME, getClass().getResource("../../resources/audio/ghost-home.wav").getFile(), false);
        addChannel(AudioID.GHOST_FRIGHTENED, getClass().getResource("../../resources/audio/ghost-frightened.wav").getFile(), false);
        addChannel(AudioID.INTRO, getClass().getResource("../../resources/audio/ready.wav").getFile(), false);
        addChannel(AudioID.END, getClass().getResource("../../resources/audio/end.wav").getFile(), false);
        addChannel(AudioID.WARNING, getClass().getResource("../../resources/audio/NO!.wav").getFile(), false);

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
