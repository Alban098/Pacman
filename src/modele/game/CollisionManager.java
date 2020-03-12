package modele.game;

import modele.game.entities.*;
import modele.game.enums.GhostState;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CollisionManager {

    private Grid grid;
    private Game game;

    private int totalGum;

    public CollisionManager(Grid grid, Game game) {
        this.grid = grid;
        this.game = game;

        totalGum = grid.getStaticEntityCount(StaticEntity.GUM);
    }

    public void testCollision() {
        EntityPlayer player = game.getPlayer();
        Point pos = grid.getPosition(player);
        game.levelScore += grid.getStaticEntity(pos).getScore();
        game.totalScore += grid.getStaticEntity(pos).getScore();
        for (MoveableEntity e : grid.getEntities()) {
            if (e instanceof EntityGhost) {
                if (grid.getPosition(e).equals(grid.getPosition(player))) {
                    if (((EntityGhost) e).getState() == GhostState.FRIGHTENED) {
                        ((EntityGhost) e).setState(GhostState.EATEN);
                        game.levelScore += 100*Math.pow(2, player.getEatenGhostMultiplier());
                        game.totalScore += 100*Math.pow(2, player.getEatenGhostMultiplier());
                        game.dynamicScore = (int) (100*Math.pow(2, player.getEatenGhostMultiplier()));
                        player.incrementEatenGhostMultiplier();
                        player.setHasEatenGhost();
                    } else if (((EntityGhost) e).getState() != GhostState.EATEN) {
                        player.setDead(true);
                        player.setLives(player.getLives() - 1);
                        return;
                    }
                }
            }
        }
        if (grid.isType(pos, StaticEntity.GUM)) {
            grid.setStaticEntity(pos, StaticEntity.EMPTY);
            player.setHasEatenGum();
        } else if (grid.isType(pos, StaticEntity.SUPER_GUM)) {
            player.setHasEatenGum();
            grid.setStaticEntity(pos, StaticEntity.EMPTY);
            player.resetEatenGhostMultiplier();
            for (MoveableEntity e : grid.getEntities()) {
                if (e instanceof EntityGhost && ((EntityGhost) e).getState() != GhostState.EATEN && ((EntityGhost) e).getState() != GhostState.STILL)
                    ((EntityGhost) e).setState(GhostState.FRIGHTENED);
            }
        } else if (grid.getStaticEntity(pos) != StaticEntity.EMPTY && grid.getStaticEntity(pos) != StaticEntity.WALL && grid.getStaticEntity(pos) != StaticEntity.ITEM_SPAWN && grid.getStaticEntity(pos) != StaticEntity.GHOST_SPAWN && grid.getStaticEntity(pos) != StaticEntity.GHOST_HOME && grid.getStaticEntity(pos) != StaticEntity.PLAYER_SPAWN) {
            game.dynamicScore = -grid.getStaticEntity(pos).getScore();
            grid.setStaticEntity(pos, StaticEntity.EMPTY);
            player.setHasEatenFruit();
        }
        if (grid.getStaticEntityCount(StaticEntity.GUM) == (int)(totalGum * 0.6)) {
            for (MoveableEntity e : grid.getEntities())
                if (e instanceof FruitSpawner) {
                    ((FruitSpawner) e).dispatchSpawnEvent();
                }
        }
        if (game.lastLevelScore < Game.EXTRA_LIFE_THRESHOLD && game.levelScore >= Game.EXTRA_LIFE_THRESHOLD) {
            player.setLives(player.getLives() + 1);
            player.setHasExtraLife();
        }
        game.lastLevelScore = game.levelScore;
    }


    public void resetTotalGum() {
        totalGum = grid.getStaticEntityCount(StaticEntity.GUM);
    }
}
