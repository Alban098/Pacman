package modele.game.entities.logic;

import modele.game.Grid;
import modele.game.entities.EntityGhost;
import modele.game.entities.EntityPlayer;
import modele.game.enums.GhostState;
import modele.game.enums.Movement;

import java.awt.*;

/**
 * <pre>
 * Pinky's target point is 2 tiles in front of the player
 * except when facing up, it became 2 tiles up and left
 * this is due to an overflow of the position vector in the original game
 *
 * The target point was stored in a 16Bits register as 2 8Bits signed integer
 * 0x[XX YY]
 * Same for each direction
 * UP =    0x[00 FF] -> {0, -1}
 * DOWN =  0x[00 01] -> {0, 1}
 * LEFT =  0x[01 00] -> {1, 0}
 * RIGHT = 0x[FF 00] -> {-1, 0}
 * The direction are flipped compared to our implementation because {0, 0} is at the top right on the original game
 *
 * When the original game calculate the target position
 * it will multiply the direction by 2 and add it to the player's position
 * but the position is treated as one 16Bits values rather than 2 8Bits one the resulting value are :
 * UP =    0x[01 FE] -> {1, -2}
 * DOWN =  0x[00 02] -> {0, 2}
 * LEFT =  0x[02 00] -> {2, 0}
 * RIGHT = 0x[FE 00] -> {-2, 0} (The 1FC is truncated to FC due to 16Bits overflow)
 *
 * When the game add that to the player's position it will result in the position 2 tile in front of the player expect for the UP direction :
 * Let's take a random position : {10, 10};
 *          1 1
 *      0x[0A 0A] -> {10, 10}
 *   +  0x[01 FE] -> {1, -2}
 *   ------------
 *   =  Ox[0C 08] -> {12, 8}
 *
 * this effectively represent 2 tiles up and 2 tiles left
 * </pre>
 */
public class TargetPinky extends TargetTileFinder{

    /**
     * Return the Movement minimizing the Euclidean distance to the target point that is the point 2 tile in front of the player
     * except if the player is facing up, in which case it return the point 2 tile up and left from the player
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
                Point target = new Point(0, 0);
                return getBestDirToTarget(grid, ghost, target);
            } else if (ghost.getState() == GhostState.CHASE) {
                Point target = new Point(grid.getPosition(player));
                switch (player.getCurrentDirection()) {
                    case UP:
                        target.y -= 2;
                        /* Simulating 8 Bits overflow inside 16 Bits register */
                        target.x -= 2;
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
