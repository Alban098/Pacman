package modele.game.entities;

import modele.game.Grid;
import modele.game.enums.Movement;
import modele.game.Game;

public class EntityPlayer extends MoveableEntity implements Runnable {

    private int eatenGhostMultiplier = 1;
    private boolean isDead = false;
    private boolean hasDied = false;
    private boolean hasEatenGhost = false;
    private boolean hasEatenFruit = false;
    private boolean hasExtraLife = false;
    private boolean hasEatenGum = false;
    private int lives = 2;

    public EntityPlayer(Grid grid) {
        super(grid);
        requestedAction = Movement.NONE;
        currentDirection = Movement.NONE;
    }

    /**
     * Return the Score Multiplier applied when eating a ghost
     * @return the Score Multiplier applied when eating a ghost
     */
    public synchronized int getEatenGhostMultiplier() {
        return eatenGhostMultiplier;
    }

    /**
     * Increment the Score Multiplier applied when eating a ghost  by 1
     */
    public synchronized void incrementEatenGhostMultiplier() {
        eatenGhostMultiplier++;
    }

    /**
     * Reset the Score Multiplier applied when eating a ghost
     */
    public synchronized void resetEatenGhostMultiplier() {
        eatenGhostMultiplier = 1;
    }

    /**
     * Update the entity by executing the requested Movement if possible
     */
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

    /**
     * Manage the entity's behavior
     */
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

    /**
     * Set the player's next action
     * will be performed when possible if not modified before
     * @param dir requested Movement
     */
    public synchronized void setAction(Movement dir) {
        requestedAction = dir;
    }

    /**
     * Reset the entity to it's initial state without starting it
     */
    @Override
    public synchronized void reset() {
        super.reset();
        eatenGhostMultiplier = 1;
        currentDirection = Movement.NONE;
        requestedAction = Movement.NONE;
        if (lives >= 0)
            this.isDead = false;
    }

    /**
     * Return whether or not the player is dead
     * @return is the player dead
     */
    public synchronized boolean isDead() {
        return isDead;
    }

    /**
     * Return whether or not the player has died since the last Movement
     * @return has the player died since last Movement
     */
    public synchronized boolean hasDied() {
        boolean tmp = hasDied;
        hasDied = false;
        return tmp;
    }

    /**
     * Return whether or not the player has eaten a Ghost since the last Movement
     * @return has the player eaten a Ghost since last Movement
     */
    public synchronized boolean hasEatenGhost() {
        boolean tmp = hasEatenGhost;
        hasEatenGhost = false;
        return tmp;
    }

    /**
     * Return whether or not the player has eaten a FRUIT since the last Movement
     * @return has the player eaten a FRUIT since last Movement
     */
    public synchronized boolean hasEatenFruit() {
        boolean tmp = hasEatenFruit;
        hasEatenFruit = false;
        return tmp;
    }

    /**
     * Return whether or not the player has received an extra life since the last Movement
     * @return has the player received an extra life since last Movement
     */
    public synchronized boolean hasExtraLife() {
        boolean tmp = hasExtraLife;
        hasExtraLife = false;
        return tmp;
    }

    /**
     * Return whether or not the player has eaten a GUM since the last Movement
     * @return has the player eaten a GUM since last Movement
     */
    public synchronized boolean hasEatenGum() {
        boolean tmp = hasEatenGum;
        hasEatenGum = false;
        return tmp;
    }

    /**
     * Set the player state to DEAD
     */
    public synchronized void setDead(boolean dead) {
        if (dead)
            hasDied = true;
        isDead = dead;
    }

    /**
     * Notify the entity that it has eaten a Ghost
     * Useful for managing the sound
     */
    public synchronized void setHasEatenGhost() {
        this.hasEatenGhost = true;
    }

    /**
     * Notify the entity that it has eaten a FRUIT
     * Useful for managing the sound
     */
    public synchronized void setHasEatenFruit() {
        this.hasEatenFruit = true;
    }

    /**
     * Notify the entity that it has received an extra life
     * Useful for managing the sound
     */
    public synchronized void setHasExtraLife() {
        this.hasExtraLife = true;
    }

    /**
     * Notify the entity that it has eaten a GUM
     * Useful for managing the sound
     */
    public synchronized void setHasEatenGum() {
        this.hasEatenGum = true;
    }

    /**
     * Get the number of lives of the player
     * @return the number of lives of the player
     */
    public synchronized int getLives() {
        return lives;
    }

    /**
     * Set the number of lives of the player
     * @param lives the new number of lives
     */
    public synchronized void setLives(int lives) {
        this.lives = lives;
    }
}
