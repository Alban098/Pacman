package modele;

import modele.entities.*;
import modele.enums.GhostName;
import modele.enums.GhostState;
import modele.enums.Movement;
import modele.entities.StaticEntity;
import modele.logic.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Grid {

    private int sizeX;
    private int sizeY;
    private final String map;

    private Map<MoveableEntity, Point> entities;
    private Map<StaticEntity, Integer> staticEntitiesCount;
    private Map<Point, StaticEntity> movementMap;
    private Map<MoveableEntity, Thread> threads;

    public Grid(String map, GhostName ... names) {
        this.map = map;

        threads = new HashMap<>();
        entities = new HashMap<>();
        movementMap = new HashMap<>();
        staticEntitiesCount = new HashMap<>();

        entities.put(new EntityPlayer(this), null);
        int i = 0;
        for (GhostName name : names) {
            entities.put(new EntityGhost(TargetTileFinder.getTargetFinder(name), this, 2000*i, name), null);
            i++;
        }

        init(1);
    }

    private void init(int level) {

        staticEntitiesCount.put(StaticEntity.SUPER_GUM, 0);
        staticEntitiesCount.put(StaticEntity.GUM, 0);
        staticEntitiesCount.put(StaticEntity.EMPTY, 0);
        staticEntitiesCount.put(StaticEntity.WALL, 0);
        staticEntitiesCount.put(StaticEntity.CHERRY, 0);
        staticEntitiesCount.put(StaticEntity.STRAWBERRY, 0);
        staticEntitiesCount.put(StaticEntity.ORANGE, 0);
        staticEntitiesCount.put(StaticEntity.APPLE, 0);
        staticEntitiesCount.put(StaticEntity.MELON, 0);
        staticEntitiesCount.put(StaticEntity.GALAXIAN_BOSS, 0);
        staticEntitiesCount.put(StaticEntity.BELL, 0);
        staticEntitiesCount.put(StaticEntity.KEY, 0);

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File fileXML = new File(map);
            Document xml;

            xml = builder.parse(fileXML);
            Element maps = (Element)xml.getElementsByTagName("maps").item(0);
            if (maps == null)
                throw new Exception("Map file corrupted (maps node not found)");
            NodeList mapList = maps.getElementsByTagName("map");
            Element mapNode = null;
            Element defaultMapNode = null;
            for (int i = 0; i < mapList.getLength(); i++) {
                Element e = (Element) mapList.item(i);
                int start = Integer.parseInt(e.getAttribute("start"));
                int end = Integer.parseInt(e.getAttribute("end"));
                boolean isDefault = e.hasAttribute("default");
                if (isDefault)
                    defaultMapNode = e;
                if (level <= end && level >= start)
                    mapNode = e;
            }
            if (mapNode != null) {
                constructMap(mapNode);
            } else {
                if (defaultMapNode == null)
                    throw new Exception("Map file corrupted (Default map not found)");
                constructMap(defaultMapNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        for (MoveableEntity e : entities.keySet()) {
            entities.replace(e, e.getSpawnPoint());
            e.reset();
        }
    }

    private void constructMap(Element xmlElement) throws Exception {
        sizeY = Integer.parseInt(xmlElement.getAttribute("sizeY")) + 2;
        sizeX = Integer.parseInt(xmlElement.getAttribute("sizeX"));
        String[] mapAsString = new String[sizeY - 2];
        NodeList rows = xmlElement.getElementsByTagName("row");
        for (int i = 0; i < rows.getLength(); i++) {
            Element e = (Element) rows.item(i);
            if (Integer.parseInt(e.getAttribute("id")) != i)
                throw new Exception("Map file corrupted (mismatch rowID");
            mapAsString[i] = e.getTextContent();
        }
        int lineIndex = 2;
        int rowIndex = 0;
        for (String line : mapAsString) {
            rowIndex = 0;
            for (char c : line.toCharArray()) {
                if (c == 'W') {
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.WALL);
                    int count = staticEntitiesCount.get(StaticEntity.WALL);
                    staticEntitiesCount.replace(StaticEntity.WALL, count + 1);
                } else if (c == '0') {
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.EMPTY);
                    int count = staticEntitiesCount.get(StaticEntity.EMPTY);
                    staticEntitiesCount.replace(StaticEntity.EMPTY, count + 1);
                } else if (c == '1') {
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.GUM);
                    int count = staticEntitiesCount.get(StaticEntity.GUM);
                    staticEntitiesCount.replace(StaticEntity.GUM, count + 1);
                } else if (c == '2') {
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.SUPER_GUM);
                    int count = staticEntitiesCount.get(StaticEntity.SUPER_GUM);
                    staticEntitiesCount.replace(StaticEntity.SUPER_GUM, count + 1);
                } else if (c == 'A') {
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.GATE);
                    int count = staticEntitiesCount.get(StaticEntity.SUPER_GUM);
                    staticEntitiesCount.replace(StaticEntity.SUPER_GUM, count + 1);
                } else if (c == 'G') {
                    for (MoveableEntity e : entities.keySet())
                        if (e instanceof EntityGhost)
                            ((EntityGhost) e).setStartingPoint(new Point(rowIndex, lineIndex));
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.EMPTY);
                    int count = staticEntitiesCount.get(StaticEntity.EMPTY);
                    staticEntitiesCount.replace(StaticEntity.EMPTY, count + 1);
                } else if (c == 'S') {
                    for (MoveableEntity e : entities.keySet())
                        if (e instanceof EntityGhost)
                            e.setSpawnPoint(new Point(rowIndex, lineIndex));
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.EMPTY);
                    int count = staticEntitiesCount.get(StaticEntity.EMPTY);
                    staticEntitiesCount.replace(StaticEntity.EMPTY, count + 1);
                } else if (c == 'P') {
                    for (MoveableEntity e : entities.keySet())
                        if (e instanceof EntityPlayer)
                            e.setSpawnPoint(new Point(rowIndex, lineIndex));
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.EMPTY);
                    int count = staticEntitiesCount.get(StaticEntity.EMPTY);
                    staticEntitiesCount.replace(StaticEntity.EMPTY, count + 1);
                } else if (c == 'I') {
                    FruitSpawner fs = new FruitSpawner(this);
                    fs.setSpawnPoint(new Point(rowIndex, lineIndex));
                    entities.put(fs, null);
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.EMPTY);
                    int count = staticEntitiesCount.get(StaticEntity.EMPTY);
                    staticEntitiesCount.replace(StaticEntity.EMPTY, count + 1);
                }
                rowIndex++;
            }
            lineIndex++;
        }
        for (int i = 0; i < rowIndex; i++) {
            movementMap.put(new Point(i, 0), StaticEntity.EMPTY);
            movementMap.put(new Point(i, 1), StaticEntity.EMPTY);
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
            if (entity instanceof EntityGhost && (((EntityGhost) entity).getState() == GhostState.STARTING || ((EntityGhost) entity).getState() == GhostState.EATEN))
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