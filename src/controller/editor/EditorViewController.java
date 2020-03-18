 package controller.editor;

import controller.input.InputController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import modele.editor.Editor;
import modele.game.Game;
import modele.game.entities.*;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import modele.game.enums.*;

public class EditorViewController extends Application implements Observer {

    private static final byte MASK_WALL_RIGHT =  0b1000;
    private static final byte MASK_WALL_UP =     0b0100;
    private static final byte MASK_WALL_LEFT =   0b0010;
    private static final byte MASK_WALL_DOWN =   0b0001;
    private static final byte MASK_GATE_UP =   0b010000;
    private static final byte MASK_GATE_LEFT = 0b100000;
    private static final float SCALE = 1;

    private Map<StaticEntity, Image> backgroundTileMap;
    private Map<Byte, Image> wallTileMap;

    private Canvas background;
    private InputController inputController;
    private Runnable renderer;


    @Override
    public void start(Stage primaryStage) throws IOException {
        Editor editor = Editor.getInstance(true);
        backgroundTileMap = new HashMap<>();
        wallTileMap = new HashMap<>();

        background = new Canvas(16*editor.getSizeX(), 16*(editor.getSizeY() - 2));

        BorderPane bp = new BorderPane();

        bp.setLeft(FXMLLoader.load(getClass().getResource("/controller/editor/mapEditor.fxml")));
        bp.setRight(background);

        StackPane root = new StackPane();
        root.getChildren().add(bp);
        Scene scene = new Scene(root);
        primaryStage.setTitle("Map Editor 2.0");
        primaryStage.setOnCloseRequest(we -> {
            editor.requestClose();
            Game.getInstance().runLater(() -> Game.getInstance().setGameState(GameState.MENU_SCREEN));
            inputController.setEditorLaunched(false);
        });
        primaryStage.getIcons().add(new Image("resources/sprites/items/cherry.png"));
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setResizable(false);
        primaryStage.show();


        loadSprites();
        drawBackground();
        renderer = () -> {
            Platform.runLater(() ->  {
                primaryStage.sizeToScene();
                if (Editor.getInstance().hasResized())
                    drawBackground();
                else
                    drawPartialBackground();
            });
        };

        int editorWidth = (int) bp.getLeft().getBoundsInParent().getWidth();
        root.setOnMousePressed(event ->  {
            Point pos = new Point((int) (event.getSceneX() - editorWidth) / 16, (int) (event.getSceneY()) / 16 + 2);
            if (!Editor.getInstance().isFillingMode()) {
                editor.runLater(() -> editor.setTileType(pos));
            } else {
                editor.runLater(() -> editor.fill(editor.getTileType(pos)));
            }
        });
        root.setOnMouseDragged(event -> {
            if (!Editor.getInstance().isFillingMode()) {
                Point pos = new Point((int) (event.getSceneX() - editorWidth) / 16, (int) (event.getSceneY()) / 16 + 2);
                editor.runLater(() -> editor.setTileType(pos));
            }
        });

        editor.addObserver(this);
        new Thread(editor).start();
        root.requestFocus();
        drawBackground();
    }

    /**
     * Set the Input Controller
     * @param inputController the new Input Controller
     */
    public synchronized void setInputController(InputController inputController) {
        this.inputController = inputController;
    }

    /**
     * Load all the sprites and images in memory
     */
    private void loadSprites() {
        backgroundTileMap.put(StaticEntity.EMPTY, new Image("resources/sprites/map/empty.png"));
        backgroundTileMap.put(StaticEntity.GUM, new Image("resources/sprites/map/gum.png"));
        backgroundTileMap.put(StaticEntity.SUPER_GUM, new Image("resources/sprites/map/super_gum.png"));
        backgroundTileMap.put(StaticEntity.ITEM_SPAWN, new Image("resources/sprites/items/cherry.png"));
        backgroundTileMap.put(StaticEntity.PLAYER_SPAWN, new Image("resources/sprites/player/pacman_right_1.png"));
        backgroundTileMap.put(StaticEntity.GHOST_SPAWN, new Image("resources/sprites/ghost/blinky_right_1.png"));
        backgroundTileMap.put(StaticEntity.GHOST_HOME, new Image("resources/sprites/ghost/frightened_0.png"));

        wallTileMap.put(MASK_GATE_UP, new Image("resources/sprites/map/gate_v.png"));
        wallTileMap.put(MASK_GATE_LEFT, new Image("resources/sprites/map/gate_h.png"));
        wallTileMap.put((byte) 0b0000, new Image("resources/sprites/map/wall_none.png"));
        wallTileMap.put(MASK_WALL_DOWN, new Image("resources/sprites/map/wall_down.png"));
        wallTileMap.put(MASK_WALL_UP, new Image("resources/sprites/map/wall_up.png"));
        wallTileMap.put(MASK_WALL_LEFT, new Image("resources/sprites/map/wall_left.png"));
        wallTileMap.put(MASK_WALL_RIGHT, new Image("resources/sprites/map/wall_right.png"));
        wallTileMap.put((byte) (MASK_WALL_UP | MASK_WALL_RIGHT), new Image("resources/sprites/map/wall_up_right.png"));
        wallTileMap.put((byte) (MASK_WALL_RIGHT | MASK_WALL_DOWN), new Image("resources/sprites/map/wall_right_down.png"));
        wallTileMap.put((byte) (MASK_WALL_DOWN | MASK_WALL_LEFT), new Image("resources/sprites/map/wall_down_left.png"));
        wallTileMap.put((byte) (MASK_WALL_LEFT | MASK_WALL_UP), new Image("resources/sprites/map/wall_left_up.png"));
        wallTileMap.put((byte) (MASK_WALL_LEFT | MASK_WALL_RIGHT), new Image("resources/sprites/map/wall_left_right.png"));
        wallTileMap.put((byte) (MASK_WALL_UP | MASK_WALL_DOWN), new Image("resources/sprites/map/wall_up_down.png"));
        wallTileMap.put((byte) (MASK_WALL_UP | MASK_WALL_RIGHT | MASK_WALL_DOWN), new Image("resources/sprites/map/wall_up_right_down.png"));
        wallTileMap.put((byte) (MASK_WALL_RIGHT | MASK_WALL_DOWN | MASK_WALL_LEFT), new Image("resources/sprites/map/wall_right_down_left.png"));
        wallTileMap.put((byte) (MASK_WALL_DOWN | MASK_WALL_LEFT | MASK_WALL_UP), new Image("resources/sprites/map/wall_down_left_up.png"));
        wallTileMap.put((byte) (MASK_WALL_LEFT | MASK_WALL_UP | MASK_WALL_RIGHT), new Image("resources/sprites/map/wall_left_up_right.png"));
        wallTileMap.put((byte) (MASK_WALL_LEFT | MASK_WALL_UP | MASK_WALL_RIGHT | MASK_WALL_DOWN), new Image("resources/sprites/map/wall_all.png"));
    }

    /**
     * Resize the background canvas and draw the difference from the last frame
     */
    private void drawPartialBackground() {
        Editor editor = Editor.getInstance();
        background.setWidth(16 * editor.getSizeX());
        background.setHeight(16 * (editor.getSizeY() - 2));
        background.setScaleX(SCALE);
        background.setScaleY(SCALE);
        final GraphicsContext gc = background.getGraphicsContext2D();
        while (editor.hasChangedPositions()) {
            Point pos = editor.pollChangedPosition();
            if (pos == null) {
                drawBackground();
                break;
            }
            gc.setFill(Color.BLACK);
            gc.fillRect(16 * pos.x, 16 * (pos.y - 2), 16, 16);
            if (editor.getTileType(pos) == StaticEntity.WALL) {
                gc.drawImage(wallTileMap.get(getWallMask(pos)), 16 * pos.x, 16 * (pos.y - 2));
            } else if (editor.getTileType(pos) == StaticEntity.GATE) {
                if (editor.getTileType(Movement.UP, pos) == StaticEntity.WALL || editor.getTileType(Movement.UP, pos) == StaticEntity.GATE)
                    gc.drawImage(wallTileMap.get(MASK_GATE_UP), 16 * pos.x, 16 * (pos.y - 2) - 3, 16, 11);
                if (editor.getTileType(Movement.DOWN, pos) == StaticEntity.WALL || editor.getTileType(Movement.DOWN, pos) == StaticEntity.GATE)
                    gc.drawImage(wallTileMap.get(MASK_GATE_UP), 16 * pos.x, 16 * (pos.y - 2) - 3 + 11, 16, 11);
                if (editor.getTileType(Movement.LEFT, pos) == StaticEntity.WALL || editor.getTileType(Movement.LEFT, pos) == StaticEntity.GATE)
                    gc.drawImage(wallTileMap.get(MASK_GATE_LEFT), 16 * pos.x - 3, 16 * (pos.y - 2), 11, 16);
                if (editor.getTileType(Movement.RIGHT, pos) == StaticEntity.WALL || editor.getTileType(Movement.RIGHT, pos) == StaticEntity.GATE)
                    gc.drawImage(wallTileMap.get(MASK_GATE_LEFT), 16 * pos.x - 3 + 11, 16 * (pos.y - 2), 11, 16);
            } else if (editor.getTileType(pos) != StaticEntity.GATE && editor.getTileType(pos) != StaticEntity.WALL) {
                gc.drawImage(backgroundTileMap.get(editor.getTileType(pos)), 16 * pos.x, 16 * (pos.y - 2));
            }
        }
    }

    /**
     * Resize and draw the background canvas
     */
    private void drawBackground() {
        Editor editor = Editor.getInstance();
        background.setWidth(16*editor.getSizeX());
        background.setHeight(16*(editor.getSizeY() - 2));
        background.setScaleX(SCALE);
        background.setScaleY(SCALE);
        final GraphicsContext gc = background.getGraphicsContext2D();
        gc.setImageSmoothing(false);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, background.getWidth(), background.getHeight());
        for (int i = 0; i < editor.getSizeX(); i++) {
            for (int j = 2; j < editor.getSizeY(); j++) {
                Point pos = new Point(i, j);
                if (editor.getTileType(pos) == StaticEntity.WALL) {
                    gc.drawImage(wallTileMap.get(getWallMask(pos)), 16 * pos.x, 16 * (pos.y - 2));
                } else if (editor.getTileType(pos) == StaticEntity.GATE) {
                    if (editor.getTileType(Movement.UP, pos) == StaticEntity.WALL || editor.getTileType(Movement.UP, pos) == StaticEntity.GATE)
                        gc.drawImage(wallTileMap.get(MASK_GATE_UP), 16 * pos.x, 16 * (pos.y - 2) - 3, 16, 11);
                    if (editor.getTileType(Movement.DOWN, pos) == StaticEntity.WALL || editor.getTileType(Movement.DOWN, pos) == StaticEntity.GATE)
                        gc.drawImage(wallTileMap.get(MASK_GATE_UP), 16 * pos.x, 16 * (pos.y - 2) - 3 + 11, 16, 11);
                    if (editor.getTileType(Movement.LEFT, pos) == StaticEntity.WALL || editor.getTileType(Movement.LEFT, pos) == StaticEntity.GATE)
                        gc.drawImage(wallTileMap.get(MASK_GATE_LEFT), 16 * pos.x - 3, 16 * (pos.y - 2), 11, 16);
                    if (editor.getTileType(Movement.RIGHT, pos) == StaticEntity.WALL || editor.getTileType(Movement.RIGHT, pos) == StaticEntity.GATE)
                        gc.drawImage(wallTileMap.get(MASK_GATE_LEFT), 16 * pos.x - 3 + 11, 16 * (pos.y - 2), 11, 16);
                } else if (editor.getTileType(pos) != StaticEntity.GATE && editor.getTileType(pos) != StaticEntity.WALL) {
                    gc.drawImage(backgroundTileMap.get(editor.getTileType(pos)), 16 * pos.x, 16 * (pos.y - 2));
                }
            }
        }
    }

    /**
     * Compute the binary mask of a specific tile
     * @param pos the pos to test
     * @return a binary mask representing the wall connections
     */
    private byte getWallMask(Point pos) {
        Editor editor = Editor.getInstance();
        byte mask = 0b0000;
        mask |= (editor.getTileType(Movement.UP, pos) == StaticEntity.WALL ? MASK_WALL_UP : 0);
        mask |= (editor.getTileType(Movement.DOWN, pos) == StaticEntity.WALL ? MASK_WALL_DOWN : 0);
        mask |= (editor.getTileType(Movement.RIGHT, pos) == StaticEntity.WALL ? MASK_WALL_RIGHT : 0);
        mask |= (editor.getTileType(Movement.LEFT, pos) == StaticEntity.WALL ? MASK_WALL_LEFT : 0);
        return mask;
    }

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(renderer);
    }
}