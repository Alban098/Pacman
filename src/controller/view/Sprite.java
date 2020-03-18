package controller.view;

import javafx.scene.image.Image;

import java.util.Map;
import java.util.TreeMap;

public class Sprite {

    private final Map<SpriteID, ImageAtlas> atlasMap;

    public Sprite() {
        this.atlasMap = new TreeMap<>();
    }

    /**
     * Add a new Animation to the sprite
     * @param id the SpriteID characterizing the animation
     * @param duration the animation duration
     * @param images a list of images of the animation
     */
    public void addImageAtlas(SpriteID id, int duration, String ... images) {
        ImageAtlas atlas = new ImageAtlas(duration, images);
        atlasMap.put(id, atlas);
    }

    /**
     * Initialize the starting time of an animation
     * @param id the SpriteID characterizing the animation
     */
    public void startAnimation(SpriteID id) {
        ImageAtlas atlas = atlasMap.get(id);
        if (atlas != null)
            atlas.startAnimation();
    }

    /**
     * Return the duration of a Sprite animation
     * @param id the SpriteID characterizing the animation
     * @return the Animation duration
     */
    public int getDuration(SpriteID id) {
        ImageAtlas atlas = atlasMap.get(id);
        if (atlas == null)
            return 0;
        return atlas.getDuration();
    }

    /**
     * Return the current Frame of a Sprite animation
     * @param id the SpriteID characterizing the animation
     * @return the correct Image to draw
     */
    public Image getFrame(SpriteID id) {
        ImageAtlas atlas = atlasMap.get(id);
        if (atlas == null)
            return null;
        if (id != SpriteID.NONE)
            atlasMap.put(SpriteID.LAST, atlasMap.get(id));
        return atlas.getImage();
    }
}
