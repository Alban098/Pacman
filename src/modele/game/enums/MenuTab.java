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

    public void addButton(String name, Button button) {
        buttons.put(name, button);
    }

    public Button getButton(String name) {
        return buttons.get(name);
    }

    public Set<String> getButtonList() {
        return buttons.keySet();
    }
}
