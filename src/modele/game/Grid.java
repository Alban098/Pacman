package modele.game;

import modele.Loader;
import modele.Utils;
import modele.game.entities.*;
import modele.game.enums.GhostName;
import modele.game.enums.GhostState;
import modele.game.enums.Movement;
import modele.game.entities.StaticEntity;
import modele.game.logic.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Grid {

    private int sizeX;
    private int sizeY;

    private Loader loader;

    private Map<MoveableEntity, Point> entities;
    private Map<StaticEntity, Integer> staticEntitiesCount;
    private Map<Point, StaticEntity> movementMap;
    private Map<MoveableEntity, Thread> threads;

    public Grid(Loader loader) {
        this.loader = loader;

        threads = new HashMap<>();
        entities = new HashMap<>();
        movementMap = new HashMap<>();
        staticEntitiesCount = new HashMap<>();

        entities.put(new EntityPlayer(this, 0), null);

        int i = 0;
        for (GhostName name : GhostName.values()) {
            entities.put(new EntityGhost(TargetTileFinder.getTargetFinder(name), this, 2000*i, name, false), null);
            i++;
        }

        init(1);
    }

    private void init(int level) {
        Point size = loader.loadMap(this, entities, movementMap, staticEntitiesCount, level);
        sizeX = size.x;
        sizeY = size.y;
        for (MoveableEntity e : entities.keySet()) {
            entities.replace(e, e.getSpawnPoint());
            e.reset();
        }
    }


    public void restart(int level) {
        init(level);
    }

    public void startEntities() {
        for (MoveableEntity e : entities.keySet()) {
            threads.put(e, new Thread(e));
            threads.get(e).start();
        }
    }

    public void stopGame() {
        for (MoveableEntity e : entities.keySet()) {
            e.kill();
        }
    }

    public void resetEntity(MoveableEntity entity) {
        entities.replace(entity, entity.getSpawnPoint());
        entity.reset();
    }


    public boolean canMove(Movement dir, MoveableEntity entity) {
        if (entity instanceof EntityPlayer)
            if (((EntityPlayer)entity).isDead())
                return false;
        Point pos = entities.get(entity);
        if (pos == null)
            return false;
        StaticEntity nextPos = getStaticEntity(dir, pos);
        if (nextPos == StaticEntity.GATE)
            if (entity instanceof EntityGhost && (((EntityGhost) entity).getState() == GhostState.STARTING || ((EntityGhost) entity).getState() == GhostState.EATEN || ((EntityGhost) entity).isPlayerControlled()))
                return true;
        return nextPos != StaticEntity.WALL && nextPos != StaticEntity.GATE;
    }

    public void move(Movement dir, MoveableEntity entity) {
        Point pos = entities.get(entity);
        if (pos != null) {
            switch (dir) {
                case UP:
                    if (canMove(dir, entity))
                        entities.replace(entity, new Point(pos.x, Utils.wrap(pos.y - 1, 2, sizeY - 1)));
                    break;
                case DOWN:
                    if (canMove(dir, entity))
                        entities.replace(entity, new Point(pos.x, Utils.wrap(pos.y + 1, 2, sizeY - 1)));
                    break;
                case LEFT:
                    if (canMove(dir, entity))
                        entities.replace(entity, new Point(Utils.wrap(pos.x - 1, 0, sizeX - 1), pos.y));
                    break;
                case RIGHT:
                    if (canMove(dir, entity))
                        entities.replace(entity, new Point(Utils.wrap(pos.x + 1, 0, sizeX - 1), pos.y));
                    break;
                case NONE:
                    break;
            }
        }
    }


    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public Set<MoveableEntity> getEntities() {
        return entities.keySet();
    }


    public Point getPosition(MoveableEntity entity) {
        return entities.get(entity);
    }

    public StaticEntity getStaticEntity(Point pos) {
        return movementMap.get(pos);
    }

    public StaticEntity getStaticEntity(Movement dir, Point pos) {
        switch (dir) {
            case UP:
                return movementMap.get(new Point(pos.x, Utils.wrap(pos.y - 1, 2, sizeY - 1)));
            case DOWN:
                return movementMap.get(new Point(pos.x, Utils.wrap(pos.y + 1, 2, sizeY - 1)));
            case LEFT:
                return movementMap.get(new Point(Utils.wrap(pos.x - 1, 0, sizeX - 1), pos.y));
            case RIGHT:
                return movementMap.get(new Point(Utils.wrap(pos.x + 1, 0, sizeX - 1), pos.y));
            case NONE:
                return movementMap.get(new Point(pos.x, pos.y));
        }
        return null;
    }

    public void setStaticEntity(Point pos, StaticEntity type) {
        int count = staticEntitiesCount.get(movementMap.get(pos));
        staticEntitiesCount.replace(movementMap.get(pos), count - 1);
        movementMap.replace(pos, type);
        count = staticEntitiesCount.get(type);
        staticEntitiesCount.replace(movementMap.get(pos), count + 1);
    }

    public boolean isType(Point pos, StaticEntity type) {
        StaticEntity e = movementMap.get(pos);
        return e == type;
    }

    public int getStaticEntityCount(StaticEntity type) {
        return staticEntitiesCount.get(type);
    }
}