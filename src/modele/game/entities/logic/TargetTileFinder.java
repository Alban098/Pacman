package modele.game.entities.logic;

import modele.game.Grid;
import modele.Utils;
import modele.game.entities.EntityGhost;
import modele.game.entities.EntityPlayer;
import modele.game.enums.GhostName;
import modele.game.enums.GhostState;
import modele.game.enums.Movement;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class TargetTileFinder {

    /**
     * Calculate the target point and
     * return the direction that minimise the euclidean distance to that point
     * @param grid the grid in which to move
     * @param ghost the ghost trying to move
     * @param player the player used to calculate the target point
     * @return a Movement representing the optimal direction
     */
    public abstract Movement getDirection(Grid grid, EntityGhost ghost, EntityPlayer player);

    /**
     * If the ghost trying to move is in certain states : FRIGHTENED, EATEN, STILL or STARTING
     * the behavior is common to every ghosts
     * this methods compute this common behavior
     * @param grid the grid in xhich to move
     * @param ghost the ghost trying to move
     * @return a Movement representing the common behavior's goal, null if not relevant)
     */
    protected Movement getCommonBehaviour(Grid grid, EntityGhost ghost) {
        if (ghost.getState() == GhostState.FRIGHTENED) {
            List<Movement> legalMoves = getLegalMoves(grid, ghost);
            return legalMoves.get(new Random().nextInt(legalMoves.size()));
        } else if (ghost.getState() == GhostState.EATEN) {
            Point target = ghost.getSpawnPoint();
            return getBestDirToTarget(grid, ghost, target);
        } else if (ghost.getState() == GhostState.STILL) {
            Point target = ghost.getSpawnPoint();
            return getBestDirToTarget(grid, ghost, target);
        } else if (ghost.getState() == GhostState.STARTING) {
            Point target = ghost.getStartingPoint();
            return getBestDirToTarget(grid, ghost, target);
        }
        return null;
    }

    /**
     * Return a List of Movement that are allowed for a ghost in a grid
     * @param grid the grid in which to move
     * @param ghost the ghost trying to move
     * @return a List of authorized Movement
     */
    protected List<Movement> getLegalMoves(Grid grid, EntityGhost ghost) {
        List<Movement> legalMoves = new ArrayList<>();
        switch (ghost.getCurrentDirection()) {
            case UP:
                if (grid.canMove(Movement.LEFT, ghost)) legalMoves.add(Movement.LEFT);
                if (grid.canMove(Movement.RIGHT, ghost)) legalMoves.add(Movement.RIGHT);
                if (grid.canMove(Movement.UP, ghost)) legalMoves.add(Movement.UP);
                if (legalMoves.size() == 0) legalMoves.add(Movement.DOWN);
                break;
            case DOWN:
                if (grid.canMove(Movement.LEFT, ghost)) legalMoves.add(Movement.LEFT);
                if (grid.canMove(Movement.DOWN, ghost)) legalMoves.add(Movement.DOWN);
                if (grid.canMove(Movement.RIGHT, ghost)) legalMoves.add(Movement.RIGHT);
                if (legalMoves.size() == 0) legalMoves.add(Movement.UP);
                break;
            case LEFT:
                if (grid.canMove(Movement.LEFT, ghost)) legalMoves.add(Movement.LEFT);
                if (grid.canMove(Movement.DOWN, ghost)) legalMoves.add(Movement.DOWN);
                if (grid.canMove(Movement.UP, ghost)) legalMoves.add(Movement.UP);
                if (legalMoves.size() == 0) legalMoves.add(Movement.RIGHT);
                break;
            case RIGHT:
                if (grid.canMove(Movement.DOWN, ghost)) legalMoves.add(Movement.DOWN);
                if (grid.canMove(Movement.RIGHT, ghost)) legalMoves.add(Movement.RIGHT);
                if (grid.canMove(Movement.UP, ghost)) legalMoves.add(Movement.UP);
                if (legalMoves.size() == 0) legalMoves.add(Movement.LEFT);
                break;
            case NONE:
                if (grid.canMove(Movement.LEFT, ghost)) legalMoves.add(Movement.LEFT);
                if (grid.canMove(Movement.DOWN, ghost)) legalMoves.add(Movement.DOWN);
                if (grid.canMove(Movement.RIGHT, ghost)) legalMoves.add(Movement.RIGHT);
                if (grid.canMove(Movement.UP, ghost)) legalMoves.add(Movement.UP);
                break;
        }
        return legalMoves;
    }

    /**
     * When to target position has been determined
     * it will determine the best one from the authorized moves
     * the best one is the one minimizing the Euclidean Distance to the target
     * @param grid the grid in which to move
     * @param ghost the ghost trying to move
     * @param target the position the ghost try to reach
     * @return the best Movement allowed
     */
    protected Movement getBestDirToTarget(Grid grid, EntityGhost ghost, Point target) {
        List<Movement> legalMoves = getLegalMoves(grid, ghost);
        float minDist = Float.MAX_VALUE;
        Movement preferedMove = Movement.UP;
        Point ghostPos = grid.getPosition(ghost);
        for (Movement movement : legalMoves) {
            float dist;
            switch (movement) {
                case UP:
                    dist = Utils.getDistSquared(target, new Point(ghostPos.x, ghostPos.y - 1));
                    if (dist <= minDist) {
                        minDist = dist;
                        preferedMove = Movement.UP;
                    }
                    break;
                case DOWN:
                    dist = Utils.getDistSquared(target, new Point(ghostPos.x, ghostPos.y + 1));
                    if (dist <= minDist) {
                        minDist = dist;
                        preferedMove = Movement.DOWN;
                    }
                    break;
                case LEFT:
                    dist = Utils.getDistSquared(target, new Point(ghostPos.x - 1, ghostPos.y));
                    if (dist <= minDist) {
                        minDist = dist;
                        preferedMove = Movement.LEFT;
                    }
                    break;
                case RIGHT:
                    dist = Utils.getDistSquared(target, new Point(ghostPos.x + 1, ghostPos.y));
                    if (dist <= minDist) {
                        minDist = dist;
                        preferedMove = Movement.RIGHT;
                    }
                    break;
            }
        }
        return preferedMove;
    }

    /**
     * Return a TargetFinder for a specific GhostName
     * @param name the GhostName of the TargetFinder
     * @return the appropriate TargetFinder
     */
    public static TargetTileFinder getTargetFinder(GhostName name) {
        switch (name) {
            case INKY:
                return new TargetInky();
            case PINKY:
                return new TargetPinky();
            case CLYDE:
                return new TargetClyde();
            case BLINKY:
            default:
                return new TargetBlinky();
        }
    }

}
