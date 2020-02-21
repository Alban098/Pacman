package modele.entities;

import modele.*;
import modele.logic.TargetTileFinder;

public class EntityGhost extends MoveableEntity implements Runnable {

    private static final long[] MODE_SWITCH_ARRAY = {7000, 20000, 7000, 20000, 5000, 20000, 5000, -1};

    private final TargetTileFinder pathFinding;
    private GhostState state;

    private long whenToGetOutOfFrightenedState = 0;
    private long whenToSwitchState = 0;
    private long whenToStart = 0;

    private final int startingTime;
    private int modeSwitchIndex = 1;

    public EntityGhost(TargetTileFinder pathFinding, Grid grid, int startingTime) {
        super(grid);
        state = GhostState.STILL;
        currentDirection = Movement.NONE;
        this.startingTime = startingTime;
        this.pathFinding = pathFinding;
    }

    public synchronized GhostState getState() {
        return state;
    }

    public synchronized void setState(GhostState state) {
        if (state == GhostState.FRIGHTENED) {
            whenToGetOutOfFrightenedState = System.currentTimeMillis() + 10000;
        }
        if (state == GhostState.FRIGHTENED || state == GhostState.CHASE || state == GhostState.SCATTER)
            switchDirection();
        this.state = state;
    }

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

    @Override
    protected void update() {
        if (state == GhostState.STILL) {
            if (System.currentTimeMillis() >= whenToStart) {
                grid.moveToHome(this);
                setState(GhostState.SCATTER);
            }
            return;
        } else if (state == GhostState.FRIGHTENED) {
            if (System.currentTimeMillis() >= whenToGetOutOfFrightenedState)
                setState(GhostState.CHASE);
        } else if (state == GhostState.EATEN) {
            if (grid.getPosition(this).equals(grid.getGhostHome()))
                setState(GhostState.CHASE);
        } else if (modeSwitchIndex < MODE_SWITCH_ARRAY.length && System.currentTimeMillis() >= whenToSwitchState && state != GhostState.STILL) {
            whenToSwitchState += MODE_SWITCH_ARRAY[modeSwitchIndex];
            if (modeSwitchIndex % 2 == 1) {
                setState(GhostState.CHASE);
            } else
                setState(GhostState.SCATTER);
        }
        Movement target = pathFinding.getDirection(grid, this, grid.getPlayer());
        if (requestedAction != null)
            target = requestedAction;
        requestedAction = null;
        if (grid.canMove(target, this)) {
            grid.move(target, this);
            currentDirection = target;
        } else {
            currentDirection = Movement.NONE;
        }
        lastActionTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        whenToSwitchState = System.currentTimeMillis() + MODE_SWITCH_ARRAY[0];
        whenToStart = System.currentTimeMillis() + startingTime;
        while(running) {
            update();
            try {
                int sleepTime;
                switch (state) {
                    case EATEN:
                        sleepTime = PacMan.EATEN_GHOST_UPDATE_INTERVAL;
                        break;
                    case FRIGHTENED:
                        sleepTime = PacMan.FRIGHTENED_GHOST_UPDATE_INTERVAL;
                        break;
                    default:
                        sleepTime = PacMan.GHOST_UPDATE_INTERVAL;
                        break;
                }
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        running = true;
    }

    @Override
    public void reset() {
        super.reset();
        state = GhostState.STILL;
        currentDirection = Movement.NONE;
        whenToGetOutOfFrightenedState = 0;
        whenToSwitchState = 0;
        whenToStart = 0;
        modeSwitchIndex = 1;
    }
}
