package modele.game;

import modele.Loader;
import modele.Utils;
import modele.editor.MapGenerator;
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

    private Map<MoveableEntity, Point> entities;
    private Map<Point, StaticEntity> movementMap;
    private Map<MoveableEntity, Thread> threads;

    private int sizeX;
    private int sizeY;

    public Grid() {

        threads = new HashMap<>();
        entities = new HashMap<>();
        movementMap = new HashMap<>();

        entities.put(new EntityPlayer(this), null);

        int i = 0;
        for (GhostName name : GhostName.values()) {
            entities.put(new EntityGhost(TargetTileFinder.getTargetFinder(name), this, 2000*i, name, false), null);
            i++;
        }

        init(1);
    }

    private void init(int level) {
        Loader.getInstance().loadMap(movementMap, level);
        for (Point p : movementMap.keySet()) {
            if (getStaticEntity(p) == StaticEntity.ITEM_SPAWN) {
                FruitSpawner fs = new FruitSpawner(this);
                fs.setSpawnPoint(new Point(p.x, p.y));
                entities.put(fs, fs.getSpawnPoint());
            } else if (getStaticEntity(p) == StaticEntity.GHOST_HOME) {
                for (MoveableEntity e : entities.keySet()) {
                    if (e instanceof EntityGhost)
                        ((EntityGhost) e).setStartingPoint(new Point(p.x, p.y));
                }
            } else if (getStaticEntity(p) == StaticEntity.GHOST_SPAWN) {
                for (MoveableEntity e : entities.keySet()) {
                    if (e instanceof EntityGhost)
                        e.setSpawnPoint(new Point(p.x, p.y));
                }
            } else if (getStaticEntity(p) == StaticEntity.PLAYER_SPAWN) {
                for (MoveableEntity e : entities.keySet()) {
                    if (e instanceof EntityPlayer)
                        e.setSpawnPoint(new Point(p.x, p.y));
                }
            }
        }

        for (MoveableEntity e : entities.keySet()) {
            entities.replace(e, e.getSpawnPoint());
            e.reset();
        }
        getDimension();
    }


    public synchronized void restart(int level) {
        init(level);
    }

    public synchronized void startEntities() {
        for (MoveableEntity e : entities.keySet()) {
            threads.put(e, new Thread(e));
            threads.get(e).start();
        }
    }

    public synchronized void stopGame() {
        for (MoveableEntity e : entities.keySet()) {
            e.kill();
        }
    }

    public synchronized void resetEntity(MoveableEntity entity) {
        entities.replace(entity, entity.getSpawnPoint());
        entity.reset();
    }


    public synchronized boolean canMove(Movement dir, MoveableEntity entity) {
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

    public synchronized void move(Movement dir, MoveableEntity entity) {
        Point dimension = getDimension();
        Point pos = entities.get(entity);
        if (pos != null) {
            switch (dir) {
                case UP:
                    if (canMove(dir, entity))
                        entities.replace(entity, new Point(pos.x, Utils.wrap(pos.y - 1, 2, dimension.y - 1)));
                    break;
                case DOWN:
                    if (canMove(dir, entity))
                        entities.replace(entity, new Point(pos.x, Utils.wrap(pos.y + 1, 2, dimension.y - 1)));
                    break;
                case LEFT:
                    if (canMove(dir, entity))
                        entities.replace(entity, new Point(Utils.wrap(pos.x - 1, 0, dimension.x - 1), pos.y));
                    break;
                case RIGHT:
                    if (canMove(dir, entity))
                        entities.replace(entity, new Point(Utils.wrap(pos.x + 1, 0, dimension.x - 1), pos.y));
                    break;
                case NONE:
                    break;
            }
        }
    }


    public Point getDimension() {
        Point size = new Point(0, 0);
        for (Point p : movementMap.keySet()) {
            if (p.x > size.x)
                size.x = p.x;
            if (p.y > size.y)
                size.y = p.y;
        }
        size.x++;
        size.y++;
        sizeX = size.x;
        sizeY = size.y;
        return size;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeX() {
        return sizeX;
    }

    public synchronized Set<MoveableEntity> getEntities() {
        return entities.keySet();
    }


    public synchronized Point getPosition(MoveableEntity entity) {
        return entities.get(entity);
    }

    public synchronized StaticEntity getStaticEntity(Point pos) {
        return movementMap.get(pos);
    }

    public synchronized StaticEntity getStaticEntity(Movement dir, Point pos) {
        Point dimension = getDimension();
        switch (dir) {
            case UP:
                return movementMap.get(new Point(pos.x, Utils.wrap(pos.y - 1, 2, dimension.y - 1)));
            case DOWN:
                return movementMap.get(new Point(pos.x, Utils.wrap(pos.y + 1, 2, dimension.y - 1)));
            case LEFT:
                return movementMap.get(new Point(Utils.wrap(pos.x - 1, 0, dimension.x - 1), pos.y));
            case RIGHT:
                return movementMap.get(new Point(Utils.wrap(pos.x + 1, 0, dimension.x - 1), pos.y));
            case NONE:
                return movementMap.get(new Point(pos.x, pos.y));
        }
        return null;
    }

    public synchronized void setStaticEntity(Point pos, StaticEntity type) {
        switch (type) {
            case PLAYER_SPAWN:
            case GHOST_HOME:
            case GHOST_SPAWN:
                for (Point p : movementMap.keySet()) {
                    if (movementMap.get(p) == type) {
                        movementMap.replace(p, StaticEntity.EMPTY);
                        StaticEntity.EMPTY.addCount(1);
                        break;
                    }
                }
            default:
                movementMap.get(pos).addCount(-1);
                type.addCount(1);
                movementMap.replace(pos, type);
        }
    }

    public synchronized boolean isType(Point pos, StaticEntity type) {
        StaticEntity e = movementMap.get(pos);
        return e == type;
    }

    public int getStaticEntityCount(StaticEntity type) {
        return type.getCount();
    }

    public synchronized void resize(int x, int y) {
        if (x <= 0 || y <= 0)
            return;
        Point dimension = getDimension();
        System.out.println(dimension + " / " + x + " " + y);
        Map<Point, StaticEntity> newMap = new HashMap<>();
        for (int i = 0; i < Math.min(x, dimension.x); i++) {
            for (int j = 0; j < Math.min(y, dimension.y); j++) {
                newMap.put(new Point(i, j), getStaticEntity(new Point(i, j)));
            }
        }
        if (x > dimension.x) {
            for (int i = dimension.x; i < x; i++)
                for (int j = 0; j < dimension.y; j++) {
                    newMap.put(new Point(i, j), StaticEntity.EMPTY);
                }
        }

        if (y > dimension.y) {
            for (int i = dimension.y; i < y; i++)
                for (int j = 0; j < dimension.x; j++) {
                    newMap.put(new Point(j, i), StaticEntity.EMPTY);
                }
        }
        movementMap = newMap;
        getDimension();
    }

    public synchronized Map<Point, StaticEntity> getMovementMap() {
        return movementMap;
    }

    public synchronized void random(int x, int y) {
        MapGenerator generator = new MapGenerator();
        generator.generateMap(x, y, movementMap);
        getDimension();
    }
}