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

    /**
     * Return an instance of the class
     * @return an instance of the class
     */
    public static Menu getInstance() {
        if (instance == null)
            instance = new Menu();
        return instance;
    }

    public Menu() {
        tab = MenuTab.MAIN;
    }

    /**
     * Notify the views
     */
    public void update() {
        setChanged();
        notifyObservers();
    }

    /**
     * Manage the object and notify the views
     */
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

    /**
     * Return the current MenuTab
     * @return the current MenuTab
     */
    public MenuTab getTab() {
        return tab;
    }

    /**
     * Set the current MenuTab
     * @param tab the new MenuTab
     */
    public void setTab(MenuTab tab) {
        this.tab = tab;
    }

    /**
     * Notify the Menu that it need to stop at the next update
     */
    public synchronized void requestClose() {
        closeGameRequested = true;
    }
}
