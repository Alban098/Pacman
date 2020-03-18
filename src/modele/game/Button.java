package modele.game;

import java.awt.*;

public class Button {

    private String text;
    private Rectangle hitbox;
    private boolean isSelected = false;
    private int fontSize;

    public Button(String text, Rectangle hitbox, int fontSize) {
        this.text = text;
        this.hitbox = hitbox;
        this.fontSize = fontSize;
    }

    /**
     * Return the text of the button
     * @return the text of the button
     */
    public String getText() {
        return text;
    }

    /**
     * Set the text of the button
     * @param text the new text of the button
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Get the button's font size
     * @return the button's font size
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Get the bounding box of the button
     * @return a Rectangle representing the button's bounding box
     */
    public Rectangle getHitbox() {
        return hitbox;
    }

    /**
     * return whether or not the button is currently selected
     * Useful in the input editor
     * @return is the button selected
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Set the button's selected state
     * @param selected does the button need to be selected
     */
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
