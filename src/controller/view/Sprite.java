package controller.view;

import javafx.scene.image.Image;

import java.util.Map;
import java.util.TreeMap;

public class Sprite {

    private final Map<SpriteID, ImageAtlas> atlasMap;

    public Sprite() {
        this.atlasMap = new TreeMap<>();
    }

    public void addImageAtlas(SpriteID id, int duration, String ... images) {
        ImageAtlas atlas = new ImageAtlas(duration, images);
        atlasMap.put(id, atlas);
    }

    public void startAnimation(SpriteID id) {
        ImageAtlas atlas = atlasMap.get(id);
        if (atlas != null)
            atlas.startAnimation();
    }

    public int getDuration(SpriteID id) {
        ImageAtlas atlas = atlasMap.get(id);
        if (atlas == null)
            return 0;
        return atlas.getDuration();
    }

    public Image getFrame(SpriteID id) {
        ImageAtlas atlas = atlasMap.get(id);
        if (atlas == null)
            return null;
        if (id != SpriteID.NONE)
            atlasMap.put(SpriteID.LAST, atlasMap.get(id));
        return atlas.getImage();
    }
}
