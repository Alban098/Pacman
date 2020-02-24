package modele;

import modele.entities.EntityGhost;
import modele.entities.EntityPlayer;
import modele.entities.MoveableEntity;

import java.awt.*;
import java.util.Observable;

public class PacMan extends Observable implements Runnable {

    public static final int GHOST_UPDATE_INTERVAL = 174;
    public static final int FRIGHTENED_GHOST_UPDATE_INTERVAL = 210;
    public static final int EATEN_GHOST_UPDATE_INTERVAL = 75;
    public static final int PLAYER_UPDATE_INTERVAL = 175;
    public static final int FRAME_DURATION = 20;
    public static final int UPDATE_PER_FRAME = 5;
    public static final int RESTART_DELAY = 2000;

    private final Grid grid;

    private boolean isPlayerDead = false;
    private boolean isGameFinished = false;
    private boolean isGameStarted = false;
    private int dynamicScore = 0;

    private long whenToRestart;
    private boolean closeGameRequested = false;
    private long nbUpdates = 0;

    public PacMan(String map) {
        grid = new Grid(map);
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
            isPlayerDead = grid.testCollision();
            boolean isGameWinned = grid.isGameFinished();
            if (isPlayerDead && grid.getLives() <= 0 || isGameWinned)
                isGameFinished = true;
            else if (isPlayerDead) {
                isGameStarted = false;
                whenToRestart = System.currentTimeMillis() + RESTART_DELAY;
                grid.stopGame();
                grid.resetGhost();
                grid.setLives(grid.getLives() - 1);
            }
            if (isGameFinished) {
                grid.stopGame();
                grid.resetGhost();
            }
            if (isGameWinned) {
                grid.nextLevel();
                whenToRestart = System.currentTimeMillis() + RESTART_DELAY;
                isGameStarted = false;
                isGameFinished = false;
            }
            if (dynamicScore == 0)
                dynamicScore = grid.getDynamicScore();
        }
        if (nbUpdates % UPDATE_PER_FRAME == 0) {
            setChanged();
            notifyObservers();
        }
        return System.currentTimeMillis() - startTime;
    }

    public void setNextPlayerAction(Movement action) {
        grid.getPlayer().setAction(action);
    }

    public Point getPosition(MoveableEntity entity) {
        return grid.getPosition(entity);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isGameStarted() {
        return isGameStarted;
    }

    public int getLives() {
        return grid.getLives();
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

    public MoveableEntity getPlayer() {
        return grid.getPlayer();
    }

    public EntityGhost getGhost(GhostName name) {
        return grid.getGhost(name);
    }

    public Movement getDirection(MoveableEntity entity) {
        return entity.getCurrentDirection();
    }

    public int getSizeX() {
        return grid.getSizeX();
    }

    public int getSizeY() {
        return grid.getSizeY();
    }

    public StaticEntity getTileType(Point pos) {
        return grid.getTileType(pos);
    }

    public StaticEntity getTileType(Movement dir, Point pos) {
        return grid.getStaticEntity(dir, pos);
    }

    public boolean isPlayerDead() {
        return isPlayerDead;
    }

    public boolean isGameFinished() {
        return isGameFinished;
    }

    public int getTotalScore() {
        return grid.getTotalScore();
    }

    public boolean startGame() {
        if (System.currentTimeMillis() >= whenToRestart) {
            isGameStarted = true;
            isGameFinished = false;
            isPlayerDead = false;
            grid.startEntities();
            return true;
        }
        return false;
    }

    public void resetPlayer() {
        grid.resetPlayer();
    }

    public synchronized void requestClose() {
        closeGameRequested = true;
    }

    public int getDynamicScoreEventValue() {
        int temp = dynamicScore;
        dynamicScore = 0;
        return temp;
    }

    public int getLevel() {
        return grid.getLevel();
    }
}
