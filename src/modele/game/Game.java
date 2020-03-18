package modele.game;

import modele.Loader;
import modele.Utils;
import modele.game.entities.EntityGhost;
import modele.game.entities.EntityPlayer;
import modele.game.entities.MoveableEntity;
import modele.game.enums.GameState;
import modele.game.enums.GhostName;
import modele.game.enums.GhostState;
import modele.game.enums.Movement;
import modele.game.entities.StaticEntity;

import java.util.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game extends Observable implements Runnable {

    public static final int GHOST_UPDATE_INTERVAL = 174;
    public static final int FRIGHTENED_GHOST_UPDATE_INTERVAL = 210;
    public static final int EATEN_GHOST_UPDATE_INTERVAL = 75;
    public static final int PLAYER_UPDATE_INTERVAL = 175;
    public static final int FRAME_DURATION = 15;
    public static final int UPDATE_PER_FRAME = 3;
    public static final int RESTART_DELAY = 5600;
    public static final int FRIGHTENED_DURATION = 10000;
    public static final int EXTRA_LIFE_THRESHOLD = 1500;
    public static final int START_LIVES = 3;

    private static Game instance;

    private final Grid grid;

    private ExecutorService worker;

    private boolean isGameFinished = false;
    private boolean isGameStarted = false;
    protected int totalScore = 0;
    private int scoreToSave = 0;
    private int level = 1;
    protected int levelScore = 0;
    protected int lastLevelScore = 0;
    protected int dynamicScore = 0;
    protected int nbPlayer;

    private long whenToRestart;
    private boolean closeGameRequested = false;
    private long nbUpdates = 0;

    private CollisionManager collisionManager;

    private GameState gameState;
    private List<Score> highscores;

    /**
     * Get the current instance of the Editor
     * @return the current Instance of Editor
     */
    public static Game getInstance() {
        if (instance == null)
            instance = new Game();
        return instance;
    }

    /**
     * Add a new action to be executed by a separated Thread
     * @param runnable the action to perform as soon as possible
     */
    public synchronized void runLater(Runnable runnable) {
        worker.submit(runnable);
    }

    private Game() {
        grid = new Grid();
        collisionManager = new CollisionManager(grid, this);
        whenToRestart = System.currentTimeMillis() + RESTART_DELAY - 1000;
        gameState = GameState.MENU_SCREEN;
        nbPlayer = 2;
        highscores = Loader.getInstance().loadHighscores();
        worker = Executors.newSingleThreadExecutor();
    }

    @Override
    public void run() {
        long elaspedTime;
        while (!closeGameRequested) {
            if (gameState == GameState.GAME_SCREEN)
                elaspedTime = gameLogic();
            else
                elaspedTime = 0;
            try {
                nbUpdates++;
                Thread.sleep(Math.max(1, (FRAME_DURATION - elaspedTime) / UPDATE_PER_FRAME));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Compute a Tick of the game (different from the drawn frames)
     * @return the duration of the Tick
     */
    private long gameLogic() {
        long startTime = System.currentTimeMillis();
        if (isGameStarted && !isGameFinished) {
            collisionManager.testCollision();
            boolean isGameWinned = allGumEaten();

            if (getPlayer().isDead() && getPlayer().getLives() < 0 || isGameWinned) {
                isGameFinished = true;
                isGameStarted = false;
            }
            if (getPlayer().isDead()) {
                isGameStarted = false;
                whenToRestart = System.currentTimeMillis() + RESTART_DELAY;
                grid.stopGame();
                resetGhosts();
            }
            if (isGameFinished) {
                grid.stopGame();
                resetGhosts();
            }
            if (isGameWinned) {
                grid.restart(++level);
                collisionManager.resetTotalGum();
                whenToRestart = System.currentTimeMillis() + RESTART_DELAY;
                isGameStarted = false;
                isGameFinished = false;
            }
        }
        if (nbUpdates % UPDATE_PER_FRAME == 0) {
            setChanged();
            notifyObservers();
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Initialize every Entity and start the game
     * @return has the game started successfully
     */
    public synchronized boolean startGame() {
        if (System.currentTimeMillis() >= whenToRestart) {
            isGameStarted = true;
            getPlayer().setDead(false);
            if (nbPlayer == 1) {
                for (MoveableEntity e : grid.getEntities())
                    if (e instanceof EntityGhost)
                        ((EntityGhost) e).setPlayerControlled(false);
            } else {
                for (MoveableEntity e : grid.getEntities())
                    if (e instanceof EntityGhost && ((EntityGhost) e).getName() == GhostName.BLINKY)
                        ((EntityGhost) e).setPlayerControlled(true);
            }
            isGameFinished = false;
            grid.startEntities();
            return true;
        }
        return false;
    }

    /**
     * Return the current GameState
     * @return the current GameState
     */
    public synchronized GameState getGameState() {
        return gameState;
    }

    /**
     * Set the current GameState and update the relevant variables
     * @param gameState the new GameState
     */
    public void setGameState(GameState gameState) {
        if (gameState == GameState.MENU_SCREEN) {
            for (MoveableEntity e : grid.getEntities())
                grid.resetEntity(e);
            scoreToSave = totalScore;
            totalScore = 0;
            level = 1;
            levelScore = 0;
            lastLevelScore = 0;
            dynamicScore = 0;
        } else if (gameState == GameState.GAME_SCREEN) {
            whenToRestart = System.currentTimeMillis() + RESTART_DELAY - 1000;
            getPlayer().setLives(START_LIVES);
            getPlayer().setDead(false);
            grid.restart(level);
        }
        this.gameState = gameState;
    }

    /**
     * Return the list of all highscores
     * @return a list of all highscores
     */
    public synchronized List<Score> getHighscores() {
        return highscores;
    }

    /**
     * Get the score to save to the Highscore database
     * @return the current score
     */
    public synchronized int getScoreToSave() {
        return scoreToSave;
    }

    /**
     * Set the number of player of the game
     * @param nbPlayer the requested number of player
     */
    public synchronized void setNbPlayer(int nbPlayer) {
        this.nbPlayer = Utils.wrap(nbPlayer, 1, 2);
    }

    /**
     * Return a set of all MoveableEntity
     * @return a set of all MoveableEntity
     */
    public Set<MoveableEntity> getEntities() {
        return grid.getEntities();
    }

    /**
     * Set the player's next action
     * @param action the requested action
     * @param id the id of the player (0 = Player, 1 = Player controlled Ghost)
     */
    public void setNextPlayerAction(Movement action, int id) {
        if (id == 0)
            getPlayer().setAction(action);
        else
            for (MoveableEntity e : grid.getEntities())
                if (e instanceof EntityGhost && ((EntityGhost) e).isPlayerControlled())
                    ((EntityGhost) e).setAction(action);
    }

    /**
     * Get the advancement between 2 updates of an entity
     * Used to calculate inter-update animations
     * @param entity the entity to get the advancement from
     * @return the advancement between 2 update of the entity normalized between 0 and 1
     */
    public float getAnimationPercent(MoveableEntity entity) {
        if (entity instanceof EntityPlayer) {
            if (entity.getCurrentDirection() != Movement.NONE)
                return (System.currentTimeMillis() - entity.getLastActionTime()) / (float)PLAYER_UPDATE_INTERVAL;
        } else if (entity instanceof EntityGhost && entity.getCurrentDirection() != Movement.NONE) {
            switch (((EntityGhost) entity).getState()) {
                case EATEN:
                    return (System.currentTimeMillis() - entity.getLastActionTime()) / (float) EATEN_GHOST_UPDATE_INTERVAL;
                case FRIGHTENED:
                    return (System.currentTimeMillis() - entity.getLastActionTime()) / (float) FRIGHTENED_GHOST_UPDATE_INTERVAL;
                default:
                    return (System.currentTimeMillis() - entity.getLastActionTime()) / (float) GHOST_UPDATE_INTERVAL;
            }
        }
        return 0;
    }

    /**
     * Return the current direction of a MoveableEntity
     * @param entity the entity to get the direction from
     * @return the entity's current direction
     */
    public Movement getDirection(MoveableEntity entity) {
        return entity.getCurrentDirection();
    }

    /**
     * Return whether or not the game is finished
     * @return is the game started
     */
    public synchronized boolean isGameFinished() {
        return isGameFinished;
    }

    /**
     * Return whether or not the game has started
     * @return has the game started
     */
    public synchronized boolean isGameStarted() {
        return isGameStarted;
    }

    /**
     * Return whether or not the game can start
     * @return can the game start
     */
    public synchronized boolean canStart() {
        return whenToRestart <= System.currentTimeMillis();
    }

    /**
     * Return whether or not the player is dead
     * @return is the player dead
     */
    public synchronized boolean isPlayerDead() {
        return getPlayer().isDead();
    }

    /**
     * Return whether or not the player has eaten every GUM and SUPER_GUM
     * @return has the player eaten all GUM and SUPER_GUM
     */
    private synchronized boolean allGumEaten() {
        return grid.getStaticEntityCount(StaticEntity.SUPER_GUM) + grid.getStaticEntityCount(StaticEntity.GUM) == 0;
    }

    /**
     * Notify the Game that it need to close itself after the next update
     */
    public synchronized void requestClose() {
        closeGameRequested = true;
    }

    /**
     * Return the score got from Eating a Fruit or a Ghost during the last update
     * @return a negative score if the player has eaten a Fruit, a positive one if the player as eaten a Ghost, 0 otherwise
     */
    public synchronized int getDynamicScoreEventValue() {
        int temp = dynamicScore;
        dynamicScore = 0;
        return temp;
    }

    /**
     * Get the number of lives of the player
     * @return the number of lives of the player
     */
    public int getLives() {
        return getPlayer().getLives();
    }

    /**
     * Return the position of a specific MoveableEntity
     * @param entity the entity to find
     * @return the entity's position
     */
    public synchronized Point getPosition(MoveableEntity entity) {
        return grid.getPosition(entity);
    }

    /**
     * Get the player MoveableEntity
     * @return the player, null if not found
     */
    public EntityPlayer getPlayer() {
        for (MoveableEntity e : grid.getEntities()) {
            if (e instanceof EntityPlayer) {
                return (EntityPlayer) e;
            }
        }
        return null;
    }

    /**
     * Return a MoveableEntity with a specific name
     * @param name the name of the entity to find
     * @return the Entity, null if not found
     */
    public EntityGhost getGhost(GhostName name) {
        for (MoveableEntity e : grid.getEntities()) {
            if (e instanceof EntityGhost && ((EntityGhost) e).getName() == name) {
                return (EntityGhost) e;
            }
        }
        return null;
    }

    /**
     * Return the size of the map along the x-axis
     * @return the size of the map along the x-axis
     */
    public int getSizeX() {
        return grid.getSizeX();
    }

    /**
     * Return the size of the map along the y-axis
     * @return the size of the map along the y-axis
     */
    public int getSizeY() {
        return grid.getSizeY();
    }

    /**
     * Return the StaticEntity at a certain position
     * @param pos the position to check
     * @return the StaticEntity at pos
     */
    public StaticEntity getTileType(Point pos) {
        return grid.getStaticEntity(pos);
    }

    /**
     * Return the StaticEntity at a certain position moved by a dir
     * @param dir the dir to move
     * @param pos the position to test
     * @return the StaticEntity at pos + dir
     */
    public StaticEntity getTileType(Movement dir, Point pos) {
        return grid.getStaticEntity(dir, pos);
    }

    /**
     * Return the total score got since the start of the game
     * @return the total score
     */
    public synchronized int getTotalScore() {
        return totalScore;
    }

    /**
     * Reset the player and put him at his starting position
     */
    public void resetPlayer() {
        for (MoveableEntity e : grid.getEntities()) {
            if (e instanceof EntityPlayer) {
                grid.resetEntity(e);
            }
        }
    }

    /**
     * Reset every Ghosts and put them at their starting position
     */
    public void resetGhosts() {
        for (MoveableEntity e : grid.getEntities()) {
            if (e instanceof EntityGhost) {
                grid.resetEntity(e);
            }
        }
    }

    /**
     * Return the current level
     * @return the current level
     */
    public synchronized int getLevel() {
        return level;
    }

    /**
     * Return whether or not the player has eaten a Ghost since the last Movement
     * @return has the player eaten a Ghost since last Movement
     */
    public boolean hasEatenGhost() {
        return getPlayer().hasEatenGhost();
    }

    /**
     * Return whether or not the player has received an extra life since the last Movement
     * @return has the player received an extra life since last Movement
     */
    public boolean hasEatenFruit() {
        return getPlayer().hasEatenFruit();
    }

    /**
     * Return whether or not the player has died since the last Movement
     * @return has the player died since last Movement
     */
    public boolean hasPlayerDied() {
        return getPlayer().hasDied();
    }

    /**
     * Notify the entity that it has received an extra life
     * Useful for managing the sound
     */
    public boolean hasExtraLife() {
        return getPlayer().hasExtraLife();
    }

    /**
     * Notify the entity that it has eaten a GUM
     * Useful for managing the sound
     */
    public boolean hasEatenGum() {
        return getPlayer().hasEatenGum();
    }

    /**
     * Return whether or not one of the ghost is in FRIGHTENED State
     * @return Is one of the ghost in FRIGHTENED State
     */
    public boolean areGhostFrightened() {
        for (MoveableEntity e : grid.getEntities()) {
            if (e instanceof EntityGhost && ((EntityGhost) e).getState() == GhostState.FRIGHTENED)
                return true;
        }
        return false;
    }

    /**
     * Return whether or not one of the ghost is in EATEN State
     * @return Is one of the ghost in EATEN State
     */
    public boolean areGhostEaten() {
        for (MoveableEntity e : grid.getEntities()) {
            if (e instanceof EntityGhost && ((EntityGhost) e).getState() == GhostState.EATEN)
                return true;
        }
        return false;
    }

    /**
     * Add a new Highscore to the current highscores
     * @param score the score to add
     */
    public synchronized void addHighscore(Score score) {
        highscores.add(score);
        Collections.sort(highscores);
        Collections.reverse(highscores);
    }
}
