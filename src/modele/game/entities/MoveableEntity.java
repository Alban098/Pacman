package modele.game.entities;

import modele.game.Grid;
import modele.game.enums.Movement;

import java.awt.*;

public abstract class MoveableEntity implements Runnable{

    protected final Grid grid;
    protected Movement currentDirection;
    protected Movement requestedAction;
    protected boolean running = true;
    private Point spawnPoint;

    protected long lastActionTime = 0;

    public MoveableEntity(Grid grid) {
        this.grid = grid;
    }

    /**
     * Kill the entity by ending it's Thread
     */
    public synchronized void kill() {
        running = false;
    }

    /**
     * Return the time in ms at which the last action was performed
     * @return the last Movement's time
     */
    public synchronized long getLastActionTime() {
        return lastActionTime;
    }

    /**
     * Return the last action performed by the entity
     * @return the last Movement performed by the entity
     */
    public synchronized Movement getCurrentDirection() {
        return currentDirection;
    }

    /**
     * Reset the entity to it's initial state
     */
    public synchronized void reset() {
        lastActionTime = 0;
    }

    /**
     * Return the entity's Spawn Point
     * @return the entity's Spawn Point
     */
    public Point getSpawnPoint() {
        return spawnPoint;
    }

    /**
     * Set the entity's Spawn Point
     * @param spawnPoint the new Spawn Point
     */
    public void setSpawnPoint(Point spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    /**
     * Update the entity's behavior
     */
    protected abstract void update();
}
