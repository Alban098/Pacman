package modele.game.entities;

import modele.game.Grid;
import modele.game.enums.Movement;
import modele.game.Game;

public class EntityPlayer extends MoveableEntity implements Runnable {

    private int eatenGhostMultiplier = 1;
    private int id;
    private boolean isDead = false;
    private boolean hasDied = false;
    private boolean hasEatenGhost = false;
    private boolean hasEatenFruit = false;
    private boolean hasExtraLife = false;
    private boolean hasEatenGum = false;
    private int lives = 2;

    public EntityPlayer(Grid grid, int id) {
        super(grid);
        this.id = id;
        requestedAction = Movement.NONE;
        currentDirection = Movement.NONE;
    }

    public int getId() {
        return id;
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
        if (lives >= 0)
            this.isDead = false;
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean hasDied() {
        boolean tmp = hasDied;
        hasDied = false;
        return tmp;
    }

    public boolean hasEatenGhost() {
        boolean tmp = hasEatenGhost;
        hasEatenGhost = false;
        return tmp;
    }

    public boolean hasEatenFruit() {
        boolean tmp = hasEatenFruit;
        hasEatenFruit = false;
        return tmp;
    }

    public boolean hasExtraLife() {
        boolean tmp = hasExtraLife;
        hasExtraLife = false;
        return tmp;
    }

    public boolean hasEatenGum() {
        boolean tmp = hasEatenGum;
        hasEatenGum = false;
        return tmp;
    }

    public void setDead(boolean dead) {
        if (dead)
            hasDied = true;
        isDead = dead;
    }

    public void setHasEatenGhost() {
        this.hasEatenGhost = true;
    }

    public void setHasEatenFruit() {
        this.hasEatenFruit = true;
    }

    public void setHasExtraLife() {
        this.hasExtraLife = true;
    }

    public void setHasEatenGum() {
        this.hasEatenGum = true;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }
}
