package modele;

import modele.entities.*;
import modele.enums.GhostState;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CollisionManager {

    private static Map<StaticEntity, Integer> scoreMap;

    private Grid grid;
    private Game game;

    private int totalGum;

    public CollisionManager(Grid grid, Game game) {
        this.grid = grid;
        this.game = game;

        totalGum = grid.getStaticEntityCount(StaticEntity.GUM);

        scoreMap = new HashMap<>();
        scoreMap.put(StaticEntity.WALL, 0);
        scoreMap.put(StaticEntity.EMPTY, 0);
        scoreMap.put(StaticEntity.GUM, 10);
        scoreMap.put(StaticEntity.SUPER_GUM, 50);
        scoreMap.put(StaticEntity.CHERRY, 100);
        scoreMap.put(StaticEntity.STRAWBERRY, 300);
        scoreMap.put(StaticEntity.ORANGE, 500);
        scoreMap.put(StaticEntity.APPLE, 700);
        scoreMap.put(StaticEntity.MELON, 1000);
        scoreMap.put(StaticEntity.GALAXIAN_BOSS, 2000);
        scoreMap.put(StaticEntity.BELL, 3000);
        scoreMap.put(StaticEntity.KEY, 5000);

        for (MoveableEntity e : grid.getEntities())
            if (e instanceof FruitSpawner)
                ((FruitSpawner) e).setGame(game);
    }

    public void testCollision() {
        EntityPlayer player = game.getPlayer();
        Point pos = grid.getPosition(player);
        game.levelScore += scoreMap.get(grid.getStaticEntity(pos));
        game.totalScore += scoreMap.get(grid.getStaticEntity(pos));
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
                        game.getPlayer().setLives(game.getPlayer().getLives() - 1);
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
        } else if (grid.getStaticEntity(pos) != StaticEntity.EMPTY && grid.getStaticEntity(pos) != StaticEntity.WALL) {
            game.totalScore += scoreMap.get(grid.getStaticEntity(pos));
            game.levelScore += scoreMap.get(grid.getStaticEntity(pos));
            game.dynamicScore = -scoreMap.get(grid.getStaticEntity(pos));
            grid.setStaticEntity(pos, StaticEntity.EMPTY);
            player.setHasEatenFruit();
        }
        if (grid.getStaticEntityCount(StaticEntity.GUM) == (int)(totalGum * 0.6)) {
            for (MoveableEntity e : grid.getEntities())
                if (e instanceof FruitSpawner)
                    ((FruitSpawner) e).dispatchSpawnEvent();
        }
        if (game.lastLevelScore < Game.EXTRA_LIFE_THRESHOLD && game.levelScore >= Game.EXTRA_LIFE_THRESHOLD) {
            game.getPlayer().setLives(game.getPlayer().getLives() + 1);
            player.setHasExtraLife();
        }
        game.lastLevelScore = game.levelScore;
    }


    public void resetTotalGum() {
        totalGum = grid.getStaticEntityCount(StaticEntity.GUM);
    }
}
