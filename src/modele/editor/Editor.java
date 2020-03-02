package modele.editor;

import modele.game.Game;

import java.util.Observable;


public class Editor extends Observable implements Runnable {

    private boolean closeGameRequested = false;

    public void update() {
        setChanged();
        notifyObservers();
    }

    @Override
    public void run() {
        while (!closeGameRequested) {
            update();
            try {
                Thread.sleep(Game.FRAME_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void requestClose() {
        closeGameRequested = true;
    }


}
