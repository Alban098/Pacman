package modele;

import modele.entities.EntityGhost;
import modele.entities.EntityPlayer;
import modele.entities.MoveableEntity;
import modele.enums.GhostName;
import modele.enums.GhostState;
import modele.enums.Movement;
import modele.entities.StaticEntity;

import java.awt.*;
import java.util.Observable;

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

    private final Grid grid;

    private boolean isGameFinished = false;
    private boolean isGameStarted = false;
    protected int totalScore = 0;
    private int level = 0;
    protected int levelScore = 0;
    protected int lastLevelScore = 0;
    protected int dynamicScore = 0;

    private long whenToRestart;
    private boolean closeGameRequested = false;
    private long nbUpdates = 0;

    private CollisionManager collisionManager;

    public Game(String map) {
        grid = new Grid(map, GhostName.BLINKY, GhostName.PINKY, GhostName.INKY, GhostName.CLYDE);
        collisionManager = new CollisionManager(grid, this);
        whenToRestart = System.currentTimeMillis() + RESTART_DELAY - 1000;
    }

    @Override
    public void run() {
        long elaspedTime;
        while (!closeGameRequested) {
            elaspedTime = gameLogic();
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
            if (getPlayer().isDead() && getPlayer().getLives() <= 0 || isGameWinned) {
                isGameFinished = true;
                isGameStarted = false;
            } if (getPlayer().isDead()) {
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

    public boolean startGame() {
        if (System.currentTimeMillis() >= whenToRestart) {
            isGameStarted = true;
            getPlayer().setDead(false);
            isGameFinished = false;
            grid.startEntities();
            return true;
        }
        return false;
    }

    public void setNextPlayerAction(Movement action) {
        getPlayer().setAction(action);
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

    public boolean isGameFinished() {
        return isGameFinished;
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public boolean canStart() {
        return whenToRestart <= System.currentTimeMillis();
    }

    public boolean isPlayerDead() {
        return getPlayer().isDead();
    }

    private boolean allGumEaten() {
        return grid.getStaticEntityCount(StaticEntity.SUPER_GUM) + grid.getStaticEntityCount(StaticEntity.GUM) == 0;
    }

    public synchronized void requestClose() {
        closeGameRequested = true;
    }

    public int getDynamicScoreEventValue() {
        int temp = dynamicScore;
        dynamicScore = 0;
        return temp;
    }

    public int getLives() {
        return getPlayer().getLives();
    }


    public Point getPosition(MoveableEntity entity) {
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

    public int getTotalScore() {
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

    public int getLevel() {
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
}
