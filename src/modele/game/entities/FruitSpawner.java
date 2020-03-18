package modele.game.entities;

import modele.game.Game;
import modele.game.Grid;

import java.awt.*;

public class FruitSpawner extends MoveableEntity {

    private boolean needToSpawnFruit = false;

    public FruitSpawner(Grid grid) {
        super(grid);
    }

    /**
     * Update the entity and spawn a fruit if necessary
     */
    @Override
    protected void update() {
        if (needToSpawnFruit) {
            spawnFruit();
            needToSpawnFruit = false;
        }
    }

    /**
     * Manage the entity's behavior
     */
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

    /**
     * Spawn a fruit depending on the current level
     */
    private void spawnFruit() {
        int level = Game.getInstance().getLevel();
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

    /**
     * Notify the Spawner that it need to spawn a fruit at it's next update
     */
    public synchronized void dispatchSpawnEvent() {
        needToSpawnFruit = true;
    }
}
