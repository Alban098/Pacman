package modele.game.logic;

import modele.game.Grid;
import modele.game.entities.EntityGhost;
import modele.game.entities.EntityPlayer;
import modele.game.enums.GhostState;
import modele.game.enums.Movement;

import java.awt.*;

public class TargetPinky extends TargetTileFinder{

    @Override
    public Movement getDirection(Grid grid, EntityGhost ghost, EntityPlayer player) {
        Movement commonBehaviour = getCommonBehaviour(grid, ghost);
        if (commonBehaviour != null)
            return commonBehaviour;
        else {
            if (ghost.getState() == GhostState.SCATTER) {
                Point target = new Point(0, 0);
                return getBestDirToTarget(grid, ghost, target);
            } else if (ghost.getState() == GhostState.CHASE) {
                Point target = new Point(grid.getPosition(player));
                switch (player.getCurrentDirection()) {
                    case UP:
                        target.y -= 2;
                        target.x -= 2; //Simulating 8 Bits overflow inside 16 Bits register
                        break;
                    case DOWN:
                        target.y +=2;
                        break;
                    case LEFT:
                        target.x -=2;
                        break;
                    case RIGHT:
                        target.x += 2;
                        break;
                }
                return getBestDirToTarget(grid, ghost, target);
            }
        }
        return Movement.UP;
    }
}
