package modele.entities;

import modele.Grid;
import modele.Movement;

public abstract class MoveableEntity implements Runnable{

    protected final Grid grid;
    protected Movement currentDirection;
    protected Movement requestedAction;
    protected boolean running = true;

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

    protected abstract void update();
}
