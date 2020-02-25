/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import modele.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import modele.entities.EntityGhost;
import modele.entities.EntityPlayer;
import modele.entities.MoveableEntity;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;

public class ViewController extends Application implements Observer {

    private static final byte MASK_WALL_RIGHT = 0b1000;
    private static final byte MASK_WALL_UP =    0b0100;
    private static final byte MASK_WALL_LEFT =  0b0010;
    private static final byte MASK_WALL_DOWN =  0b0001;

    Map<MoveableEntity, Sprite> foregroundSpriteMap;
    Map<StaticEntity, Image> backgroundTileMap;
    Map<Byte, Image> wallTileMap;
    Map<GUIElement, Image> GUITileMap;

    Canvas foreground;
    Canvas background;
    Canvas gui;

    Game game;
    AudioController audioController;

    long whenToStopDeathAnim;
    boolean isDeathAnimPlaying = false;
    boolean isDeathAnimFinished = true;

    long whenToStopScoreAnim;
    boolean isScoreAnimPlaying = false;
    int dynamicScore;
    Point dynamicScorePos;

    private Runnable renderer;

    @Override
    public void start(Stage primaryStage) {

        game = new Game("map.xml");
        audioController = new AudioController(game);

        renderer = () -> {
            primaryStage.setWidth(15*game.getSizeX() + 16);
            primaryStage.setHeight(15*game.getSizeY() + 39);

            drawBackground();
            drawForeground();
            drawGUI();
        };

        backgroundTileMap = new HashMap<>();
        foregroundSpriteMap = new HashMap<>();
        wallTileMap = new HashMap<>();
        GUITileMap = new HashMap<>();

        foreground = new Canvas(15*game.getSizeX(), 15*game.getSizeY());
        background = new Canvas(15*game.getSizeX(), 15*game.getSizeY());
        gui = new Canvas(15*game.getSizeX(), 15*game.getSizeY());

        StackPane root = new StackPane();
        root.getChildren().add(background);
        root.getChildren().add(foreground);
        root.getChildren().add(gui);

        Scene scene = new Scene(root, 15*game.getSizeX(), 15*game.getSizeY());

        primaryStage.setTitle("Beta 1.6");
        primaryStage.setOnCloseRequest(we -> {
            game.requestClose();
            System.exit(0);
        });
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setResizable(false);
        primaryStage.show();

        loadSprites();

        game.addObserver(this);
        new Thread(game).start();

        root.setOnKeyPressed(event -> {
            if (!game.isGameStarted() && !game.isPlayerDead()) {
                if (game.startGame()) {
                    isDeathAnimPlaying = false;
                    isDeathAnimFinished = true;
                }
            }
            if (event.getCode().equals(KeyCode.UP))
                game.setNextPlayerAction(Movement.UP);
            if (event.getCode().equals(KeyCode.DOWN))
                game.setNextPlayerAction(Movement.DOWN);
            if (event.getCode().equals(KeyCode.RIGHT))
                game.setNextPlayerAction(Movement.RIGHT);
            if (event.getCode().equals(KeyCode.LEFT))
                game.setNextPlayerAction(Movement.LEFT);
        });
        root.requestFocus();
    }

    private void loadSprites() {
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
        background.setWidth(15*game.getSizeX());
        background.setHeight(15*game.getSizeY());
        final GraphicsContext gc = background.getGraphicsContext2D();
        for (int i = 0; i < game.getSizeX(); i++) {
            for (int j = 0; j < game.getSizeY(); j++) {
                Point pos = new Point(i, j);
                if (game.getTileType(pos) == StaticEntity.WALL) {
                    gc.drawImage(wallTileMap.get(getWallMask(pos)), 15 * pos.x, 15 * pos.y);
                } else {
                    gc.drawImage(backgroundTileMap.get(game.getTileType(pos)), 15 * pos.x, 15 * pos.y);
                }
            }
        }
    }

    private void drawForeground() {
        foreground.setWidth(15*game.getSizeX());
        foreground.setHeight(15*game.getSizeY());
        final GraphicsContext gc = foreground.getGraphicsContext2D();
        gc.clearRect(0, 0, foreground.getWidth(), foreground.getHeight());
        if (!game.isPlayerDead()) {
            audioController.canPlayIntro(false);
            drawSprite(game.getGhost(GhostName.BLINKY), gc);
            drawSprite(game.getGhost(GhostName.INKY), gc);
            drawSprite(game.getGhost(GhostName.PINKY), gc);
            drawSprite(game.getGhost(GhostName.CLYDE), gc);
            drawSprite(game.getPlayer(), gc);
        } else {
            if (!isDeathAnimPlaying) {
                isDeathAnimPlaying = true;
                isDeathAnimFinished = false;
                Sprite sprite = foregroundSpriteMap.get(game.getPlayer());
                sprite.startAnimation(SpriteID.DEATH);
                whenToStopDeathAnim = System.currentTimeMillis() + sprite.getDuration(SpriteID.DEATH);

            }
            if (System.currentTimeMillis() <= whenToStopDeathAnim) {
                Point pos = game.getPosition(game.getPlayer());
                gc.drawImage(foregroundSpriteMap.get(game.getPlayer()).getFrame(SpriteID.DEATH), 15 * pos.x, 15 * pos.y);
            } else {
                game.resetPlayer();
                audioController.canPlayIntro(true);
                isDeathAnimFinished = true;
                if (!(game.isGameFinished() && game.isPlayerDead())) {
                    foregroundSpriteMap.get(game.getPlayer()).getFrame(SpriteID.UP);
                    foregroundSpriteMap.get(game.getGhost(GhostName.BLINKY)).getFrame(SpriteID.UP);
                    foregroundSpriteMap.get(game.getGhost(GhostName.CLYDE)).getFrame(SpriteID.UP);
                    foregroundSpriteMap.get(game.getGhost(GhostName.INKY)).getFrame(SpriteID.UP);
                    foregroundSpriteMap.get(game.getGhost(GhostName.PINKY)).getFrame(SpriteID.UP);
                    drawSprite(game.getGhost(GhostName.BLINKY), gc);
                    drawSprite(game.getGhost(GhostName.INKY), gc);
                    drawSprite(game.getGhost(GhostName.PINKY), gc);
                    drawSprite(game.getGhost(GhostName.CLYDE), gc);
                    drawSprite(game.getPlayer(), gc);
                }
            }
        }
    }

    private void drawGUI() {
        gui.setWidth(15*game.getSizeX());
        gui.setHeight(15*game.getSizeY());
        final GraphicsContext gc = gui.getGraphicsContext2D();
        gc.clearRect(0, 0, gui.getWidth(), gui.getHeight());
        gc.setFill(Color.gray(.2f));
        gc.fillRect(0, 0, gui.getWidth(), 31);

        gc.setFill(Color.WHITE);
        gc.fillText("Level", 15, 13);
        gc.fillText("Score", 15, 13+15);


        for (int i = (int) Math.log10(game.getTotalScore() + 1); i >= 0; i--) {
            int digit = (int) (game.getTotalScore() / Math.pow(10, (int)Math.log10(game.getTotalScore()) -  i)) % 10;
            drawDigit(digit, new Point(9*(i + 5) + 3, 20), gc);
        }
        for (int i = (int) Math.log10(game.getLevel()); i >= 0; i--) {
            int digit = (int) (game.getLevel() / Math.pow(10, (int)Math.log10(game.getLevel()) -  i)) % 10;
            drawDigit(digit, new Point(9*(i + 5), 5), gc);
        }
        for (int i = 0; i < game.getLives(); i++) {
            gc.drawImage(GUITileMap.get(GUIElement.LIVE), gui.getWidth() - 15*(i+2), 15);
        }

        if (!game.isGameStarted() && isDeathAnimFinished && !game.isGameFinished()) {
            gc.drawImage(GUITileMap.get(GUIElement.READY), 15*(game.getSizeX()/2 - 2), 15*(game.getSizeY()/2 + 2), 5*15, 15);
        }
        if (game.isGameFinished() && game.isPlayerDead() && isDeathAnimFinished) {
            gc.drawImage(GUITileMap.get(GUIElement.GAME_OVER), 15*(game.getSizeX()/2 - 3), 15*(game.getSizeY()/2 + 2), 7*15, 15);
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
                    gc.drawImage(GUITileMap.get(GUIElement.SCORE_200), 15 * dynamicScorePos.x, 15 * dynamicScorePos.y);
                    break;
                case 400:
                    gc.drawImage(GUITileMap.get(GUIElement.SCORE_400), 15 * dynamicScorePos.x, 15 * dynamicScorePos.y);
                    break;
                case 800:
                    gc.drawImage(GUITileMap.get(GUIElement.SCORE_800), 15 * dynamicScorePos.x, 15 * dynamicScorePos.y);
                    break;
                case 1600:
                    gc.drawImage(GUITileMap.get(GUIElement.SCORE_1600), 15 * dynamicScorePos.x, 15 * dynamicScorePos.y);
                    break;
                case -100:
                    gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_100), 15 * dynamicScorePos.x, 15 * dynamicScorePos.y);
                    break;
                case -300:
                    gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_300), 15 * dynamicScorePos.x, 15 * dynamicScorePos.y);
                    break;
                case -500:
                    gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_500), 15 * dynamicScorePos.x, 15 * dynamicScorePos.y);
                    break;
                case -700:
                    gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_700), 15 * dynamicScorePos.x, 15 * dynamicScorePos.y);
                    break;
                case -1000:
                    gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_1000), 15 * dynamicScorePos.x, 15 * dynamicScorePos.y);
                    break;
                case -2000:
                    gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_2000), 15 * dynamicScorePos.x, 15 * dynamicScorePos.y);
                    break;
                case -3000:
                    gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_3000), 15 * dynamicScorePos.x, 15 * dynamicScorePos.y);
                    break;
                case -5000:
                    gc.drawImage(GUITileMap.get(GUIElement.SCORE_FRUIT_5000), 15 * dynamicScorePos.x, 15 * dynamicScorePos.y);
                    break;
                default:
            }
            if (System.currentTimeMillis() >= whenToStopScoreAnim) {
                isScoreAnimPlaying = false;
                dynamicScore = 0;
            }
        }
    }



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

    private void drawSprite(MoveableEntity entity, GraphicsContext gc) {
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
                        gc.drawImage(sprite.getFrame(SpriteID.EATEN_UP), 15 * lastPos.x, 15 * (lastPos.y - animationPercent));
                        break;
                    case DOWN:
                        lastPos.y--;
                        gc.drawImage(sprite.getFrame(SpriteID.EATEN_DOWN), 15 * lastPos.x, 15 * (lastPos.y + animationPercent));
                        break;
                    case LEFT:
                        lastPos.x++;
                        gc.drawImage(sprite.getFrame(SpriteID.EATEN_LEFT), 15 * (lastPos.x - animationPercent), 15 * lastPos.y);
                        break;
                    case RIGHT:
                        lastPos.x--;
                        gc.drawImage(sprite.getFrame(SpriteID.EATEN_RIGHT), 15 * (lastPos.x + animationPercent), 15 * lastPos.y);
                        break;
                    default:
                        gc.drawImage(sprite.getFrame(SpriteID.LAST), 15 * pos.x, 15 * pos.y);
                        break;
                }
                return;
            } else if (((EntityGhost) entity).getState() == GhostState.FRIGHTENED) {
                switch (dir) {
                    case UP:
                        lastPos.y++;
                        gc.drawImage(sprite.getFrame(SpriteID.FRIGHTENED), 15 * lastPos.x, 15 * (lastPos.y - animationPercent));
                        break;
                    case DOWN:
                        lastPos.y--;
                        gc.drawImage(sprite.getFrame(SpriteID.FRIGHTENED), 15 * lastPos.x, 15 * (lastPos.y + animationPercent));
                        break;
                    case LEFT:
                        lastPos.x++;
                        gc.drawImage(sprite.getFrame(SpriteID.FRIGHTENED), 15 * (lastPos.x - animationPercent), 15 * lastPos.y);
                        break;
                    case RIGHT:
                        lastPos.x--;
                        gc.drawImage(sprite.getFrame(SpriteID.FRIGHTENED), 15 * (lastPos.x + animationPercent), 15 * lastPos.y);
                        break;
                    default:
                        gc.drawImage(sprite.getFrame(SpriteID.FRIGHTENED), 15 * pos.x, 15 * pos.y);
                        break;
                }
                return;
            }
        }
        switch (dir) {
            case UP:
                lastPos.y++;
                gc.drawImage(sprite.getFrame(SpriteID.UP), 15 * lastPos.x, 15 * (lastPos.y - animationPercent));
                break;
            case DOWN:
                lastPos.y--;
                gc.drawImage(sprite.getFrame(SpriteID.DOWN), 15 * lastPos.x, 15 * (lastPos.y + animationPercent));
                break;
            case LEFT:
                lastPos.x++;
                gc.drawImage(sprite.getFrame(SpriteID.LEFT), 15 * (lastPos.x - animationPercent), 15 * lastPos.y);
                break;
            case RIGHT:
                lastPos.x--;
                gc.drawImage(sprite.getFrame(SpriteID.RIGHT), 15 * (lastPos.x + animationPercent), 15 * lastPos.y);
                break;
            default:
                gc.drawImage(sprite.getFrame(SpriteID.LAST), 15 * pos.x, 15 * pos.y);
                break;
        }
    }

    private byte getWallMask(Point pos) {
        byte mask = 0b0000;
        mask |= (game.getTileType(Movement.UP, pos) == StaticEntity.WALL ? MASK_WALL_UP : 0);
        mask |= (game.getTileType(Movement.DOWN, pos) == StaticEntity.WALL ? MASK_WALL_DOWN : 0);
        mask |= (game.getTileType(Movement.RIGHT, pos) == StaticEntity.WALL ? MASK_WALL_RIGHT : 0);
        mask |= (game.getTileType(Movement.LEFT, pos) == StaticEntity.WALL ? MASK_WALL_LEFT : 0);
        return mask;
    }


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
