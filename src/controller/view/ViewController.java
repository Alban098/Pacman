package controller.view;

import controller.input.Input;
import controller.input.InputController;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.StageStyle;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import modele.Menu;
import modele.Utils;
import modele.game.Button;
import modele.game.Game;
import modele.game.Score;
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
import controller.audio.AudioController;

public class ViewController extends Application implements Observer {

    private static final byte MASK_WALL_RIGHT =  0b1000;
    private static final byte MASK_WALL_UP =     0b0100;
    private static final byte MASK_WALL_LEFT =   0b0010;
    private static final byte MASK_WALL_DOWN =   0b0001;
    private static final byte MASK_GATE_UP =   0b010000;
    private static final byte MASK_GATE_LEFT = 0b100000;

    public static final float SCALE = 1;

    private Map<MoveableEntity, Sprite> foregroundSpriteMap;
    private Map<StaticEntity, Image> backgroundTileMap;
    private Map<Byte, Image> wallTileMap;
    private Map<GUIElement, Image> GUITileMap;

    private Canvas foreground;
    private Canvas background;
    private Canvas gui;

    public AudioController audioController;
    private InputController inputController;

    public long whenToStopDeathAnim;
    public boolean isDeathAnimPlaying = false;
    public boolean isDeathAnimFinished = true;
    public boolean backgroundHasBeenDrawn = false;

    public long whenToStopScoreAnim;
    boolean isScoreAnimPlaying = false;
    private int dynamicScore;
    private Point dynamicScorePos;

    private Runnable renderer;

    private Point mouseCoords = new Point(0, 0);

    @Override
    public void start(Stage primaryStage) {

        Game game = Game.getInstance();
        Menu menu = Menu.getInstance();

        audioController = new AudioController();
        inputController = new InputController(audioController);


        renderer = () -> {
            primaryStage.setWidth(16 * game.getSizeX() * SCALE + 16);
            primaryStage.setHeight(16 * game.getSizeY() * SCALE + 39);
            if (game.getGameState() != GameState.LEVEL_EDITOR)
                inputController.setEditorLaunched(false);
            drawBackground();
            drawForeground();
            drawGUI();
        };

        backgroundTileMap = new HashMap<>();
        foregroundSpriteMap = new HashMap<>();
        wallTileMap = new HashMap<>();
        GUITileMap = new HashMap<>();

        foreground = new Canvas(16 * game.getSizeX(), 16 * game.getSizeY());
        background = new Canvas(16 * game.getSizeX(), 16 * game.getSizeY());
        gui = new Canvas(16 * game.getSizeX(), 16 * game.getSizeY());

        background.setScaleX(SCALE);
        background.setScaleY(SCALE);
        foreground.setScaleX(SCALE);
        foreground.setScaleY(SCALE);
        gui.setScaleX(SCALE);
        gui.setScaleY(SCALE);

        StackPane root = new StackPane();
        root.getChildren().add(background);
        root.getChildren().add(foreground);
        root.getChildren().add(gui);

        Scene scene = new Scene(root, 16 * game.getSizeX() * SCALE, 16 * game.getSizeY() * SCALE);

        primaryStage.setTitle("Pacman v1.0");
        primaryStage.setOnCloseRequest(we -> {
            game.requestClose();
            menu.requestClose();
            System.exit(0);
        });
        primaryStage.getIcons().add(new Image("resources/sprites/player/pacman_right_1.png"));
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setResizable(false);
        primaryStage.show();

        loadSprites();
        initButtons();

        game.addObserver(this);
        menu.addObserver(this);

        new Thread(game).start();
        new Thread(menu).start();

        root.setOnKeyPressed(event -> inputController.handleInput(event, null, this));
        root.setOnMouseClicked(event -> inputController.handleInput(null, event, this));
        root.setOnMouseMoved(event -> {
            mouseCoords = new Point((int) (event.getSceneX() / SCALE), (int) (event.getSceneY() / SCALE));
        });
        root.requestFocus();
    }

    /**
     * Load all the sprites and images in memory
     */
    private void loadSprites() {
        Game game = Game.getInstance();
        Sprite pacmanSprite = new Sprite();
        pacmanSprite.addImageAtlas(SpriteID.LEFT, 250, "resources/sprites/player/pacman_0.png", "resources/sprites/player/pacman_left_1.png", "resources/sprites/player/pacman_left_2.png", "resources/sprites/player/pacman_left_1.png");
        pacmanSprite.addImageAtlas(SpriteID.RIGHT, 250, "resources/sprites/player/pacman_0.png", "resources/sprites/player/pacman_right_1.png", "resources/sprites/player/pacman_right_2.png", "resources/sprites/player/pacman_right_1.png");
        pacmanSprite.addImageAtlas(SpriteID.UP, 250, "resources/sprites/player/pacman_0.png", "resources/sprites/player/pacman_up_1.png", "resources/sprites/player/pacman_up_2.png", "resources/sprites/player/pacman_up_1.png");
        pacmanSprite.addImageAtlas(SpriteID.DOWN, 250, "resources/sprites/player/pacman_0.png", "resources/sprites/player/pacman_down_1.png", "resources/sprites/player/pacman_down_2.png", "resources/sprites/player/pacman_down_1.png");
        pacmanSprite.addImageAtlas(SpriteID.DEATH, 1600, "resources/sprites/player/pacman_death_0.png", "resources/sprites/player/pacman_death_1.png", "resources/sprites/player/pacman_death_2.png", "resources/sprites/player/pacman_death_3.png", "resources/sprites/player/pacman_death_4.png", "resources/sprites/player/pacman_death_5.png", "resources/sprites/player/pacman_death_6.png", "resources/sprites/player/pacman_death_7.png", "resources/sprites/player/pacman_death_8.png", "resources/sprites/player/pacman_death_9.png", "resources/sprites/player/pacman_death_10.png", "resources/sprites/player/pacman_death_11.png");
        pacmanSprite.addImageAtlas(SpriteID.LAST, 250, "resources/sprites/player/pacman_0.png", "resources/sprites/player/pacman_up_1.png", "resources/sprites/player/pacman_up_2.png", "resources/sprites/player/pacman_up_1.png");
        foregroundSpriteMap.put(game.getPlayer(), pacmanSprite);


        Sprite blinkySprite = new Sprite();
        blinkySprite.addImageAtlas(SpriteID.LEFT, 333, "resources/sprites/ghost/blinky_left_0.png", "resources/sprites/ghost/blinky_left_1.png");
        blinkySprite.addImageAtlas(SpriteID.RIGHT, 333, "resources/sprites/ghost/blinky_right_0.png", "resources/sprites/ghost/blinky_right_1.png");
        blinkySprite.addImageAtlas(SpriteID.UP, 333, "resources/sprites/ghost/blinky_up_0.png", "resources/sprites/ghost/blinky_up_1.png");
        blinkySprite.addImageAtlas(SpriteID.DOWN, 333, "resources/sprites/ghost/blinky_down_0.png", "resources/sprites/ghost/blinky_down_1.png");
        blinkySprite.addImageAtlas(SpriteID.LAST, 333, "resources/sprites/ghost/blinky_up_0.png", "resources/sprites/ghost/blinky_up_1.png");
        blinkySprite.addImageAtlas(SpriteID.FRIGHTENED, 500, "resources/sprites/ghost/frightened_0.png", "resources/sprites/ghost/frightened_1.png", "resources/sprites/ghost/frightened_2.png", "resources/sprites/ghost/frightened_3.png");
        blinkySprite.addImageAtlas(SpriteID.EATEN_LEFT, 1000, "resources/sprites/ghost/eaten_left.png");
        blinkySprite.addImageAtlas(SpriteID.EATEN_RIGHT, 1000, "resources/sprites/ghost/eaten_right.png");
        blinkySprite.addImageAtlas(SpriteID.EATEN_UP, 1000, "resources/sprites/ghost/eaten_up.png");
        blinkySprite.addImageAtlas(SpriteID.EATEN_DOWN, 1000, "resources/sprites/ghost/eaten_down.png");
        foregroundSpriteMap.put(game.getGhost(GhostName.BLINKY), blinkySprite);

        Sprite pinkySprite = new Sprite();
        pinkySprite.addImageAtlas(SpriteID.LEFT, 333, "resources/sprites/ghost/pinky_left_0.png", "resources/sprites/ghost/pinky_left_1.png");
        pinkySprite.addImageAtlas(SpriteID.RIGHT, 333, "resources/sprites/ghost/pinky_right_0.png", "resources/sprites/ghost/pinky_right_1.png");
        pinkySprite.addImageAtlas(SpriteID.UP, 333, "resources/sprites/ghost/pinky_up_0.png", "resources/sprites/ghost/pinky_up_1.png");
        pinkySprite.addImageAtlas(SpriteID.DOWN, 333, "resources/sprites/ghost/pinky_down_0.png", "resources/sprites/ghost/pinky_down_1.png");
        pinkySprite.addImageAtlas(SpriteID.LAST, 333, "resources/sprites/ghost/pinky_up_0.png", "resources/sprites/ghost/pinky_up_1.png");
        pinkySprite.addImageAtlas(SpriteID.FRIGHTENED, 500, "resources/sprites/ghost/frightened_0.png", "resources/sprites/ghost/frightened_1.png", "resources/sprites/ghost/frightened_2.png", "resources/sprites/ghost/frightened_3.png");
        pinkySprite.addImageAtlas(SpriteID.EATEN_LEFT, 1000, "resources/sprites/ghost/eaten_left.png");
        pinkySprite.addImageAtlas(SpriteID.EATEN_RIGHT, 1000, "resources/sprites/ghost/eaten_right.png");
        pinkySprite.addImageAtlas(SpriteID.EATEN_UP, 1000, "resources/sprites/ghost/eaten_up.png");
        pinkySprite.addImageAtlas(SpriteID.EATEN_DOWN, 1000, "resources/sprites/ghost/eaten_down.png");
        foregroundSpriteMap.put(game.getGhost(GhostName.PINKY), pinkySprite);

        Sprite inkySprite = new Sprite();
        inkySprite.addImageAtlas(SpriteID.LEFT, 333, "resources/sprites/ghost/inky_left_0.png", "resources/sprites/ghost/inky_left_1.png");
        inkySprite.addImageAtlas(SpriteID.RIGHT, 333, "resources/sprites/ghost/inky_right_0.png", "resources/sprites/ghost/inky_right_1.png");
        inkySprite.addImageAtlas(SpriteID.UP, 333, "resources/sprites/ghost/inky_up_0.png", "resources/sprites/ghost/inky_up_1.png");
        inkySprite.addImageAtlas(SpriteID.DOWN, 333, "resources/sprites/ghost/inky_down_0.png", "resources/sprites/ghost/inky_down_1.png");
        inkySprite.addImageAtlas(SpriteID.LAST, 333, "resources/sprites/ghost/inky_up_0.png", "resources/sprites/ghost/inky_up_1.png");
        inkySprite.addImageAtlas(SpriteID.FRIGHTENED, 500, "resources/sprites/ghost/frightened_0.png", "resources/sprites/ghost/frightened_1.png", "resources/sprites/ghost/frightened_2.png", "resources/sprites/ghost/frightened_3.png");
        inkySprite.addImageAtlas(SpriteID.EATEN_LEFT, 1000, "resources/sprites/ghost/eaten_left.png");
        inkySprite.addImageAtlas(SpriteID.EATEN_RIGHT, 1000, "resources/sprites/ghost/eaten_right.png");
        inkySprite.addImageAtlas(SpriteID.EATEN_UP, 1000, "resources/sprites/ghost/eaten_up.png");
        inkySprite.addImageAtlas(SpriteID.EATEN_DOWN, 1000, "resources/sprites/ghost/eaten_down.png");
        foregroundSpriteMap.put(game.getGhost(GhostName.INKY), inkySprite);

        Sprite clydeSprite = new Sprite();
        clydeSprite.addImageAtlas(SpriteID.LEFT, 333, "resources/sprites/ghost/clyde_left_0.png", "resources/sprites/ghost/clyde_left_1.png");
        clydeSprite.addImageAtlas(SpriteID.RIGHT, 333, "resources/sprites/ghost/clyde_right_0.png", "resources/sprites/ghost/clyde_right_1.png");
        clydeSprite.addImageAtlas(SpriteID.UP, 333, "resources/sprites/ghost/clyde_up_0.png", "resources/sprites/ghost/clyde_up_1.png");
        clydeSprite.addImageAtlas(SpriteID.DOWN, 333, "resources/sprites/ghost/clyde_down_0.png", "resources/sprites/ghost/clyde_down_1.png");
        clydeSprite.addImageAtlas(SpriteID.LAST, 333, "resources/sprites/ghost/clyde_up_0.png", "resources/sprites/ghost/clyde_up_1.png");
        clydeSprite.addImageAtlas(SpriteID.FRIGHTENED, 500, "resources/sprites/ghost/frightened_0.png", "resources/sprites/ghost/frightened_1.png", "resources/sprites/ghost/frightened_2.png", "resources/sprites/ghost/frightened_3.png");
        clydeSprite.addImageAtlas(SpriteID.EATEN_LEFT, 1000, "resources/sprites/ghost/eaten_left.png");
        clydeSprite.addImageAtlas(SpriteID.EATEN_RIGHT, 1000, "resources/sprites/ghost/eaten_right.png");
        clydeSprite.addImageAtlas(SpriteID.EATEN_UP, 1000, "resources/sprites/ghost/eaten_up.png");
        clydeSprite.addImageAtlas(SpriteID.EATEN_DOWN, 1000, "resources/sprites/ghost/eaten_down.png");
        foregroundSpriteMap.put(game.getGhost(GhostName.CLYDE), clydeSprite);

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

        GUITileMap.put(GUIElement.N_0, new Image("resources/sprites/gui/0.png"));
        GUITileMap.put(GUIElement.N_1, new Image("resources/sprites/gui/1.png"));
        GUITileMap.put(GUIElement.N_2, new Image("resources/sprites/gui/2.png"));
        GUITileMap.put(GUIElement.N_3, new Image("resources/sprites/gui/3.png"));
        GUITileMap.put(GUIElement.N_4, new Image("resources/sprites/gui/4.png"));
        GUITileMap.put(GUIElement.N_5, new Image("resources/sprites/gui/5.png"));
        GUITileMap.put(GUIElement.N_6, new Image("resources/sprites/gui/6.png"));
        GUITileMap.put(GUIElement.N_7, new Image("resources/sprites/gui/7.png"));
        GUITileMap.put(GUIElement.N_8, new Image("resources/sprites/gui/8.png"));
        GUITileMap.put(GUIElement.N_9, new Image("resources/sprites/gui/9.png"));
        GUITileMap.put(GUIElement.SCORE_200, new Image("resources/sprites/gui/score-200.png"));
        GUITileMap.put(GUIElement.SCORE_400, new Image("resources/sprites/gui/score-400.png"));
        GUITileMap.put(GUIElement.SCORE_800, new Image("resources/sprites/gui/score-800.png"));
        GUITileMap.put(GUIElement.SCORE_1600, new Image("resources/sprites/gui/score-1600.png"));
        GUITileMap.put(GUIElement.SCORE_FRUIT_100, new Image("resources/sprites/gui/score-object-100.png"));
        GUITileMap.put(GUIElement.SCORE_FRUIT_300, new Image("resources/sprites/gui/score-object-300.png"));
        GUITileMap.put(GUIElement.SCORE_FRUIT_500, new Image("resources/sprites/gui/score-object-500.png"));
        GUITileMap.put(GUIElement.SCORE_FRUIT_700, new Image("resources/sprites/gui/score-object-700.png"));
        GUITileMap.put(GUIElement.SCORE_FRUIT_1000, new Image("resources/sprites/gui/score-object-1000.png"));
        GUITileMap.put(GUIElement.SCORE_FRUIT_2000, new Image("resources/sprites/gui/score-object-2000.png"));
        GUITileMap.put(GUIElement.SCORE_FRUIT_3000, new Image("resources/sprites/gui/score-object-3000.png"));
        GUITileMap.put(GUIElement.SCORE_FRUIT_5000, new Image("resources/sprites/gui/score-object-5000.png"));
        GUITileMap.put(GUIElement.READY, new Image("resources/sprites/gui/ready.png"));
        GUITileMap.put(GUIElement.GAME_OVER, new Image("resources/sprites/gui/gameover.png"));
        GUITileMap.put(GUIElement.LIVE, new Image("resources/sprites/gui/live.png"));
        GUITileMap.put(GUIElement.LOGO, new Image("resources/sprites/gui/logo.png"));


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
     * Reset every entity's Sprite to its UP Animation
     */
    public void resetSprites() {
        for (MoveableEntity e : Game.getInstance().getEntities())
            if (e instanceof EntityGhost || e instanceof EntityPlayer)
                foregroundSpriteMap.get(e).getFrame(SpriteID.UP);
    }

    /**
     * Draw the Background canvas
     */
    private void drawBackground() {
        Game game = Game.getInstance();
        background.setWidth(16*game.getSizeX());
        background.setHeight(16*game.getSizeY());
        background.setScaleX(SCALE);
        background.setScaleY(SCALE);
        final GraphicsContext gc = background.getGraphicsContext2D();
        if (!backgroundHasBeenDrawn) {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, background.getWidth(), background.getHeight());
            switch (game.getGameState()) {
                case GAME_SCREEN:
                    backgroundHasBeenDrawn = true;
                    for (int i = 0; i < game.getSizeX(); i++) {
                        for (int j = 0; j < game.getSizeY(); j++) {
                            Point pos = new Point(i, j);
                            if (game.getTileType(pos) == StaticEntity.WALL) {
                                gc.drawImage(wallTileMap.get(getWallMask(pos)), 16 * pos.x, 16 * pos.y);
                            } else if (game.getTileType(pos) == StaticEntity.GATE) {
                                if (game.getTileType(Movement.UP, pos) == StaticEntity.WALL || game.getTileType(Movement.UP, pos) == StaticEntity.GATE)
                                    gc.drawImage(wallTileMap.get(MASK_GATE_UP), 16 * pos.x, 16 * pos.y - 3, 16, 11);
                                if (game.getTileType(Movement.DOWN, pos) == StaticEntity.WALL || game.getTileType(Movement.DOWN, pos) == StaticEntity.GATE)
                                    gc.drawImage(wallTileMap.get(MASK_GATE_UP), 16 * pos.x, 16 * pos.y - 3 + 11, 16, 11);
                                if (game.getTileType(Movement.LEFT, pos) == StaticEntity.WALL || game.getTileType(Movement.LEFT, pos) == StaticEntity.GATE)
                                    gc.drawImage(wallTileMap.get(MASK_GATE_LEFT), 16 * pos.x - 3, 16 * pos.y, 11, 16);
                                if (game.getTileType(Movement.RIGHT, pos) == StaticEntity.WALL || game.getTileType(Movement.RIGHT, pos) == StaticEntity.GATE)
                                    gc.drawImage(wallTileMap.get(MASK_GATE_LEFT), 16 * pos.x - 3 + 11, 16 * pos.y, 11, 16);
                            }
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Draw the Foreground canvas
     */
    private void drawForeground() {
        Game game = Game.getInstance();
        EntityPlayer player = game.getPlayer();
        foreground.setWidth(16*game.getSizeX());
        foreground.setHeight(16*game.getSizeY());
        foreground.setScaleX(SCALE);
        foreground.setScaleY(SCALE);
        final GraphicsContext gc = foreground.getGraphicsContext2D();
        gc.clearRect(0, 0, foreground.getWidth(), foreground.getHeight());
        switch (game.getGameState()) {
            case GAME_SCREEN:
                for (int i = 0; i < game.getSizeX(); i++) {
                    for (int j = 0; j < game.getSizeY(); j++) {
                        Point pos = new Point(i, j);
                        if (game.getTileType(pos) != StaticEntity.GATE && game.getTileType(pos) != StaticEntity.WALL) {
                            gc.drawImage(backgroundTileMap.get(game.getTileType(pos)), 16 * pos.x, 16 * pos.y);
                        }
                    }
                }
                if (!game.isPlayerDead()) {
                    for (MoveableEntity e : game.getEntities())
                        if (!(e instanceof FruitSpawner)) {
                            drawSprite(e, gc);
                        }
                } else {
                    if (!isDeathAnimPlaying) {
                        isDeathAnimPlaying = true;
                        isDeathAnimFinished = false;
                        Sprite sprite = foregroundSpriteMap.get(player);
                        sprite.startAnimation(SpriteID.DEATH);
                        whenToStopDeathAnim = System.currentTimeMillis() + sprite.getDuration(SpriteID.DEATH);
                    }
                    if (System.currentTimeMillis() <= whenToStopDeathAnim) {
                        Point pos = game.getPosition(player);
                        gc.drawImage(foregroundSpriteMap.get(player).getFrame(SpriteID.DEATH), 16 * pos.x, 16 * pos.y);
                    } else {
                        game.resetPlayer();
                        audioController.canPlayIntro(true);
                        isDeathAnimFinished = true;
                        if (!(game.isGameFinished() && game.isPlayerDead())) {
                            foregroundSpriteMap.get(player).getFrame(SpriteID.UP);
                            foregroundSpriteMap.get(game.getGhost(GhostName.BLINKY)).getFrame(SpriteID.UP);
                            foregroundSpriteMap.get(game.getGhost(GhostName.CLYDE)).getFrame(SpriteID.UP);
                            foregroundSpriteMap.get(game.getGhost(GhostName.INKY)).getFrame(SpriteID.UP);
                            foregroundSpriteMap.get(game.getGhost(GhostName.PINKY)).getFrame(SpriteID.UP);
                            for (MoveableEntity e : game.getEntities())
                                if (!(e instanceof FruitSpawner))
                                    drawSprite(e, gc);
                        }
                    }
                }
                break;
        }
    }

    /**
     * Draw the GUI canvas
     */
    private void drawGUI() {
        Game game = Game.getInstance();
        Menu menu = Menu.getInstance();

        gui.setWidth(16*game.getSizeX());
        gui.setHeight(16*game.getSizeY());
        gui.setScaleX(SCALE);
        gui.setScaleY(SCALE);
        final GraphicsContext gc = gui.getGraphicsContext2D();
        gc.clearRect(0, 0, gui.getWidth(), gui.getHeight());
        switch (game.getGameState()) {
            case GAME_SCREEN:
                gc.setFill(Color.gray(.2f));
                gc.fillRect(0, 0, gui.getWidth(), 33);

                gc.setFill(Color.WHITE);
                gc.setFont(javafx.scene.text.Font.font("Verdana", FontWeight.BOLD, 10));
                gc.setTextAlign(TextAlignment.LEFT);
                gc.fillText("Level", 12, 13);
                gc.fillText("Score", 12, 13+16);


                for (int i = (int) Math.log10(game.getTotalScore() + 1); i >= 0; i--) {
                    int digit = (int) (game.getTotalScore() / Math.pow(10, (int)Math.log10(game.getTotalScore()) -  i)) % 10;
                    drawDigit(digit, new Point(9*(i + 5) + 3, 21), gc);
                }
                for (int i = (int) Math.log10(game.getLevel()); i >= 0; i--) {
                    int digit = (int) (game.getLevel() / Math.pow(10, (int)Math.log10(game.getLevel()) -  i)) % 10;
                    drawDigit(digit, new Point(9*(i + 5), 5), gc);
                }
                for (int i = 0; i < game.getLives(); i++) {
                    gc.drawImage(GUITileMap.get(GUIElement.LIVE), gui.getWidth() - 15*(i+2), 16);
                }

                if (!game.isGameStarted() && isDeathAnimFinished && game.getLives() >= 0) {
                    backgroundHasBeenDrawn = false;
                    gc.drawImage(GUITileMap.get(GUIElement.READY), 16*(game.getSizeX()/2 - 2), 16*(game.getSizeY()/2 + 2), 5*16, 16);
                }

                if (game.isGameFinished() &&  game.isPlayerDead() && isDeathAnimFinished) {
                    gc.drawImage(GUITileMap.get(GUIElement.GAME_OVER), 16*(game.getSizeX()/2 - 3), 16*(game.getSizeY()/2 + 2), 7*16, 16);
                }

                int score = game.getDynamicScoreEventValue();
                if (score != 0) {
                    dynamicScore = score;
                    dynamicScorePos = game.getPosition(game.getPlayer());
                    isScoreAnimPlaying = true;
                    whenToStopScoreAnim = System.currentTimeMillis() + 1000;
                }
                if (isScoreAnimPlaying) {
                    switch (dynamicScore) {
                        case 200:
                            gc.drawImage(GUITileMap.get(GUIElement.SCORE_200), 16 * dynamicScorePos.x, 16 * dynamicScorePos.y);
                            break;
                        case 400:
                            gc.drawImage(GUITileMap.get(GUIElement.SCORE_400), 16 * dynamicScorePos.x, 16 * dynamicScorePos.y);
                            break;
                        case 800:
                            gc.drawImage(GUITileMap.get(GUIElement.SCORE_800), 16 * dynamicScorePos.x, 16 * dynamicScorePos.y);
                            break;
                        case 1600:
                            gc.drawImage(GUITileMap.get(GUIElement.SCORE_1600), 16 * dynamicScorePos.x, 16 * dynamicScorePos.y);
                            break;
                        case -100:
                            gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_100), 16 * dynamicScorePos.x, 16 * dynamicScorePos.y);
                            break;
                        case -300:
                            gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_300), 16 * dynamicScorePos.x, 16 * dynamicScorePos.y);
                            break;
                        case -500:
                            gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_500), 16 * dynamicScorePos.x, 16 * dynamicScorePos.y);
                            break;
                        case -700:
                            gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_700), 16 * dynamicScorePos.x, 16 * dynamicScorePos.y);
                            break;
                        case -1000:
                            gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_1000), 16 * dynamicScorePos.x, 16 * dynamicScorePos.y);
                            break;
                        case -2000:
                            gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_2000), 16 * dynamicScorePos.x, 16 * dynamicScorePos.y);
                            break;
                        case -3000:
                            gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_3000), 16 * dynamicScorePos.x, 16 * dynamicScorePos.y);
                            break;
                        case -5000:
                            gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_5000), 16 * dynamicScorePos.x, 16 * dynamicScorePos.y);
                            break;
                        default:
                    }
                    if (System.currentTimeMillis() >= whenToStopScoreAnim) {
                        isScoreAnimPlaying = false;
                        dynamicScore = 0;
                    }
                }
                break;
            case LEVEL_EDITOR:
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, gui.getWidth(), gui.getHeight());
            case MENU_SCREEN:
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, gui.getWidth(), gui.getHeight());
                switch (menu.getTab()) {
                    case MAIN:
                        Image logo = GUITileMap.get(GUIElement.LOGO);
                        float aspectRatio = (float) (logo.getWidth() / logo.getHeight());
                        gc.setStroke(Color.BLUE);
                        gc.setFill(Color.YELLOW);
                        gc.setTextAlign(TextAlignment.CENTER);
                        gc.drawImage(logo, gui.getWidth() * 0.125, gui.getWidth() * 0.125 / 2, gui.getWidth() * 0.75, gui.getWidth() / 1.25 / aspectRatio);

                        for (String buttonId : MenuTab.MAIN.getButtonList())
                            drawButton(gc, MenuTab.MAIN.getButton(buttonId));
                        break;
                    case CONTROLS:
                        gc.setFill(Color.YELLOW);
                        gc.setTextAlign(TextAlignment.CENTER);
                        Sprite pacman = foregroundSpriteMap.get(game.getPlayer());
                        Sprite ghost = foregroundSpriteMap.get(game.getGhost(GhostName.BLINKY));

                        SpriteID dirP1 = SpriteID.DOWN, dirP2 = SpriteID.UP;
                        if (Utils.isInsideTri(mouseCoords, new Point(0, 0), new Point((int) (gui.getWidth() / 2), (int) (gui.getHeight() / 3)), new Point((int) gui.getWidth(), 0)))
                            dirP1 = SpriteID.UP;
                        if (Utils.isInsideTri(mouseCoords, new Point(0, 0), new Point((int) (gui.getWidth() / 2), (int) (gui.getHeight() / 3)), new Point(0, (int) (gui.getHeight()*(2/3f)))))
                            dirP1 = SpriteID.LEFT;
                        if (Utils.isInsideTri(mouseCoords, new Point((int) gui.getWidth(), 0), new Point((int) (gui.getWidth() / 2), (int) (gui.getHeight() / 3)), new Point((int) gui.getWidth(), (int) (gui.getHeight()*(2/3f)))))
                            dirP1 = SpriteID.RIGHT;

                        if (Utils.isInsideTri(mouseCoords, new Point(0, (int) gui.getHeight()), new Point((int) (gui.getWidth() / 2), (int) (gui.getHeight() * (3/4f))), new Point((int) gui.getWidth(), (int) gui.getHeight())))
                            dirP2 = SpriteID.DOWN;
                        if (Utils.isInsideTri(mouseCoords, new Point(0, (int) (gui.getHeight() * (1/2f))), new Point((int) (gui.getWidth() / 2), (int) (gui.getHeight() * (3/4f))), new Point(0, (int) gui.getHeight())))
                            dirP2 = SpriteID.LEFT;
                        if (Utils.isInsideTri(mouseCoords, new Point((int) gui.getWidth(), (int) (gui.getHeight() * (1/2f))), new Point((int) (gui.getWidth() / 2), (int) (gui.getHeight() * (3/4f))), new Point((int) gui.getWidth(), (int) gui.getHeight())))
                            dirP2 = SpriteID.RIGHT;

                        gc.drawImage(pacman.getFrame(dirP1), gui.getWidth()/2 - 16, gui.getHeight()/3 - 16, 32, 32);
                        gc.drawImage(ghost.getFrame(dirP2), gui.getWidth()/2 - 16, gui.getHeight() * (3/4f) - 16, 32, 32);
                        for (String buttonId : MenuTab.CONTROLS.getButtonList())
                            drawButton(gc, MenuTab.CONTROLS.getButton(buttonId));
                        break;
                    case HIGHSCORE:
                        gc.setFill(Color.YELLOW);
                        gc.setTextAlign(TextAlignment.CENTER);
                        for (String buttonId : MenuTab.HIGHSCORE.getButtonList())
                            drawButton(gc, MenuTab.HIGHSCORE.getButton(buttonId));

                        gc.setFont(javafx.scene.text.Font.font("Verdana", FontWeight.BOLD,  15));
                        for (int i = 0; i < 9; i++) {
                            if (i < game.getHighscores().size()) {
                                Score s = game.getHighscores().get(i);
                                gc.setTextAlign(TextAlignment.LEFT);
                                gc.fillText((i + 1) + "   " + s.getName(), gui.getWidth() / 4, gui.getHeight() / 5 + i * 30);
                                gc.setTextAlign(TextAlignment.RIGHT);
                                gc.fillText(s.getScore() + "", gui.getWidth() * 3 / 4f, gui.getHeight() / 5 + i * 30);
                            } else {
                                gc.setTextAlign(TextAlignment.LEFT);
                                gc.fillText((i + 1) + "   " + "---", gui.getWidth() / 4, gui.getHeight() / 5 + i * 30);
                                gc.setTextAlign(TextAlignment.RIGHT);
                                gc.fillText("---" + "", gui.getWidth() * 3 / 4f, gui.getHeight() / 5 + i * 30);
                            }
                        }
                        break;
                    case HIGHSCORE_ENTER:
                        gc.setFill(Color.YELLOW);
                        gc.setTextAlign(TextAlignment.CENTER);
                        gc.setFont(javafx.scene.text.Font.font("Verdana", FontWeight.BOLD,  15));
                        gc.fillText("Your score : " + game.getScoreToSave(), gui.getWidth()/2, gui.getHeight() / 4);
                        gc.fillText("Enter your name", gui.getWidth()/2, gui.getHeight() / 2);
                        for (String buttonId : MenuTab.HIGHSCORE_ENTER.getButtonList())
                            drawButton(gc, MenuTab.HIGHSCORE_ENTER.getButton(buttonId));
                        break;
                }
                break;
        }
    }

    /**
     * Draw a button on a canvas
     * @param gc the GraphicsContext to draw to
     * @param button the button to draw
     */
    private void drawButton(GraphicsContext gc, Button button) {
        gc.setFont(javafx.scene.text.Font.font("Verdana", FontWeight.BOLD,  button.getFontSize()));
        if (Utils.isInside(mouseCoords, button.getHitbox())) gc.setStroke(Color.RED);
        else gc.setStroke(Color.BLUE);
        gc.strokeRoundRect(button.getHitbox().x, button.getHitbox().y, button.getHitbox().width, button.getHitbox().height, 10, 10);
        if (button.isSelected()) {
            gc.fillText("...", button.getHitbox().x + button.getHitbox().width / 2f, button.getHitbox().y + button.getFontSize() * 4/3f);
        } else
            gc.fillText(button.getText(), button.getHitbox().x + button.getHitbox().width / 2f, button.getHitbox().y + button.getFontSize() * 4/3f);
    }

    /**
     * Draw a digit on a canvas
     * @param digit the digit to draw
     * @param pos where to draw the digit
     * @param gc the GraphicsContext to draw to
     */
    private void drawDigit(int digit, Point pos, GraphicsContext gc) {
        switch (digit) {
            case 0:
                gc.drawImage(GUITileMap.get(GUIElement.N_0), pos.x, pos.y);
                break;
            case 1:
                gc.drawImage(GUITileMap.get(GUIElement.N_1), pos.x, pos.y);
                break;
            case 2:
                gc.drawImage(GUITileMap.get(GUIElement.N_2), pos.x, pos.y);
                break;
            case 3:
                gc.drawImage(GUITileMap.get(GUIElement.N_3), pos.x, pos.y);
                break;
            case 4:
                gc.drawImage(GUITileMap.get(GUIElement.N_4), pos.x, pos.y);
                break;
            case 5:
                gc.drawImage(GUITileMap.get(GUIElement.N_5), pos.x, pos.y);
                break;
            case 6:
                gc.drawImage(GUITileMap.get(GUIElement.N_6), pos.x, pos.y);
                break;
            case 7:
                gc.drawImage(GUITileMap.get(GUIElement.N_7), pos.x, pos.y);
                break;
            case 8:
                gc.drawImage(GUITileMap.get(GUIElement.N_8), pos.x, pos.y);
                break;
            case 9:
                gc.drawImage(GUITileMap.get(GUIElement.N_9), pos.x, pos.y);
                break;
        }
    }

    /**
     * Draw a sprite on a canvas
     * @param entity the Entity to draw
     * @param gc the GraphicsContext to draw to
     */
    private void drawSprite(MoveableEntity entity, GraphicsContext gc) {
        Game game = Game.getInstance();
        if (entity == null)
            return;
        Point pos = game.getPosition(entity);
        Point lastPos = new Point(pos.x, pos.y);
        Movement dir = game.getDirection(entity);
        Sprite sprite = foregroundSpriteMap.get(entity);
        float animationPercent = game.getAnimationPercent(entity);
        if (entity instanceof EntityGhost) {
            if (((EntityGhost) entity).getState() == GhostState.EATEN) {
                switch (dir) {
                    case UP:
                        lastPos.y++;
                        gc.drawImage(sprite.getFrame(SpriteID.EATEN_UP), 16 * lastPos.x, 16 * (lastPos.y - animationPercent));
                        break;
                    case DOWN:
                        lastPos.y--;
                        gc.drawImage(sprite.getFrame(SpriteID.EATEN_DOWN), 16 * lastPos.x, 16 * (lastPos.y + animationPercent));
                        break;
                    case LEFT:
                        lastPos.x++;
                        gc.drawImage(sprite.getFrame(SpriteID.EATEN_LEFT), 16 * (lastPos.x - animationPercent), 16 * lastPos.y);
                        break;
                    case RIGHT:
                        lastPos.x--;
                        gc.drawImage(sprite.getFrame(SpriteID.EATEN_RIGHT), 16 * (lastPos.x + animationPercent), 16 * lastPos.y);
                        break;
                    default:
                        gc.drawImage(sprite.getFrame(SpriteID.LAST), 16 * pos.x, 16 * pos.y);
                        break;
                }
                return;
            } else if (((EntityGhost) entity).getState() == GhostState.FRIGHTENED) {
                switch (dir) {
                    case UP:
                        lastPos.y++;
                        gc.drawImage(sprite.getFrame(SpriteID.FRIGHTENED), 16 * lastPos.x, 16 * (lastPos.y - animationPercent));
                        break;
                    case DOWN:
                        lastPos.y--;
                        gc.drawImage(sprite.getFrame(SpriteID.FRIGHTENED), 16 * lastPos.x, 16 * (lastPos.y + animationPercent));
                        break;
                    case LEFT:
                        lastPos.x++;
                        gc.drawImage(sprite.getFrame(SpriteID.FRIGHTENED), 16 * (lastPos.x - animationPercent), 16 * lastPos.y);
                        break;
                    case RIGHT:
                        lastPos.x--;
                        gc.drawImage(sprite.getFrame(SpriteID.FRIGHTENED), 16 * (lastPos.x + animationPercent), 16 * lastPos.y);
                        break;
                    default:
                        gc.drawImage(sprite.getFrame(SpriteID.FRIGHTENED), 16 * pos.x, 16 * pos.y);
                        break;
                }
                return;
            }
        }
        switch (dir) {
            case UP:
                lastPos.y++;
                gc.drawImage(sprite.getFrame(SpriteID.UP), 16 * lastPos.x, 16 * (lastPos.y - animationPercent));
                break;
            case DOWN:
                lastPos.y--;
                gc.drawImage(sprite.getFrame(SpriteID.DOWN), 16 * lastPos.x, 16 * (lastPos.y + animationPercent));
                break;
            case LEFT:
                lastPos.x++;
                gc.drawImage(sprite.getFrame(SpriteID.LEFT), 16 * (lastPos.x - animationPercent), 16 * lastPos.y);
                break;
            case RIGHT:
                lastPos.x--;
                gc.drawImage(sprite.getFrame(SpriteID.RIGHT), 16 * (lastPos.x + animationPercent), 16 * lastPos.y);
                break;
            default:
                gc.drawImage(sprite.getFrame(SpriteID.LAST), 16 * pos.x, 16 * pos.y);
                break;
        }
    }

    /**
     * Compute the binary mask of a specific tile
     * @param pos the pos to test
     * @return a binary mask representing the wall connections
     */
    private byte getWallMask(Point pos) {
        Game game = Game.getInstance();
        byte mask = 0b0000;
        mask |= (game.getTileType(Movement.UP, pos) == StaticEntity.WALL ? MASK_WALL_UP : 0);
        mask |= (game.getTileType(Movement.DOWN, pos) == StaticEntity.WALL ? MASK_WALL_DOWN : 0);
        mask |= (game.getTileType(Movement.RIGHT, pos) == StaticEntity.WALL ? MASK_WALL_RIGHT : 0);
        mask |= (game.getTileType(Movement.LEFT, pos) == StaticEntity.WALL ? MASK_WALL_LEFT : 0);
        return mask;
    }

    /**
     * Create and initialize every button of the menu
     */
    public void initButtons() {
        MenuTab.CONTROLS.addButton("back", new Button("⮈", new Rectangle((int)(gui.getWidth() * 1/15), (int)(gui.getWidth() * 1/15), 50, 34), 18));
        MenuTab.CONTROLS.addButton(Input.UP_P1.toString(), new Button(inputController.getKey(Input.UP_P1).getName(), new Rectangle((int)(gui.getWidth() / 2 - 37), (int)(gui.getHeight() / 3 - 15 - 40), 74, 30), 15));
        MenuTab.CONTROLS.addButton(Input.DOWN_P1.toString(), new Button(inputController.getKey(Input.DOWN_P1).getName(), new Rectangle((int)(gui.getWidth() /2 - 37), (int)(gui.getHeight() / 3 - 15 + 40), 74, 30), 15));
        MenuTab.CONTROLS.addButton(Input.RIGHT_P1.toString(), new Button(inputController.getKey(Input.RIGHT_P1).getName(), new Rectangle((int)(gui.getWidth() / 2 - 37 + 62), (int)(gui.getHeight() / 3 - 15), 74, 30), 15));
        MenuTab.CONTROLS.addButton(Input.LEFT_P1.toString(), new Button(inputController.getKey(Input.LEFT_P1).getName(), new Rectangle((int)(gui.getWidth() / 2 - 37 - 62), (int)(gui.getHeight() / 3 - 15), 74, 30), 15));
        MenuTab.CONTROLS.addButton(Input.UP_P2.toString(), new Button(inputController.getKey(Input.UP_P2).getName(), new Rectangle((int)(gui.getWidth() / 2 - 37), (int)(gui.getHeight() * (3/4f) - 15 - 40), 74, 30), 15));
        MenuTab.CONTROLS.addButton(Input.DOWN_P2.toString(), new Button(inputController.getKey(Input.DOWN_P2).getName(), new Rectangle((int)(gui.getWidth() /2 - 37), (int)(gui.getHeight() * (3/4f) - 15 + 40), 74, 30), 15));
        MenuTab.CONTROLS.addButton(Input.RIGHT_P2.toString(), new Button(inputController.getKey(Input.RIGHT_P2).getName(), new Rectangle((int)(gui.getWidth() / 2 - 37 + 62), (int)(gui.getHeight() * (3/4f) - 15), 74, 30), 15));
        MenuTab.CONTROLS.addButton(Input.LEFT_P2.toString(), new Button(inputController.getKey(Input.LEFT_P2).getName(), new Rectangle((int)(gui.getWidth() / 2 - 37 - 62), (int)(gui.getHeight() * (3/4f) - 15), 74, 30), 15));

        MenuTab.HIGHSCORE.addButton("back", new Button("⮈", new Rectangle((int)(gui.getWidth() * 1/15), (int)(gui.getWidth() * 1/15), 50, 34), 18));

        MenuTab.HIGHSCORE_ENTER.addButton("name", new Button("", new Rectangle((int)(gui.getWidth() * 1/6), (int)(gui.getHeight()/2 + 30), (int)(gui.getWidth() / 1.5), 34), 18));

        Image logo = GUITileMap.get(GUIElement.LOGO);
        float aspectRatio = (float) (logo.getWidth() / logo.getHeight());
        MenuTab.MAIN.addButton("1-player", new Button("1 Player", new Rectangle((int)(gui.getWidth() * 1/6), (int)(4*gui.getHeight() * 0.125 / 2 + gui.getHeight() / 1.25 / aspectRatio), (int)(gui.getWidth() / 3 - 5), 34), 18));
        MenuTab.MAIN.addButton("2-players", new Button("2 Players", new Rectangle((int)(gui.getWidth() * 1/2 + 5), (int)(4*gui.getHeight() * 0.125 / 2 + gui.getHeight() / 1.25 / aspectRatio), (int)(gui.getWidth() / 3 - 5), 34), 18));
        MenuTab.MAIN.addButton("controls", new Button("Controls", new Rectangle((int)(gui.getWidth() * 1/6), (int)(4*gui.getHeight() * 0.125 / 2 + gui.getHeight() / 1.25 / aspectRatio + 1.5 * 34), (int)(gui.getWidth() / 1.5), 34), 18));
        MenuTab.MAIN.addButton("highscore", new Button("High Score", new Rectangle((int)(gui.getWidth() * 1/6), (int)(4*gui.getHeight() * 0.125 / 2 + gui.getHeight() / 1.25 / aspectRatio + 3 * 34), (int)(gui.getWidth() / 1.5), 34), 18));
        MenuTab.MAIN.addButton("editor", new Button("Editor", new Rectangle((int)(gui.getWidth() * 1/6), (int)(4*gui.getHeight() * 0.125 / 2 + gui.getHeight() / 1.25 / aspectRatio + 4.5 * 34), (int)(gui.getWidth() / 1.5), 34), 18));
    }

    /**
     * Update the controller and play draw the canvas
     */
    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(renderer);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
