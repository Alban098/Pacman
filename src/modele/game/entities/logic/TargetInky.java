package modele.game.entities.logic;

import modele.game.Grid;
import modele.game.entities.EntityGhost;
import modele.game.entities.EntityPlayer;
import modele.game.entities.MoveableEntity;
import modele.game.enums.GhostName;
import modele.game.enums.GhostState;
import modele.game.enums.Movement;

import java.awt.*;

/**
 * Inky's target point is the symmetric of Blinky's position relative to the point 1 tile in front of the player (Pivot point)
 * except when facing up, the Pivot point became 1 tiles up and left
 *
 * this is due to an overflow of the position vector in the original game
 *
 * The target point was stored in a 16Bits register as 2 8Bits signed integer
 * 0x[XX YY]
 * Same for each direction
 * UP =    0x[00 FF] -> {0, -1}
 * DOWN =  0x[00 01] -> {0, 1}
 * LEFT =  0x[01 00] -> {1, 0}
 * RIGHT = 0x[FF 00] -> {-1, 0}
 * The direction are flipped compared to out implementation because {0, 0} is at the top right on the original game
 *
 * When the game add that to the player's position it will result in the position 1 tile in front of the player expect for the UP direction :
 * Let's take a random position : {10, 10};
 *          1 1
 *      0x[0A 0A] -> {10, 10}
 *   +  0x[00 FF] -> {0, -1}
 *   ------------
 *   =  Ox[0B 09] -> {11, 9}
 *
 *   this effectively represent 1 tiles up and 1 tiles left
 */
public class TargetInky extends TargetTileFinder {

    /**
     * Return the Movement minimizing the Euclidean distance to the target point
     * that is the symmetric of blinky's position relative to the point 2 tile in front of the player (Pivot point)
     * if the player is facing up the Pivot point became 2 tiles up and left
     *  _______________    _______________
     * |       B      |   |     X        |
     * |       |      |   |     |        |
     * |    P--o      |   |     o--      |
     * |       |      |   |     | |      |
     * |       X      |   |     B P      |
     * |______________|   |______________|
     * P : Player
     * o : Pivot point
     * B : Blinky
     * X target
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
                Point target = new Point(grid.getSizeX() - 1, grid.getSizeY() - 1);
                return getBestDirToTarget(grid, ghost, target);
            } else if (ghost.getState() == GhostState.CHASE) {
                Point target = new Point(grid.getPosition(player));
                switch (player.getCurrentDirection()) {
                    case UP:
                        target.y--;
                        /* Simulating 8 Bits overflow inside 16 Bits register */
                        target.x--;
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
