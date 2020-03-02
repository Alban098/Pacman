package modele.game.logic;

import modele.game.Grid;
import modele.Utils;
import modele.game.entities.EntityGhost;
import modele.game.entities.EntityPlayer;
import modele.game.enums.GhostState;
import modele.game.enums.Movement;

import java.awt.*;

public class TargetClyde extends TargetTileFinder {

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
