package modele;

import javafx.scene.input.KeyCode;
import modele.game.Grid;
import modele.game.Score;
import modele.game.entities.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import controller.input.Input;

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
import java.io.File;
import java.util.*;
import java.util.List;

public class Loader {

    private String mapFile;
    private String controlsFile;
    private String scoreFile;

    private static Loader instance;

    /**
     * Return an instance of the class
     * @return an instance of the class
     */
    public static Loader getInstance() {
        if (instance == null)
            instance = new Loader();
        return instance;
    }

    private Loader() {
        this.mapFile = "map.xml";
        this.controlsFile = "controls.xml";
        this.scoreFile = "scores.xml";
    }

    /**
     * Load the highscores from an XML File
     * @return a sorted list of highscores
     */
    public List<Score> loadHighscores() {
        List<Score> highscores = new ArrayList<>();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File fileXML = new File(scoreFile);
            Document xml;

            xml = builder.parse(fileXML);
            Element highscoreList = (Element)xml.getElementsByTagName("highscores").item(0);
            if (highscoreList == null)
                throw new Exception(scoreFile + " file corrupted (highscore node not found)");
            NodeList scores = highscoreList.getElementsByTagName("score");

            for (int i = 0; i < scores.getLength(); i++) {
                Element e = (Element) scores.item(i);
                highscores.add(new Score(Integer.parseInt(e.getTextContent()), e.getAttribute("name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        Collections.sort(highscores);
        Collections.reverse(highscores);
        return highscores;
    }

    /**
     * save the highscores to an XML File
     * @param highscores the list of highscores to be saved
     */
    public void saveHighscore(List<Score> highscores) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootNode = document.createElement("highscores");

            for (Score s : highscores) {
                Element input = document.createElement("score");
                input.setAttribute("name", s.getName());
                input.setTextContent(String.valueOf(s.getScore()));
                rootNode.appendChild(input);
            }

            document.appendChild(rootNode);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(scoreFile));
            transformer.transform(domSource, streamResult);

        } catch (TransformerException | ParserConfigurationException pce) {
            pce.printStackTrace();
        }
    }

    /**
     * Save the config to a XML File
     * @param inputsMap the input map to be saved
     */
    public void saveConfigs(Map<Input, KeyCode> inputsMap) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootNode = document.createElement("controls");

            for (Input i : inputsMap.keySet()) {
                Element input = document.createElement("input");
                input.setAttribute("id", String.valueOf(i));
                input.setTextContent(String.valueOf(inputsMap.get(i).getName()));
                rootNode.appendChild(input);
            }

            document.appendChild(rootNode);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(controlsFile));
            transformer.transform(domSource, streamResult);

        } catch (TransformerException | ParserConfigurationException pce) {
            pce.printStackTrace();
        }
    }

    /**
     * Load the inputs config from an XML File
     * @return a map linking every Input to its corresponding Key
     */
    public Map<Input, KeyCode> loadConfigs() {
        Map<Input, KeyCode> inputsMap = new HashMap<>();
        inputsMap.put(Input.UP_P1, KeyCode.UP);
        inputsMap.put(Input.DOWN_P1, KeyCode.DOWN);
        inputsMap.put(Input.LEFT_P1, KeyCode.LEFT);
        inputsMap.put(Input.RIGHT_P1, KeyCode.RIGHT);
        inputsMap.put(Input.UP_P2, KeyCode.Z);
        inputsMap.put(Input.DOWN_P2, KeyCode.S);
        inputsMap.put(Input.LEFT_P2, KeyCode.Q);
        inputsMap.put(Input.RIGHT_P2, KeyCode.D);
        inputsMap.put(Input.ENTER, KeyCode.ENTER);

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File fileXML = new File(controlsFile);
            Document xml;

            xml = builder.parse(fileXML);
            Element controls = (Element)xml.getElementsByTagName("controls").item(0);
            if (controls == null)
                throw new Exception(controlsFile + " file corrupted (controls node not found)");
            NodeList inputs = controls.getElementsByTagName("input");

            for (int i = 0; i < inputs.getLength(); i++) {
                Element e = (Element) inputs.item(i);
                String id = e.getAttribute("id");
                String keycode = e.getTextContent();
                switch (id) {
                    case "UP_P1":
                        inputsMap.replace(Input.UP_P1, KeyCode.getKeyCode(keycode));
                        break;
                    case "UP_P2":
                        inputsMap.replace(Input.UP_P2, KeyCode.getKeyCode(keycode));
                        break;
                    case "DOWN_P1":
                        inputsMap.replace(Input.DOWN_P1, KeyCode.getKeyCode(keycode));
                        break;
                    case "DOWN_P2":
                        inputsMap.replace(Input.DOWN_P2, KeyCode.getKeyCode(keycode));
                        break;
                    case "LEFT_P1":
                        inputsMap.replace(Input.LEFT_P1, KeyCode.getKeyCode(keycode));
                        break;
                    case "LEFT_P2":
                        inputsMap.replace(Input.LEFT_P2, KeyCode.getKeyCode(keycode));
                        break;
                    case "RIGHT_P1":
                        inputsMap.replace(Input.RIGHT_P1, KeyCode.getKeyCode(keycode));
                        break;
                    case "RIGHT_P2":
                        inputsMap.replace(Input.RIGHT_P2, KeyCode.getKeyCode(keycode));
                        break;
                    case "ENTER":
                        inputsMap.replace(Input.ENTER, KeyCode.getKeyCode(keycode));
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return inputsMap;
    }

    /**
     * Populate a grid's movement map from an XML File
     * Used by the game
     * @param movementMap the movement map to be populated
     * @param level the wanted level
     */
    public void loadMap(Map<Point, StaticEntity> movementMap, int level) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File fileXML = new File(mapFile);
            Document xml;

            xml = builder.parse(fileXML);
            Element maps = (Element)xml.getElementsByTagName("maps").item(0);
            if (maps == null)
                throw new Exception(mapFile + " file corrupted (maps node not found)");
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
                constructMap(movementMap, mapNode);
            } else {
                if (defaultMapNode == null)
                    throw new Exception(mapFile + " file corrupted (Default map not found)");
                constructMap(movementMap,defaultMapNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Load a grid associated to a level from a file
     * Used by the editor
     * @param grid the grid element to be populated
     * @param level the wanted level
     * @return an int array representing the loaded grid's information {startLevel, endLevel, isDefault}
     */
    public int[] loadMap(Grid grid, int level) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File fileXML = new File(mapFile);
            Document xml;

            xml = builder.parse(fileXML);
            Element maps = (Element)xml.getElementsByTagName("maps").item(0);
            if (maps == null)
                throw new Exception(mapFile + " file corrupted (maps node not found)");
            NodeList mapList = maps.getElementsByTagName("map");
            Element mapNode = null;
            Element defaultMapNode = null;
            int[] options = new int[3];
            int[] defaultOptions = new int[3];
            for (int i = 0; i < mapList.getLength(); i++) {
                Element e = (Element) mapList.item(i);
                int start = Integer.parseInt(e.getAttribute("start"));
                int end = Integer.parseInt(e.getAttribute("end"));
                boolean isDefault = e.hasAttribute("default");
                if (isDefault) {
                    defaultMapNode = e;
                    defaultOptions = new int[]{start, end, isDefault ? 1 : 0};
                }
                if (level <= end && level >= start) {
                    mapNode = e;
                    options = new int[]{start, end, isDefault ? 1 : 0};
                }
            }
            if (mapNode != null) {
                constructMap(grid.getMovementMap(), mapNode);
                grid.getDimension();
                return options;
            } else {
                if (defaultMapNode == null)
                    throw new Exception(mapFile + " file corrupted (Default map not found)");
                constructMap(grid.getMovementMap(), defaultMapNode);
                grid.getDimension();
                return defaultOptions;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    /**
     * Construct a Grid's movement map from an XML Node
     * @param movementMap the movement map to be populated
     * @param xmlElement the XML Node representing the grid
     * @throws Exception an Exception is thrown when the Node isn't normalized
     */
    private void constructMap(Map<Point, StaticEntity> movementMap, Element xmlElement) throws Exception {
        movementMap.clear();
        int sizeY = Integer.parseInt(xmlElement.getAttribute("sizeY")) + 2;
        String[] mapAsString = new String[sizeY - 2];
        NodeList rows = xmlElement.getElementsByTagName("row");
        for (int i = 0; i < rows.getLength(); i++) {
            Element e = (Element) rows.item(i);
            if (Integer.parseInt(e.getAttribute("id")) != i)
                throw new Exception("Map file corrupted (mismatch rowID");
            mapAsString[i] = e.getTextContent();
        }

        StaticEntity.EMPTY.setCount(0);
        StaticEntity.WALL.setCount(0);
        StaticEntity.GATE.setCount(0);
        StaticEntity.GUM.setCount(0);
        StaticEntity.GHOST_HOME.setCount(0);
        StaticEntity.GHOST_SPAWN.setCount(0);
        StaticEntity.PLAYER_SPAWN.setCount(0);
        StaticEntity.ITEM_SPAWN.setCount(0);
        StaticEntity.SUPER_GUM.setCount(0);
        StaticEntity.CHERRY.setCount(0);
        StaticEntity.STRAWBERRY.setCount(0);
        StaticEntity.ORANGE.setCount(0);
        StaticEntity.APPLE.setCount(0);
        StaticEntity.MELON.setCount(0);
        StaticEntity.GALAXIAN_BOSS.setCount(0);
        StaticEntity.BELL.setCount(0);
        StaticEntity.KEY.setCount(0);

        int lineIndex = 2;
        int rowIndex = 0;
        for (String line : mapAsString) {
            rowIndex = 0;
            for (char c : line.toCharArray()) {
                switch (c) {
                    case 'W':
                        movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.WALL);
                        StaticEntity.WALL.addCount(1);
                        break;
                    case '1':
                        movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.GUM);
                        StaticEntity.GUM.addCount(1);
                        break;
                    case '2':
                        movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.SUPER_GUM);
                        StaticEntity.SUPER_GUM.addCount(1);
                        break;
                    case 'A':
                        movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.GATE);
                        StaticEntity.GATE.addCount(1);
                        break;
                    case 'G':
                        movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.GHOST_HOME);
                        StaticEntity.GHOST_HOME.addCount(1);
                        break;
                    case 'S':
                        movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.GHOST_SPAWN);
                        StaticEntity.GHOST_SPAWN.addCount(1);
                        break;
                    case 'P':
                        movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.PLAYER_SPAWN);
                        StaticEntity.PLAYER_SPAWN.addCount(1);
                        break;
                    case 'I':
                        movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.ITEM_SPAWN);
                        StaticEntity.ITEM_SPAWN.addCount(1);
                        break;
                    case '0':
                    default:
                        movementMap.put(new Point(rowIndex, lineIndex), StaticEntity.EMPTY);
                        StaticEntity.EMPTY.addCount(1);
                        break;
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

    /**
     * Save a grid to an XML Document and edit the document to keep a coherent file
     * @param grid the grid to be saved
     * @param startLevel the starting level index
     * @param endLevel the ending level index
     * @param isDefault is the grid the default one
     */
    public void saveMap(Grid grid, int startLevel, int endLevel, boolean isDefault) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File fileXML = new File(mapFile);
            Document xml = builder.parse(fileXML);
            Element maps = (Element) xml.getElementsByTagName("maps").item(0);
            Element newMaps = xml.createElement("maps");
            if (maps == null)
                throw new Exception(mapFile + " file corrupted (maps node not found)");
            NodeList mapList = maps.getElementsByTagName("map");
            boolean hasBeenInserted = false;
            for (int i = 0; i < mapList.getLength(); i++) {
                Element e = (Element) mapList.item(i).cloneNode(true);
                int start = Integer.parseInt(e.getAttribute("start"));
                int end = Integer.parseInt(e.getAttribute("end"));
                if (isDefault && e.hasAttribute("default"))
                    e.removeAttribute("default");
                if (startLevel <= start && endLevel >= end) {// Old inside new
                    continue;
                } if (startLevel > start && endLevel < end) { // new inside old
                    Element e2 = (Element) e.cloneNode(true);
                    e.setAttribute("end", String.valueOf(startLevel - 1));
                    if (!hasBeenInserted) newMaps.appendChild(constructMapNode(xml, grid, startLevel, endLevel, isDefault));
                    e2.setAttribute("start", String.valueOf(endLevel + 1));
                    newMaps.appendChild(e2);
                    hasBeenInserted = true;
                    newMaps.appendChild(e);
                    continue;
                } else if (startLevel >= start && endLevel < end) {
                    if (!hasBeenInserted) newMaps.appendChild(constructMapNode(xml, grid, startLevel, endLevel, isDefault));
                    e.setAttribute("start", String.valueOf(endLevel + 1));
                    newMaps.appendChild(e);
                    hasBeenInserted = true;
                    newMaps.appendChild(e);
                    continue;
                } else if (startLevel > start && endLevel <= end) {
                    e.setAttribute("end", String.valueOf(startLevel - 1));
                    if (!hasBeenInserted) newMaps.appendChild(constructMapNode(xml, grid, startLevel, endLevel, isDefault));
                    hasBeenInserted = true;
                    newMaps.appendChild(e);
                    continue;
                }
                if (startLevel <= start && endLevel >= start) { // new overlap left
                    if (!hasBeenInserted) newMaps.appendChild(constructMapNode(xml, grid, startLevel, endLevel, isDefault));
                    e.setAttribute("start", String.valueOf(endLevel + 1));
                    hasBeenInserted = true;
                    newMaps.appendChild(e);
                    continue;
                }
                if (startLevel <= end && endLevel >= end) {  // new overlap right
                    if (!hasBeenInserted) newMaps.appendChild(constructMapNode(xml, grid, startLevel, endLevel, isDefault));
                    e.setAttribute("end", String.valueOf(startLevel - 1));
                    hasBeenInserted = true;
                    newMaps.appendChild(e);
                    continue;
                }
                newMaps.appendChild(e);
            }
            if (!hasBeenInserted)
                newMaps.appendChild(constructMapNode(xml, grid, startLevel, endLevel, isDefault));
            xml.removeChild(maps);
            xml.appendChild(newMaps);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(xml);
            StreamResult streamResult = new StreamResult(new File(mapFile));
            transformer.transform(domSource, streamResult);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Create an XML Node from a grid
     * @param document the XML document used to create the node
     * @param grid the grid to be converted
     * @param startLevel the starting level index
     * @param endLevel the ending level index
     * @param defaultMap is this grid the default one
     * @return an XML Node representing the grid
     */
    private Node constructMapNode(Document document, Grid grid, int startLevel, int endLevel, boolean defaultMap) {
        Point size = grid.getDimension();
        String[] mapAsString = convertToStringArray(grid);
        Element mapNode = document.createElement("map");
        mapNode.setAttribute("sizeX", String.valueOf(size.x));
        mapNode.setAttribute("sizeY", String.valueOf(size.y - 2));
        mapNode.setAttribute("start", String.valueOf(startLevel));
        mapNode.setAttribute("end", String.valueOf(endLevel));
        if (defaultMap)
            mapNode.setAttribute("default", String.valueOf(1));
        for (int i = 0; i < mapAsString.length; i++) {
            Element row = document.createElement("row");
            row.setAttribute("id", String.valueOf(i));
            row.setTextContent(mapAsString[i]);
            mapNode.appendChild(row);
        }
        return mapNode;
    }

    /**
     * Convert a Grid to an array of string
     * @param grid the grid to be converted
     * @return an array of String describing the grid
     */
    private String[] convertToStringArray(Grid grid) {
        String[] mapAsString = new String[grid.getSizeY() - 2];
        for (int row = 2; row < grid.getSizeY(); row++) {
            StringBuilder rowAsString = new StringBuilder();
            for (int col = 0; col < grid.getSizeX(); col++) {
                switch (grid.getStaticEntity(new Point(col, row))) {
                    case WALL:
                        rowAsString.append("W");
                        break;
                    case GUM:
                        rowAsString.append("1");
                        break;
                    case SUPER_GUM:
                        rowAsString.append("2");
                        break;
                    case GHOST_HOME:
                        rowAsString.append("G");
                        break;
                    case GHOST_SPAWN:
                        rowAsString.append("S");
                        break;
                    case PLAYER_SPAWN:
                        rowAsString.append("P");
                        break;
                    case ITEM_SPAWN:
                        rowAsString.append("I");
                        break;
                    case EMPTY:
                        rowAsString.append("0");
                        break;
                    case GATE:
                        rowAsString.append("A");
                }
            }
            mapAsString[row - 2] = String.valueOf(rowAsString);
        }
        return mapAsString;
    }


}
