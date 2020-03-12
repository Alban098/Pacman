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

    public static Game getInstance() {
        if (instance == null)
            instance = new Game();
        return instance;
    }

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

    public synchronized GameState getGameState() {
        return gameState;
    }

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

    public synchronized List<Score> getHighscores() {
        return highscores;
    }

    public synchronized int getScoreToSave() {
        return scoreToSave;
    }

    public synchronized void setNbPlayer(int nbPlayer) {
        this.nbPlayer = Utils.wrap(nbPlayer, 1, 2);
    }

    public Set<MoveableEntity> getEntities() {
        return grid.getEntities();
    }

    public void setNextPlayerAction(Movement action, int id) {
        if (id == 0)
            getPlayer().setAction(action);
        else
            for (MoveableEntity e : grid.getEntities())
                if (e instanceof EntityGhost && ((EntityGhost) e).isPlayerControlled())
                    ((EntityGhost) e).setAction(action);
    }

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

    public Movement getDirection(MoveableEntity entity) {
        return entity.getCurrentDirection();
    }

    public synchronized boolean isGameFinished() {
        return isGameFinished;
    }

    public synchronized boolean isGameStarted() {
        return isGameStarted;
    }

    public synchronized boolean canStart() {
        return whenToRestart <= System.currentTimeMillis();
    }

    public synchronized boolean isPlayerDead() {
        return getPlayer().isDead();
    }

    private synchronized boolean allGumEaten() {
        return grid.getStaticEntityCount(StaticEntity.SUPER_GUM) + grid.getStaticEntityCount(StaticEntity.GUM) == 0;
    }

    public synchronized void requestClose() {
        closeGameRequested = true;
    }

    public synchronized int getDynamicScoreEventValue() {
        int temp = dynamicScore;
        dynamicScore = 0;
        return temp;
    }

    public int getLives() {
        return getPlayer().getLives();
    }

    public synchronized Point getPosition(MoveableEntity entity) {
        return grid.getPosition(entity);
    }

    public EntityPlayer getPlayer() {
        for (MoveableEntity e : grid.getEntities()) {
            if (e instanceof EntityPlayer) {
                return (EntityPlayer) e;
            }
        }
        return null;
    }

    public EntityGhost getGhost(GhostName name) {
        for (MoveableEntity e : grid.getEntities()) {
            if (e instanceof EntityGhost && ((EntityGhost) e).getName() == name) {
                return (EntityGhost) e;
            }
        }
        return null;
    }

    public int getSizeX() {
        return grid.getSizeX();
    }

    public int getSizeY() {
        return grid.getSizeY();
    }

    public StaticEntity getTileType(Point pos) {
        return grid.getStaticEntity(pos);
    }

    public StaticEntity getTileType(Movement dir, Point pos) {
        return grid.getStaticEntity(dir, pos);
    }

    public synchronized int getTotalScore() {
        return totalScore;
    }

    public void resetPlayer() {
        for (MoveableEntity e : grid.getEntities()) {
            if (e instanceof EntityPlayer) {
                grid.resetEntity(e);
            }
        }
    }

    public void resetGhosts() {
        for (MoveableEntity e : grid.getEntities()) {
            if (e instanceof EntityGhost) {
                grid.resetEntity(e);
            }
        }
    }

    public synchronized int getLevel() {
        return level;
    }

    public boolean hasEatenGhost() {
        return getPlayer().hasEatenGhost();
    }

    public boolean hasEatenFruit() {
        return getPlayer().hasEatenFruit();
    }

    public boolean hasPlayerDied() {
        return getPlayer().hasDied();
    }

    public boolean hasExtraLife() {
        return getPlayer().hasExtraLife();
    }

    public boolean hasEatenGum() {
        return getPlayer().hasEatenGum();
    }

    public boolean areGhostFrightened() {
        for (MoveableEntity e : grid.getEntities()) {
            if (e instanceof EntityGhost && ((EntityGhost) e).getState() == GhostState.FRIGHTENED)
                return true;
        }
        return false;
    }

    public boolean areGhostEaten() {
        for (MoveableEntity e : grid.getEntities()) {
            if (e instanceof EntityGhost && ((EntityGhost) e).getState() == GhostState.EATEN)
                return true;
        }
        return false;
    }

    public synchronized void addHighscore(Score score) {
        highscores.add(score);
        Collections.sort(highscores);
        Collections.reverse(highscores);
    }
}
