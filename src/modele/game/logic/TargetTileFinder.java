package modele.game.logic;

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

    public abstract Movement getDirection(Grid grid, EntityGhost ghost, EntityPlayer player);

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
