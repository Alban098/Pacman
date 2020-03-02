package modele.game.logic;

import modele.game.Grid;
import modele.game.entities.EntityGhost;
import modele.game.entities.EntityPlayer;
import modele.game.entities.MoveableEntity;
import modele.game.enums.GhostName;
import modele.game.enums.GhostState;
import modele.game.enums.Movement;

import java.awt.*;

public class TargetInky extends TargetTileFinder {

    @Override
    public Movement getDirection(Grid grid, EntityGhost ghost, EntityPlayer player) {
        Movement commonBehaviour = getCommonBehaviour(grid, ghost);
        if (commonBehaviour != null)
            return commonBehaviour;
        else {
            if (ghost.getState() == GhostState.SCATTER) {
                Point target = new Point(grid.getSizeX() - 1, grid.getSizeY() - 1);
                return getBestDirToTarget(grid, ghost, target);
            } else if (ghost.getState() == GhostState.CHASE) {
                Point target = new Point(grid.getPosition(player));
                switch (player.getCurrentDirection()) {
                    case UP:
                        target.y--;
                        target.x--; //Simulating 8 Bits overflow inside 16 Bits register
                        break;
                    case DOWN:
                        target.y++;
                        break;
                    case LEFT:
                        target.x--;
                        break;
                    case RIGHT:
                        target.x++;
                        break;
                }
                Point blinkyPos = new Point(0, 0);
                for (MoveableEntity e : grid.getEntities())
                    if (e instanceof EntityGhost && ((EntityGhost) e).getName() == GhostName.BLINKY)
                        blinkyPos = grid.getPosition(e);
                target.x += target.x - blinkyPos.x;
                target.y += target.y - blinkyPos.y;
                return getBestDirToTarget(grid, ghost, target);
            }
        }
        return Movement.UP;
    }
}
