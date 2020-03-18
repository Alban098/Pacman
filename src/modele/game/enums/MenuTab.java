package modele.game.enums;


import modele.game.Button;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum MenuTab {
    MAIN,
    HIGHSCORE_ENTER,
    CONTROLS,
    HIGHSCORE;

    private Map<String, Button> buttons;

    MenuTab() {
        buttons = new HashMap<>();
    }

    /**
     * Add a button to the tab
     * @param name the identifier of the button
     * @param button the button itself
     */
    public void addButton(String name, Button button) {
        buttons.put(name, button);
    }

    /**
     * Return a specific button
     * @param name the identifier of the desired button
     * @return the desired button, null if unknown
     */
    public Button getButton(String name) {
        return buttons.get(name);
    }

    /**
     * Return a set of every button's identifier
     * @return a set of every button identifier
     */
    public Set<String> getButtonList() {
        return buttons.keySet();
    }
}
