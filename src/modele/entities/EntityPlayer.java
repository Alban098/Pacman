package modele.entities;

import modele.Grid;
import modele.Movement;
import modele.Game;

public class EntityPlayer extends MoveableEntity implements Runnable {

    private int eatenGhostMultiplier = 1;
    private boolean isDead = false;

    public EntityPlayer(Grid grid) {
        super(grid);
        requestedAction = Movement.NONE;
        currentDirection = Movement.NONE;
    }

    public int getEatenGhostMultiplier() {
        return eatenGhostMultiplier;
    }

    public void incrementEatenGhostMultiplier() {
        eatenGhostMultiplier++;
    }

    public void resetEatenGhostMultiplier() {
        eatenGhostMultiplier = 1;
    }

    @Override
    protected void update() {
        if (grid.canMove(requestedAction, this)) {
            grid.move(requestedAction, this);
            currentDirection = requestedAction;
        } else if (grid.canMove(currentDirection, this)) {
                grid.move(currentDirection, this);
        } else {
            currentDirection = Movement.NONE;
        }
        lastActionTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while (running) {
            update();
            try {
                Thread.sleep(Game.PLAYER_UPDATE_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        running = true;
    }

    public synchronized void setAction(Movement dir) {
        requestedAction = dir;
    }

    @Override
    public synchronized void reset() {
        super.reset();
        eatenGhostMultiplier = 1;
        currentDirection = Movement.NONE;
        requestedAction = Movement.NONE;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }
}
