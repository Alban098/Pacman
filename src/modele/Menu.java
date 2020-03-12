package modele;

import modele.game.Game;
import modele.game.enums.GameState;
import modele.game.enums.MenuTab;
import controller.input.InputController;
import java.util.Observable;


public class Menu extends Observable implements Runnable {

    private MenuTab tab;
    private boolean closeGameRequested = false;

    private static Menu instance;

    public static Menu getInstance() {
        if (instance == null)
            instance = new Menu();
        return instance;
    }

    public Menu() {
        tab = MenuTab.MAIN;
    }

    public void update() {
        setChanged();
        notifyObservers();
    }

    @Override
    public void run() {
        while (!closeGameRequested) {
            if (Game.getInstance().getGameState() == GameState.MENU_SCREEN)
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
