package modele.logic;

import modele.Grid;
import modele.entities.EntityGhost;
import modele.entities.EntityPlayer;
import modele.GhostState;
import modele.Movement;

import java.awt.*;

public class TargetInky extends TargetTileFinder {

    private final EntityGhost blinky;

    public TargetInky(EntityGhost blinky) {
        this.blinky = blinky;
    }

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
                Point blinkyPos = grid.getPosition(blinky);
                target.x += target.x - blinkyPos.x;
                target.y += target.y - blinkyPos.y;
                return getBestDirToTarget(grid, ghost, target);
            }
        }
        return Movement.UP;
    }
}
