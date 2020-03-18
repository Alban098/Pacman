package modele.game.entities.logic;

import modele.game.Grid;
import modele.Utils;
import modele.game.entities.EntityGhost;
import modele.game.entities.EntityPlayer;
import modele.game.enums.GhostState;
import modele.game.enums.Movement;

import java.awt.*;

/**
 * Clyde's target point the player's position
 * except if the player closed than 4 tiles, in which case the target point became the bottom left corner
 */
public class TargetClyde extends TargetTileFinder {

    /**
     * Return the Movement minimizing the Euclidean distance to the target point that is the player's position
     * except if the player closed than 4 tiles, in which case the target point became the bottom left corner
     * @param grid the grid in which to move
     * @param ghost the ghost trying to move
     * @param player the player used to calculate the target point
     * @return a Movement representing the optimal direction
     */
    @Override
    public Movement getDirection(Grid grid, EntityGhost ghost, EntityPlayer player) {
        Movement commonBehaviour = getCommonBehaviour(grid, ghost);
        if (commonBehaviour != null)
            return commonBehaviour;
        else {
            if (ghost.getState() == GhostState.SCATTER) {
                Point target = new Point(0, grid.getSizeY() - 1);
                return getBestDirToTarget(grid, ghost, target);
            } else if (ghost.getState() == GhostState.CHASE) {
                Point target = new Point(grid.getPosition(player));
                if (Utils.getDistSquared(grid.getPosition(player), grid.getPosition(ghost)) <= 16)
                    target = new Point(0, grid.getSizeY() - 1);
                return getBestDirToTarget(grid, ghost, target);
            }
        }
        return Movement.UP;
    }
}
