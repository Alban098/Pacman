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

    public Point pollChangedPosition() {
        return changedPosition.pop();
    }

    public boolean hasChangedPositions() {
        return !changedPosition.isEmpty();
    }

    public synchronized void runLater(Runnable runnable) {
        worker.submit(runnable);
    }

    public synchronized void setSelectedEntity(StaticEntity entity) {
        selectedEntity = entity;
    }

    public static Editor getInstance() {
        return getInstance(false);
    }

    public static Editor getInstance(boolean newInstance) {
        if (instance == null || newInstance)
            instance = new Editor();
        return instance;
    }

    private void update() {
        setChanged();
        notifyObservers();
    }

    public synchronized void resize(int x, int y) {
        grid.resize(x, y);
        hasResized = true;
    }

    public synchronized void generate(int x, int y) {
        grid.random(x, y);
        hasResized = true;
    }

    public synchronized int getSizeX() {
        return grid.getSizeX();
    }

    public synchronized int getSizeY() {
        return grid.getSizeY();
    }


    public void setTileType(Point pos) {
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

    public synchronized StaticEntity getTileType(Point pos) {
        return grid.getStaticEntity(pos);
    }

    public synchronized StaticEntity getTileType(Movement dir, Point pos) {
        return grid.getStaticEntity(dir, pos);
    }

    public void fill(StaticEntity source) {
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                if (grid.getStaticEntity(new Point(i, j)) == source)
                    setTileType(new Point(i, j));
            }
        }
    }

    public void saveMap(int startLevel, int endLevel, boolean isDefault) {
        Loader.getInstance().saveMap(grid, startLevel, endLevel, isDefault);
    }

    public void loadMap(int level) {
         int[] options = Loader.getInstance().loadMap(grid, level);
         levelInterval.x = options[0];
         levelInterval.y = options[1];
         isDefault = options[2] == 1;
         hasResized = true;
    }

    public boolean isFillingMode() {
        return fillingMode;
    }

    public void switchFillMode() {
        fillingMode = !fillingMode;
    }

    public synchronized Point getLevelInterval() {
        return levelInterval;
    }

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

    public synchronized void requestClose() {
        closeEditorRequested = true;
    }

    public synchronized boolean hasResized() {
        boolean tmp = hasResized;
        hasResized = false;
        return tmp;
    }
}
