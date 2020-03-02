package modele.game.entities;

import modele.game.Game;
import modele.game.Grid;

import java.awt.*;

public class FruitSpawner extends MoveableEntity {

    private Game game;
    private boolean needToSpawnFruit = false;

    public FruitSpawner(Grid grid) {
        super(grid);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    protected void update() {
        if (needToSpawnFruit) {
            spawnFruit(game.getLevel());
            needToSpawnFruit = false;
        }
    }

    @Override
    public void run() {
        while (running) {
            update();
            try {
                Thread.sleep(Game.PLAYER_UPDATE_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        running = true;
    }

    protected void spawnFruit(int level) {
        Point pos = grid.getPosition(this);
        if (level <= 2)
            grid.setStaticEntity(pos, StaticEntity.CHERRY);
        else if (level <= 4)
            grid.setStaticEntity(pos, StaticEntity.STRAWBERRY);
        else if (level <= 6)
            grid.setStaticEntity(pos, StaticEntity.ORANGE);
        else if (level <= 8)
            grid.setStaticEntity(pos, StaticEntity.APPLE);
        else if (level <= 10)
            grid.setStaticEntity(pos, StaticEntity.MELON);
        else if (level <= 12)
            grid.setStaticEntity(pos, StaticEntity.GALAXIAN_BOSS);
        else if (level <= 14)
            grid.setStaticEntity(pos, StaticEntity.BELL);
        else
            grid.setStaticEntity(pos, StaticEntity.KEY);
    }

    public void dispatchSpawnEvent() {
        needToSpawnFruit = true;
    }
}
