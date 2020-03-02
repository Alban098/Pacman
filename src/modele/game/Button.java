package modele.game;

import java.awt.*;

public class Button {

    private String text;
    private Rectangle hitbox;
    private boolean isSelected = false;
    private int fontSize;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getFontSize() {
        return fontSize;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Button(String text, Rectangle hitbox, int fontSize) {
        this.text = text;
        this.hitbox = hitbox;
        this.fontSize = fontSize;
    }
}
