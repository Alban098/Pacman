/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller.editor.fxml;

import controller.input.InputController;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import modele.Loader;
import modele.editor.Editor;
import modele.game.Game;
import modele.game.Grid;
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

    private Editor editor;
    private Grid grid;

    private Game game;
    private InputController inputController;

    private Runnable renderer;


    @Override
    public void start(Stage primaryStage) throws IOException {
        Loader loader = new Loader("map.xml", "controls.xml", "scores.xml");
        grid = new Grid(loader);
        editor = new Editor();

        backgroundTileMap = new HashMap<>();
        wallTileMap = new HashMap<>();

        background = new Canvas(16*grid.getSizeX(), 16*grid.getSizeY());

        BorderPane bp = new BorderPane();

        bp.setRight(FXMLLoader.load(getClass().getResource("/controller/editor/fxml/mapEditor.fxml")));
        bp.setCenter(background);

        StackPane root = new StackPane();
        root.getChildren().add(bp);
        Scene scene = new Scene(root);
        primaryStage.setTitle("Editor Map");
        primaryStage.setOnCloseRequest(we -> {
            editor.requestClose();
            game.setGameState(GameState.MENU_SCREEN);

            inputController.setEditorLaunched(false);
        });
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setResizable(false);
        primaryStage.show();


        loadSprites();

        renderer = () -> {
            drawBackground();
            root.setOnMousePressed(this::setTile);
            root.setOnMouseDragged(this::setTile);
        };

        editor.addObserver(this);

        new Thread(editor).start();

        root.requestFocus();
    }

    private void setTile(MouseEvent event) {
        Point pos = new Point((int) event.getSceneX() / 16, (int) event.getSceneY() / 16);
        if (grid.getStaticEntity(pos) != null && Controller.getSelectionnedEntity() != null && grid.getStaticEntity(pos) != Controller.getSelectionnedEntity()) {
            grid.setStaticEntity(pos, Controller.getSelectionnedEntity());
            if (grid.getStaticEntity(pos) == StaticEntity.WALL || grid.getStaticEntity(pos) == StaticEntity.EMPTY){
                if (pos.x == 0){
                    pos.x = grid.getSizeX() - 1;
                    grid.setStaticEntity(pos, Controller.getSelectionnedEntity());
                }
                if (pos.x == grid.getSizeX() - 1){
                    pos.x = 0;
                    grid.setStaticEntity(pos, Controller.getSelectionnedEntity());
                }
                if (pos.y == 0){
                    pos.y = grid.getSizeY() - 1;
                    grid.setStaticEntity(pos, Controller.getSelectionnedEntity());
                }
                if (pos.y == grid.getSizeY() - 1){
                    pos.y = 0;
                    grid.setStaticEntity(pos, Controller.getSelectionnedEntity());
                }
            }
        }
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public synchronized void setInputController(InputController inputController) {
        this.inputController = inputController;
    }

    private void loadSprites() {
        backgroundTileMap.put(StaticEntity.EMPTY, new Image("resources/sprites/map/empty.png"));
        backgroundTileMap.put(StaticEntity.GUM, new Image("resources/sprites/map/gum.png"));
        backgroundTileMap.put(StaticEntity.SUPER_GUM, new Image("resources/sprites/map/super_gum.png"));
        backgroundTileMap.put(StaticEntity.CHERRY, new Image("resources/sprites/items/cherry.png"));
        backgroundTileMap.put(StaticEntity.STRAWBERRY, new Image("resources/sprites/items/strawberry.png"));
        backgroundTileMap.put(StaticEntity.ORANGE, new Image("resources/sprites/items/orange.png"));
        backgroundTileMap.put(StaticEntity.APPLE, new Image("resources/sprites/items/apple.png"));
        backgroundTileMap.put(StaticEntity.MELON, new Image("resources/sprites/items/melon.png"));
        backgroundTileMap.put(StaticEntity.GALAXIAN_BOSS, new Image("resources/sprites/items/galaxian_boss.png"));
        backgroundTileMap.put(StaticEntity.BELL, new Image("resources/sprites/items/bell.png"));
        backgroundTileMap.put(StaticEntity.KEY, new Image("resources/sprites/items/key.png"));

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

    private void drawBackground() {
        background.setWidth(16*grid.getSizeX());
        background.setHeight(16*grid.getSizeY());
        background.setScaleX(SCALE);
        background.setScaleY(SCALE);
        final GraphicsContext gc = background.getGraphicsContext2D();
        gc.setImageSmoothing(false);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, background.getWidth(), background.getHeight());
        for (int i = 0; i < grid.getSizeX(); i++) {
            for (int j = 0; j < grid.getSizeY(); j++) {
                Point pos = new Point(i, j);
                if (grid.getStaticEntity(pos) == StaticEntity.WALL) {
                    gc.drawImage(wallTileMap.get(getWallMask(pos)), 16 * pos.x, 16 * pos.y);
                } else if (grid.getStaticEntity(pos) == StaticEntity.GATE) {
                    if (grid.getStaticEntity(Movement.UP, pos) == StaticEntity.WALL || grid.getStaticEntity(Movement.UP, pos) == StaticEntity.GATE)
                        gc.drawImage(wallTileMap.get(MASK_GATE_UP), 16 * pos.x, 16 * pos.y - 3, 16, 11);
                    if (grid.getStaticEntity(Movement.DOWN, pos) == StaticEntity.WALL || grid.getStaticEntity(Movement.DOWN, pos) == StaticEntity.GATE)
                        gc.drawImage(wallTileMap.get(MASK_GATE_UP), 16 * pos.x, 16 * pos.y - 3 + 11, 16, 11);
                    if (grid.getStaticEntity(Movement.LEFT, pos) == StaticEntity.WALL || grid.getStaticEntity(Movement.LEFT, pos) == StaticEntity.GATE)
                        gc.drawImage(wallTileMap.get(MASK_GATE_LEFT), 16 * pos.x - 3, 16 * pos.y, 11, 16);
                    if (grid.getStaticEntity(Movement.RIGHT, pos) == StaticEntity.WALL || grid.getStaticEntity(Movement.RIGHT, pos) == StaticEntity.GATE)
                        gc.drawImage(wallTileMap.get(MASK_GATE_LEFT), 16 * pos.x - 3 + 11, 16 * pos.y, 11, 16);
                } else {
                    gc.drawImage(backgroundTileMap.get(grid.getStaticEntity(pos)), 16 * pos.x, 16 * pos.y);
                }
            }
        }
    }

    private byte getWallMask(Point pos) {
        byte mask = 0b0000;
        mask |= (grid.getStaticEntity(Movement.UP, pos) == StaticEntity.WALL ? MASK_WALL_UP : 0);
        mask |= (grid.getStaticEntity(Movement.DOWN, pos) == StaticEntity.WALL ? MASK_WALL_DOWN : 0);
        mask |= (grid.getStaticEntity(Movement.RIGHT, pos) == StaticEntity.WALL ? MASK_WALL_RIGHT : 0);
        mask |= (grid.getStaticEntity(Movement.LEFT, pos) == StaticEntity.WALL ? MASK_WALL_LEFT : 0);
        return mask;
    }

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(renderer);
    }
}
