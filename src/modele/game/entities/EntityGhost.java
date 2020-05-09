package modele.game.entities;

import modele.game.Game;
import modele.game.Grid;
import modele.game.enums.GhostName;
import modele.game.enums.GhostState;
import modele.game.enums.Movement;
import modele.game.entities.logic.TargetTileFinder;

import java.awt.*;

/**
 * <pre>
 * A Ghost work using GhostStates that will dictate the way we compute his movement
 * if a Ghost hasn't started yet he is in STILL Mode
 * When he start, he will switch to STARTING Mode and seek his Starting Point
 * When he reach his starting point he will switch to ether SCATTER or CHASE Mode
 * In SCATTER Mode the ghost seek one corner of the map
 * In CHASE Mode the ghost seek a position depending on the player (calculated by his TargetTileFinder)
 * When the player eat a SUPER_GUM, the ghosts will all switch to FRIGHTENED Mode
 * in this Mode, if the player touches a ghost, he will switch to EATEN Mode and seek his Spawn Point
 * When he reach his Spawn Point, it will switch back to STARTING Mode.
 * During the first 85sec of the game, the ghosts will switch between CHASE and SCATTER Mode to offer the player some spare time.
 *
 *  Possible switch :
 *  STILL -> STARTING -> SCATTER -> CHASE -> FRIGHTENED -> EATEN -> STARTING
 *                    -> CHASE -> SCATTER ->            -> CHASE
 *                                                      -> SCATTER
 * </pre>
 */
public class EntityGhost extends MoveableEntity implements Runnable {

    /* The time at which ghosts must switch between Scatter and Chase mode */
    private static final long[] MODE_SWITCH_ARRAY = {9000, 20000, 7000, 20000, 5000, 20000, 4000, -1};

    private final TargetTileFinder pathFinding;
    private final int startingTime;

    private GhostState state;
    private GhostName name;
    private Point startingPoint;

    private long whenToGetOutOfFrightenedState = 0;
    private long whenToSwitchState = 0;
    private long whenToStart = 0;
    private int modeSwitchIndex = 1;
    private boolean playerControlled;

    public EntityGhost(TargetTileFinder pathFinding, Grid grid, int startingTime, GhostName name, boolean playerControlled) {
        super(grid);
        state = GhostState.STILL;
        currentDirection = Movement.NONE;
        requestedAction = Movement.NONE;
        this.startingTime = startingTime;
        this.pathFinding = pathFinding;
        this.name = name;
        this.playerControlled = playerControlled;
    }

    /**
     * Return whether or not the entity is controlled by a player
     * @return is the entity player controlled
     */
    public boolean isPlayerControlled() {
        return playerControlled;
    }

    /**
     * Set whether or not the entity is controlled by a player
     * @param playerControlled is the entity controlled by a player
     */
    public void setPlayerControlled(boolean playerControlled) {
        if (playerControlled)
            state = GhostState.STARTING;
        this.playerControlled = playerControlled;
    }

    /**
     * Return the current state of the entity
     * @return the GhostState of the entity
     */
    public synchronized GhostState getState() {
        return state;
    }

    /**
     * Set the entity's new state
     * and update the relevant variables
     * @param state the new state of the entity
     */
    public synchronized void setState(GhostState state) {
        /* If the ghost became Frightened we calculate the time at which to get out of Frightened mode */
        if (state == GhostState.FRIGHTENED) {
            whenToGetOutOfFrightenedState = System.currentTimeMillis() + Game.FRIGHTENED_DURATION;
        }
        /* If the ghost switch to certain modes, it must do a 180° rotation */
        if (state == GhostState.FRIGHTENED || state == GhostState.CHASE || state == GhostState.SCATTER)
            switchDirection();
        this.state = state;
    }

    /**
     * Make the entity take a 180° turn
     * (only allowed at state switch)
     */
    private void switchDirection() {
        switch (currentDirection) {
            case UP:
                requestedAction = Movement.DOWN;
                break;
            case LEFT:
                requestedAction = Movement.RIGHT;
                break;
            case RIGHT:
                requestedAction = Movement.LEFT;
                break;
            case DOWN:
            case NONE:
                requestedAction = Movement.UP;
                break;
        }
    }

    /**
     * Update the entity's position and desired actions
     */
    @Override
    protected void update() {
        /* if the ghost is controlled by a player and isn't eaten it will execute the action required by the player */
        if (playerControlled && state != GhostState.EATEN) {
            if (grid.canMove(requestedAction, this)) {
                grid.move(requestedAction, this);
                currentDirection = requestedAction;
            } else if (grid.canMove(currentDirection, this)) {
                grid.move(currentDirection, this);
            } else {
                currentDirection = Movement.NONE;
            }
        /* Otherwise we compute the optimal direction*/
        } else {
            /* We first retrieve the player entity*/
            EntityPlayer player = null;
            for (MoveableEntity e : grid.getEntities()) {
                if (e instanceof EntityPlayer) {
                    player = (EntityPlayer) e;
                }
            }
            if (player != null) {
                /* We update the state of the ghost and change it if necessary */
                if (state == GhostState.STILL) {
                    if (System.currentTimeMillis() >= whenToStart) {
                        setState(GhostState.STARTING);
                    }
                    return;
                } else if (state == GhostState.STARTING) {
                    if (grid.getPosition(this).equals(startingPoint))
                        if (modeSwitchIndex >= MODE_SWITCH_ARRAY.length)
                            setState(GhostState.CHASE);
                        else
                            setState(GhostState.SCATTER);
                } else if (state == GhostState.FRIGHTENED) {
                    if (System.currentTimeMillis() >= whenToGetOutOfFrightenedState)
                        setState(GhostState.CHASE);
                } else if (state == GhostState.EATEN) {
                    if (grid.getPosition(this).equals(getSpawnPoint())) {
                        setState(GhostState.STARTING);
                        lastActionTime = System.currentTimeMillis();
                        return;
                    }
                } else if (modeSwitchIndex < MODE_SWITCH_ARRAY.length && System.currentTimeMillis() >= whenToSwitchState && state != GhostState.STILL) {
                    whenToSwitchState += MODE_SWITCH_ARRAY[modeSwitchIndex];
                    if (modeSwitchIndex % 2 == 1)
                        setState(GhostState.CHASE);
                    else
                        setState(GhostState.SCATTER);
                }
                /* We get the optimal direction and apply it */
                Movement target = pathFinding.getDirection(grid, this, player);
                /* if the ghost has an action with priority stored (such as a 180° rotation) it's executed instead */
                if (requestedAction != Movement.NONE)
                    target = requestedAction;
                requestedAction = Movement.NONE;
                /* We move if possible and update the relevant variables*/
                if (grid.canMove(target, this)) {
                    grid.move(target, this);
                    currentDirection = target;
                } else {
                    currentDirection = Movement.NONE;
                }
            }
        }
        lastActionTime = System.currentTimeMillis();
    }

    /**
     * Manage the entity's behavior
     */
    @Override
    public void run() {
        whenToSwitchState = System.currentTimeMillis() + MODE_SWITCH_ARRAY[0];
        whenToStart = System.currentTimeMillis() + startingTime;
        while(running) {
            update();
            try {
                /* Calculate the delay between 2 action depending on the current state */
                int sleepTime;
                switch (state) {
                    case EATEN:
                        sleepTime = Game.EATEN_GHOST_UPDATE_INTERVAL;
                        break;
                    case FRIGHTENED:
                        sleepTime = Game.FRIGHTENED_GHOST_UPDATE_INTERVAL;
                        break;
                    default:
                        sleepTime = Game.GHOST_UPDATE_INTERVAL;
                        break;
                }
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        running = true;
    }

    /**
     * Reset the entity to it's initial state without starting it
     */
    @Override
    public void reset() {
        super.reset();
        state = GhostState.STILL;
        currentDirection = Movement.NONE;
        whenToGetOutOfFrightenedState = 0;
        whenToStart = 0;
        modeSwitchIndex = 1;
    }

    /**
     * Return the name (aka Ghost identifier)
     * @return the GhostName of the entity
     */
    public GhostName getName() {
        return name;
    }

    /**
     * Return the current starting point
     * @return a Point representing the starting position
     */
    public Point getStartingPoint() {
        return startingPoint;
    }

    /**
     * Set the Ghost starting position
     * aka he point targeted when the ghost leave it's spawn point
     * @param startingPoint the new starting point
     */
    public void setStartingPoint(Point startingPoint) {
        this.startingPoint = startingPoint;
    }

    /**
     * Set the next action of the Ghost if controlled by the player
     * @param dir the desired direction
     */
    public synchronized void setAction(Movement dir) {
        if (playerControlled && state != GhostState.EATEN)
            requestedAction = dir;
    }
}
