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

    public synchronized void kill() {
        running = false;
    }

    public synchronized long getLastActionTime() {
        return lastActionTime;
    }

    public synchronized Movement getCurrentDirection() {
        return currentDirection;
    }

    public synchronized void reset() {
        lastActionTime = 0;
    }

    public Point getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Point spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    protected abstract void update();
}
