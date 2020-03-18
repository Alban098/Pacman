package controller.input;

import controller.audio.AudioController;
import controller.editor.EditorViewController;
import controller.view.ViewController;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import modele.Loader;
import modele.Menu;
import modele.Utils;
import modele.game.Button;
import modele.game.Game;
import modele.game.Score;
import modele.game.enums.GameState;
import modele.game.enums.MenuTab;
import modele.game.enums.Movement;

import java.awt.*;
import java.io.IOException;
import java.util.Map;

public class InputController {

    private KeyCode currentCode;
    private boolean editorLaunched;
    private AudioController audioController;
    private Map<Input, KeyCode> inputsMap;

    public InputController(AudioController audioController) {
        inputsMap = Loader.getInstance().loadConfigs();
        this.audioController = audioController;
    }

    /**
     * Get the key associated to an Input
     * @param input the input to get
     * @return the KeyCode mapped to the Input
     */
    public KeyCode getKey(Input input) {
        return inputsMap.get(input);
    }

    /**
     * Set the pair of Input -> Key
     * @param input the input to be mapped
     * @param keyCode the key associated with the input
     */
    private void setKey(Input input, KeyCode keyCode) {
        inputsMap.put(input, keyCode);
    }

    /**
     * Handle Key and Mouse input and update the game
     * @param kEvent the KeyEvent fired by the frame
     * @param mEvent the MouseEvent fired by the frame
     * @param viewController the ViewController associated to the frame
     */
    public void handleInput(KeyEvent kEvent, MouseEvent mEvent, ViewController viewController) {
        Game game = Game.getInstance();
        switch (game.getGameState()) {
            case GAME_SCREEN:
                if (kEvent != null) {
                    if (!game.isGameStarted() && !game.isPlayerDead()) {
                        if (game.startGame()) {
                            viewController.isDeathAnimPlaying = false;
                            viewController.isDeathAnimFinished = true;
                        }
                    }
                    if (kEvent.getCode().equals(inputsMap.get(Input.UP_P1)))
                        game.setNextPlayerAction(Movement.UP, 0);
                    if (kEvent.getCode().equals(inputsMap.get(Input.DOWN_P1)))
                        game.setNextPlayerAction(Movement.DOWN, 0);
                    if (kEvent.getCode().equals(inputsMap.get(Input.RIGHT_P1)))
                        game.setNextPlayerAction(Movement.RIGHT, 0);
                    if (kEvent.getCode().equals(inputsMap.get(Input.LEFT_P1)))
                        game.setNextPlayerAction(Movement.LEFT, 0);

                    if (kEvent.getCode().equals(inputsMap.get(Input.UP_P2)))
                        game.setNextPlayerAction(Movement.UP, 1);
                    if (kEvent.getCode().equals(inputsMap.get(Input.DOWN_P2)))
                        game.setNextPlayerAction(Movement.DOWN, 1);
                    if (kEvent.getCode().equals(inputsMap.get(Input.RIGHT_P2)))
                        game.setNextPlayerAction(Movement.RIGHT, 1);
                    if (kEvent.getCode().equals(inputsMap.get(Input.LEFT_P2)))
                        game.setNextPlayerAction(Movement.LEFT, 1);

                    if (kEvent.getCode().equals(inputsMap.get(Input.ENTER)))
                        if (game.isGameFinished() && game.isPlayerDead() && viewController.isDeathAnimFinished) {
                            viewController.initButtons();
                            game.runLater(() -> game.setGameState(GameState.MENU_SCREEN));
                            Menu.getInstance().setTab(MenuTab.HIGHSCORE_ENTER);
                        }
                }
                break;
            case MENU_SCREEN:
                switch (Menu.getInstance().getTab()) {
                    case MAIN:
                        if (mEvent != null) {
                            Point mouseCoords = new Point((int) (mEvent.getSceneX() / ViewController.SCALE), (int) (mEvent.getSceneY() / ViewController.SCALE));
                            if (Utils.isInside(mouseCoords, MenuTab.MAIN.getButton("1-player").getHitbox())) {
                                game.runLater(() -> game.setGameState(GameState.GAME_SCREEN));
                                game.setNbPlayer(1);
                                viewController.resetSprites();
                                viewController.whenToStopDeathAnim = System.currentTimeMillis() - 1;
                                viewController.whenToStopScoreAnim = System.currentTimeMillis() - 1;
                                viewController.audioController.canPlayIntro(true);
                            }
                            if (Utils.isInside(mouseCoords, MenuTab.MAIN.getButton("2-players").getHitbox())) {
                                game.runLater(() -> game.setGameState(GameState.GAME_SCREEN));
                                game.setNbPlayer(2);
                                viewController.resetSprites();
                                viewController.whenToStopDeathAnim = System.currentTimeMillis() - 1;
                                viewController.whenToStopScoreAnim = System.currentTimeMillis() - 1;
                                viewController.audioController.canPlayIntro(true);
                            }
                            if (Utils.isInside(mouseCoords, MenuTab.MAIN.getButton("controls").getHitbox()))
                                Menu.getInstance().setTab(MenuTab.CONTROLS);
                            if (Utils.isInside(mouseCoords, MenuTab.MAIN.getButton("highscore").getHitbox()))
                                Menu.getInstance().setTab(MenuTab.HIGHSCORE);
                            if (Utils.isInside(mouseCoords, MenuTab.MAIN.getButton("editor").getHitbox())) {
                                game.setGameState(GameState.LEVEL_EDITOR);
                                if (!editorLaunched) {
                                    Runnable runnable = () -> {
                                        try {
                                            EditorViewController editorController = new EditorViewController();
                                            editorController.setInputController(this);
                                            editorController.start(new Stage());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    };
                                    Platform.runLater(runnable);
                                    editorLaunched = true;
                                }
                            }
                        }
                        break;
                    case CONTROLS:
                        boolean isButtonFocused = false;
                        Input selectedInput = null;
                        for (String id : MenuTab.CONTROLS.getButtonList())
                            if (MenuTab.CONTROLS.getButton(id).isSelected()) {
                                isButtonFocused = true;
                                selectedInput = Input.valueOf(id);
                            }
                        if (mEvent != null) {
                            Point mouseCoords = new Point((int) (mEvent.getSceneX() / ViewController.SCALE), (int) (mEvent.getSceneY() / ViewController.SCALE));
                            if (!isButtonFocused) {
                                if (Utils.isInside(mouseCoords, MenuTab.CONTROLS.getButton("back").getHitbox()))
                                    Menu.getInstance().setTab(MenuTab.MAIN);
                                if (Utils.isInside(mouseCoords, MenuTab.CONTROLS.getButton(Input.UP_P1.toString()).getHitbox())) {
                                    MenuTab.CONTROLS.getButton(Input.UP_P1.toString()).setSelected(true);
                                    currentCode = inputsMap.get(Input.UP_P1);
                                }
                                if (Utils.isInside(mouseCoords, MenuTab.CONTROLS.getButton(Input.DOWN_P1.toString()).getHitbox())) {
                                    MenuTab.CONTROLS.getButton(Input.DOWN_P1.toString()).setSelected(true);
                                    currentCode = inputsMap.get(Input.DOWN_P1);
                                }
                                if (Utils.isInside(mouseCoords, MenuTab.CONTROLS.getButton(Input.RIGHT_P1.toString()).getHitbox())) {
                                    MenuTab.CONTROLS.getButton(Input.RIGHT_P1.toString()).setSelected(true);
                                    currentCode = inputsMap.get(Input.RIGHT_P1);
                                }
                                if (Utils.isInside(mouseCoords, MenuTab.CONTROLS.getButton(Input.LEFT_P1.toString()).getHitbox())) {
                                    MenuTab.CONTROLS.getButton(Input.LEFT_P1.toString()).setSelected(true);
                                    currentCode = inputsMap.get(Input.LEFT_P1);
                                }

                                if (Utils.isInside(mouseCoords, MenuTab.CONTROLS.getButton(Input.UP_P2.toString()).getHitbox())) {
                                    MenuTab.CONTROLS.getButton(Input.UP_P2.toString()).setSelected(true);
                                    currentCode = inputsMap.get(Input.UP_P2);
                                }
                                if (Utils.isInside(mouseCoords, MenuTab.CONTROLS.getButton(Input.DOWN_P2.toString()).getHitbox())) {
                                    MenuTab.CONTROLS.getButton(Input.DOWN_P2.toString()).setSelected(true);
                                    currentCode = inputsMap.get(Input.DOWN_P2);
                                }
                                if (Utils.isInside(mouseCoords, MenuTab.CONTROLS.getButton(Input.RIGHT_P2.toString()).getHitbox())) {
                                    MenuTab.CONTROLS.getButton(Input.RIGHT_P2.toString()).setSelected(true);
                                    currentCode = inputsMap.get(Input.RIGHT_P2);
                                }
                                if (Utils.isInside(mouseCoords, MenuTab.CONTROLS.getButton(Input.LEFT_P2.toString()).getHitbox())) {
                                    MenuTab.CONTROLS.getButton(Input.LEFT_P2.toString()).setSelected(true);
                                    currentCode = inputsMap.get(Input.LEFT_P2);
                                }
                            }
                        }
                        if (kEvent != null) {
                            if (isButtonFocused && selectedInput != null) {
                                if (currentCode == kEvent.getCode() || !inputsMap.containsValue(kEvent.getCode())) {
                                    setKey(selectedInput, kEvent.getCode());
                                    MenuTab.CONTROLS.getButton(selectedInput.toString()).setSelected(false);
                                    MenuTab.CONTROLS.getButton(selectedInput.toString()).setText(kEvent.getCode().getName());
                                    Loader.getInstance().saveConfigs(inputsMap);
                                    currentCode = null;
                                } else
                                    audioController.warning();
                            }
                        }
                        break;
                    case HIGHSCORE:
                        if (mEvent != null) {
                            Point mouseCoords = new Point((int) (mEvent.getSceneX() / ViewController.SCALE), (int) (mEvent.getSceneY() / ViewController.SCALE));
                            if (Utils.isInside(mouseCoords, MenuTab.CONTROLS.getButton("back").getHitbox()))
                                Menu.getInstance().setTab(MenuTab.MAIN);
                        }
                        break;
                    case HIGHSCORE_ENTER:
                        if (kEvent != null) {
                            Button button = MenuTab.HIGHSCORE_ENTER.getButton("name");
                            if ((kEvent.getCode().isLetterKey() || kEvent.getCode().isDigitKey())&& (button.getText().length() < 5))
                                button.setText(button.getText() + kEvent.getText());
                            else if (kEvent.getCode() == KeyCode.BACK_SPACE && button.getText().length() >= 1)
                                button.setText(button.getText().substring(0, button.getText().length() - 1));
                            else if (kEvent.getCode().equals(inputsMap.get(Input.ENTER)) && button.getText().length() > 0) {
                                game.runLater(() -> game.addHighscore(new Score(game.getScoreToSave(), button.getText())));
                                Loader.getInstance().saveHighscore(game.getHighscores());
                                Menu.getInstance().setTab(MenuTab.MAIN);
                            }
                        }
                }
                break;
        }
    }

    /**
     * Set whether or not the Editor Windows is launched
     * @param editorLaunched is the editor windows launched
     */
    public synchronized void setEditorLaunched(boolean editorLaunched) {
        this.editorLaunched = editorLaunched;
    }
}

