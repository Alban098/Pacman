package modele;

import modele.game.Game;
import modele.game.enums.GameState;
import modele.game.enums.MenuTab;
import view.InputController;
import java.util.Observable;


public class Menu extends Observable implements Runnable {

    private InputController inputController;
    private MenuTab tab;
    private boolean closeGameRequested = false;
    private Game game;

    public Menu(Game game) {
        tab = MenuTab.MAIN;
        this.game = game;
    }

    public void setInputController(InputController inputController) {
        this.inputController = inputController;
    }

    public void update() {
        if (tab == MenuTab.CONTROLS) {

        }
        setChanged();
        notifyObservers();
    }

    @Override
    public void run() {
        while (!closeGameRequested) {
            if (game.getGameState() == GameState.MENU_SCREEN)
                update();
            try {
                Thread.sleep(Game.FRAME_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public MenuTab getTab() {
        return tab;
    }

    public void setTab(MenuTab tab) {
        this.tab = tab;
    }

    public synchronized void requestClose() {
        closeGameRequested = true;
    }


}
