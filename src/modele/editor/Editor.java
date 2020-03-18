package modele.editor;

import modele.Loader;
import modele.Utils;
import modele.game.Game;
import modele.game.Grid;
import modele.game.entities.StaticEntity;
import modele.game.enums.Movement;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Editor extends Observable implements Runnable {

    private static Editor instance;

    private ExecutorService worker;

    private boolean closeEditorRequested = false;
    private Grid grid;

    private StaticEntity selectedEntity;
    private Point levelInterval;
    private boolean isDefault;
    private boolean fillingMode;
    private boolean hasResized;

    private Stack<Point> changedPosition;

    private Editor() {
        grid = new Grid();
        levelInterval = new Point(1, 1);
        worker = Executors.newSingleThreadExecutor();
        changedPosition = new Stack<>();
    }

    /**
     * Pop the most recent change from the changes on the map
     * @return the most recent change on the map
     */
    public Point pollChangedPosition() {
        return changedPosition.pop();
    }

    /**
     * Return whether or not the Editor ar undrawn change on the map
     * @return has the editor undraw change on the map
     */
    public boolean hasChangedPositions() {
        return !changedPosition.isEmpty();
    }

    /**
     * Add a new action to be executed by a separated Thread
     * @param runnable the action to perform as soon as possible
     */
    public synchronized void runLater(Runnable runnable) {
        worker.submit(runnable);
    }

    /**
     * Set the selected StaticEntity
     * @param entity the new selected entity
     */
    public synchronized void setSelectedEntity(StaticEntity entity) {
        selectedEntity = entity;
    }

    /**
     * Get the current instance of the Editor
     * @return the current Instance of Editor
     */
    public static Editor getInstance() {
        return getInstance(false);
    }

    /**
     * Get the current instance of the Editor or a new one
     * @param newInstance does a new instance need to be created
     * @return the Instance of Editor
     */
    public static Editor getInstance(boolean newInstance) {
        if (instance == null || newInstance)
            instance = new Editor();
        return instance;
    }

    /**
     * Notify the views and controllers
     */
    private void update() {
        setChanged();
        notifyObservers();
    }

    /**
     * Resize the current map to a certain size
     * @param x the requested size along the x-axis
     * @param y the requested size along the y-axis
     */
    public synchronized void resize(int x, int y) {
        grid.resize(x, y);
        hasResized = true;
    }

    /**
     * Generate a random map of a certain size
     * @param x the requested size along the x-axis
     * @param y the requested size along the y-axis
     */
    public synchronized void generate(int x, int y) {
        grid.random(x, y);
        hasResized = true;
    }

    /**
     * Return the size of the map along the x-axis
     * @return the size of the map along the x-axis
     */
    public synchronized int getSizeX() {
        return grid.getSizeX();
    }

    /**
     * Return the size of the map along the y-axis
     * @return the size of the map along the y-axis
     */
    public synchronized int getSizeY() {
        return grid.getSizeY();
    }

    /**
     * Replace the StaticEntity at a certain pos by the selected one
     * @param pos the position to process
     */
    public synchronized void setTileType(Point pos) {
        StaticEntity e = grid.getStaticEntity(pos);
        if (e != null && selectedEntity != null && e != selectedEntity) {
            switch (selectedEntity) {
                case PLAYER_SPAWN:
                case GHOST_HOME:
                case GHOST_SPAWN:
                    Map<Point, StaticEntity> movementMap = grid.getMovementMap();
                    for (Point pt : movementMap.keySet()) {
                        if (movementMap.get(pt) == selectedEntity) {
                            changedPosition.push(pt);
                            break;
                        }
                    }
                default:
                    grid.setStaticEntity(pos, selectedEntity);
                    changedPosition.push(pos);
                    changedPosition.push(new Point(pos.x, Utils.wrap(pos.y - 1, 2, grid.getSizeY() - 1)));
                    changedPosition.push(new Point(pos.x, Utils.wrap(pos.y + 1, 2, grid.getSizeY() - 1)));
                    changedPosition.push(new Point(Utils.wrap(pos.x - 1, 0, grid.getSizeX() - 1), pos.y));
                    changedPosition.push(new Point(Utils.wrap(pos.x + 1, 0, grid.getSizeX() - 1), pos.y));
            }
        }
    }

    /**
     * Return the StaticEntity at a certain position
     * @param pos the position to check
     * @return the StaticEntity at pos
     */
    public synchronized StaticEntity getTileType(Point pos) {
        return grid.getStaticEntity(pos);
    }

    /**
     * Return the StaticEntity at a certain position moved by a dir
     * @param dir the dir to move
     * @param pos the position to test
     * @return the StaticEntity at pos + dir
     */
    public synchronized StaticEntity getTileType(Movement dir, Point pos) {
        return grid.getStaticEntity(dir, pos);
    }

    /**
     * Replace every StaticEntity of a type by the selected one
     * @param source the StaticEntity to replace
     */
    public synchronized void fill(StaticEntity source) {
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                if (grid.getStaticEntity(new Point(i, j)) == source)
                    setTileType(new Point(i, j));
            }
        }
    }

    /**
     * Save the current map to an XML File
     * @param startLevel the map's starting level
     * @param endLevel the map's ending level
     * @param isDefault is the map the default one
     */
    public synchronized void saveMap(int startLevel, int endLevel, boolean isDefault) {
        Loader.getInstance().saveMap(grid, startLevel, endLevel, isDefault);
    }

    /**
     * Load a map from an XML File for a specific level
     * @param level the level to load
     */
    public synchronized void loadMap(int level) {
         int[] options = Loader.getInstance().loadMap(grid, level);
         levelInterval.x = options[0];
         levelInterval.y = options[1];
         isDefault = options[2] == 1;
         hasResized = true;
    }

    /**
     * Return whether or not the Editor is in Filling mode
     * @return is the Editor in Filling mode
     */
    public boolean isFillingMode() {
        return fillingMode;
    }

    /**
     * Switch between filling and not filling
     */
    public void switchFillMode() {
        fillingMode = !fillingMode;
    }

    /**
     * Return the current grid's level interval
     * @return a Point representing the current grid's level interval
     */
    public synchronized Point getLevelInterval() {
        return levelInterval;
    }

    /**
     * Return whether or not the current grid is the default one
     * @return Is the current grid the default one
     */
    public synchronized boolean isDefault() {
        return isDefault;
    }

    @Override
    public void run() {
        while (!closeEditorRequested) {
            update();
            try {
                Thread.sleep(Game.FRAME_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Notify the Editor that it need to closed after the next update
     */
    public synchronized void requestClose() {
        closeEditorRequested = true;
    }

    /**
     * Return whether or not the map has been resized since the last frame
     * @return has the map been resized since the last frame
     */
    public synchronized boolean hasResized() {
        boolean tmp = hasResized;
        hasResized = false;
        return tmp;
    }
}
