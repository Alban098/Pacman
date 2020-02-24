package modele;

import modele.entities.*;
import modele.logic.TargetBlinky;
import modele.logic.TargetClyde;
import modele.logic.TargetInky;
import modele.logic.TargetPinky;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Grid {

    private static Map<StaticEntity, Integer> scoreMap;

    private int sizeX;
    private int sizeY;
    private final String map;

    private int totalScore = 0;
    private int level = 0;
    private int levelScore = 0;
    private int lives = 3;
    private int dynamicScore = 0;

    private Point ghostHome;
    private Point ghostSpawn;
    private Point playerStartPos;
    private Point itemSpawn;

    private final EntityPlayer player;
    private final EntityGhost blinky;
    private final EntityGhost pinky;
    private final EntityGhost inky;
    private final EntityGhost clyde;

    private Map<MoveableEntity, Point> entities;
    private Map<Point, StaticEntity> movementMap;
    private Map<MoveableEntity, Thread> threads;

    private int nbGum = 0;
    private int totalGum = 0;

    public Grid(String map) {
        this.map = map;

        threads = new HashMap<>();

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

        player = new EntityPlayer(this);
        blinky = new EntityGhost(new TargetBlinky(), this, 0);
        inky = new EntityGhost(new TargetInky(blinky), this, 2500);
        pinky = new EntityGhost(new TargetPinky(), this, 5000);
        clyde = new EntityGhost(new TargetClyde(), this, 7500);

        init();
    }

    public EntityGhost getGhost(GhostName name) {
        switch (name) {
            case BLINKY:
                return blinky;
            case INKY:
                return inky;
            case PINKY:
                return pinky;
            case CLYDE:
                return clyde;
        }
        return null;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public boolean canMove(Movement dir, MoveableEntity entity) {
        Point pos = entities.get(entity);
        if (pos == null)
            return false;
        StaticEntity nextPos = getStaticEntity(dir, pos);
        return nextPos != StaticEntity.WALL;
    }

    public void moveToHome(EntityGhost ghost) {
        entities.replace(ghost, ghostHome);
    }

    public void move(Movement dir, MoveableEntity entity) {
        Point pos = entities.get(entity);
        if (pos != null) {
            switch (dir) {
                case UP:
                    if (canMove(dir, entity))
                        entities.replace(entity, new Point(pos.x, Utils.wrap(pos.y - 1, 0, sizeY - 1)));
                    break;
                case DOWN:
                    if (canMove(dir, entity))
                        entities.replace(entity, new Point(pos.x, Utils.wrap(pos.y + 1, 0, sizeY - 1)));
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

    public boolean isGameFinished() {
        return nbGum == 0;
    }

    public boolean testCollision() {
        Point pos = entities.get(player);
        if (levelScore < 1000 && levelScore + scoreMap.get(movementMap.get(pos)) >= 1000)
            lives++;
        levelScore += scoreMap.get(movementMap.get(pos));
        totalScore += scoreMap.get(movementMap.get(pos));
        for (MoveableEntity e : entities.keySet()) {
            if (e instanceof EntityGhost) {
                if (getPosition(e).equals(getPosition(player))) {
                    if (((EntityGhost) e).getState() == GhostState.FRIGHTENED) {
                        ((EntityGhost) e).setState(GhostState.EATEN);
                        levelScore += 100*Math.pow(2, player.getEatenGhostMultiplier());
                        totalScore += 100*Math.pow(2, player.getEatenGhostMultiplier());
                        dynamicScore = (int) (100*Math.pow(2, player.getEatenGhostMultiplier()));
                        player.incrementEatenGhostMultiplier();
                    } else if (((EntityGhost) e).getState() != GhostState.EATEN){
                        return true;
                    }
                }
            }
        }
        if (isType(pos, StaticEntity.GUM)) {
            nbGum--;
            movementMap.replace(pos, StaticEntity.EMPTY);
        } else if (isType(pos, StaticEntity.SUPER_GUM)) {
            nbGum--;
            movementMap.replace(pos, StaticEntity.EMPTY);
            player.resetEatenGhostMultiplier();
            for (MoveableEntity e : entities.keySet()) {
                if (e instanceof EntityGhost && ((EntityGhost) e).getState() != GhostState.EATEN && ((EntityGhost) e).getState() != GhostState.STILL)
                    ((EntityGhost) e).setState(GhostState.FRIGHTENED);
            }
        } else if (movementMap.get(pos) != StaticEntity.EMPTY && movementMap.get(pos) != StaticEntity.WALL) {
            totalScore += scoreMap.get(movementMap.get(pos));
            levelScore += scoreMap.get(movementMap.get(pos));
            dynamicScore = -scoreMap.get(movementMap.get(pos));
            movementMap.replace(pos, StaticEntity.EMPTY);
        }
        if (nbGum == (int)(.9f * totalGum)) {
            spawnFruit();
        }
        return false;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getLevel() {
        return level;
    }

    public StaticEntity getStaticEntity(Movement dir, Point pos) {
        switch (dir) {
            case UP:
                return movementMap.get(new Point(pos.x, Utils.wrap(pos.y - 1, 0, sizeY - 1)));
            case DOWN:
                return movementMap.get(new Point(pos.x, Utils.wrap(pos.y + 1, 0, sizeY - 1)));
            case LEFT:
                return movementMap.get(new Point(Utils.wrap(pos.x - 1, 0, sizeX - 1), pos.y));
            case RIGHT:
                return movementMap.get(new Point(Utils.wrap(pos.x + 1, 0, sizeX - 1), pos.y));
            case NONE:
                return movementMap.get(new Point(pos.x, pos.y));
        }
        return null;
    }

    private void spawnFruit() {
        if (level <= 2)
            movementMap.put(itemSpawn, StaticEntity.CHERRY);
        else if (level <= 4)
            movementMap.put(itemSpawn, StaticEntity.STRAWBERRY);
        else if (level <= 6)
            movementMap.put(itemSpawn, StaticEntity.ORANGE);
        else if (level <= 8)
            movementMap.put(itemSpawn, StaticEntity.APPLE);
        else if (level <= 10)
            movementMap.put(itemSpawn, StaticEntity.MELON);
        else if (level <= 12)
            movementMap.put(itemSpawn, StaticEntity.GALAXIAN_BOSS);
        else if (level <= 14)
            movementMap.put(itemSpawn, StaticEntity.BELL);
        else
            movementMap.put(itemSpawn, StaticEntity.KEY);
    }

    public Point getPosition(MoveableEntity entity) {
        return entities.get(entity);
    }

    public int getTotalScore() {
        return totalScore;
    }

    public boolean isType(Point pos, StaticEntity type) {
        StaticEntity e = movementMap.get(pos);
        return e == type;
    }

    public StaticEntity getTileType(Point pos) {
        return movementMap.get(pos);
    }

    public Point getGhostHome() {
        return ghostHome;
    }

    private void init() {
        level++;
        levelScore = 0;
        nbGum = 0;

        entities = new HashMap<>();
        movementMap = new HashMap<>();

        ghostHome = new Point(1, 1);
        ghostSpawn = new Point(1, 1);
        itemSpawn = new Point(1, 1);
        playerStartPos = new Point(1, 1);

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

        entities.put(player, playerStartPos);
        entities.put(blinky, ghostSpawn);
        entities.put(clyde, ghostSpawn);
        entities.put(pinky, ghostSpawn);
        entities.put(inky, ghostSpawn);

        for (MoveableEntity e : entities.keySet()) {
            e.reset();
        }
    }

    private void constructMap(Element xmlElement) throws Exception {
        sizeY = Integer.parseInt(xmlElement.getAttribute("sizeY")) + 3;
        sizeX = Integer.parseInt(xmlElement.getAttribute("sizeX"));
        String[] mapAsString = new String[sizeY - 3];
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
                if (c == 'W')
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.WALL);
                else if (c == '0')
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.EMPTY);
                else if (c == '1') {
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.GUM);
                    nbGum++;
                } else if (c == '2') {
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.SUPER_GUM);
                    nbGum++;
                } else if (c == 'G') {
                    ghostHome = new Point(rowIndex, lineIndex);
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.EMPTY);
                } else if (c == 'S') {
                    ghostSpawn = new Point(rowIndex, lineIndex);
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.EMPTY);
                } else if (c == 'P') {
                    playerStartPos = new Point(rowIndex, lineIndex);
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.EMPTY);
                } else if (c == 'I') {
                    itemSpawn = new Point(rowIndex, lineIndex);
                    movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.EMPTY);
                }
                rowIndex++;
            }
            lineIndex++;
        }
        lineIndex++;
        for (int i = 0; i < rowIndex; i++) {
            movementMap.put(new Point(i, 0), StaticEntity.EMPTY);
            movementMap.put(new Point(i, 1), StaticEntity.EMPTY);
            movementMap.put(new Point(i, lineIndex-1), StaticEntity.EMPTY);
        }
        totalGum = nbGum;
    }

    public void startEntities() {
        threads.put(player, new Thread(player));
        threads.put(blinky, new Thread(blinky));
        threads.put(pinky, new Thread(pinky));
        threads.put(inky, new Thread(inky));
        threads.put(clyde, new Thread(clyde));
        for (MoveableEntity e : threads.keySet()) {
            threads.get(e).start();
        }
    }

    public void nextLevel() {
        init();
    }

    public void stopGame() {
        for (MoveableEntity e : entities.keySet()) {
            e.kill();
            e.reset();
        }
    }

    public void resetPlayer() {
        entities.replace(player, playerStartPos);
        player.reset();
    }

    public void resetGhost() {
        for (MoveableEntity e : entities.keySet()) {
            if (e instanceof EntityGhost) {
                entities.replace(e, ghostSpawn);
                e.reset();
            }
        }
    }

    public int getDynamicScore() {
        int tmp = dynamicScore;
        dynamicScore = 0;
        return tmp;
    }
}
