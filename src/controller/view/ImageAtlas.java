package controller.view;


import javafx.scene.image.Image;

public class ImageAtlas {

    private final Image[] frames;
    private final int animationDuration;
    private long animationStart;

    public ImageAtlas(int duration, String ... images) {
        animationDuration = duration;
        frames = new Image[images.length];
        for (int i = 0; i < images.length; i++)
            frames[i] = new Image(images[i]);
        animationStart = System.currentTimeMillis();
    }

    public void startAnimation() {
        animationStart = System.currentTimeMillis();
    }

    public Image getImage() {
        float percent = (System.currentTimeMillis() - animationStart) % animationDuration / (float)animationDuration;
        return frames[(int) (percent * frames.length)];
    }

    public int getDuration() {
        return animationDuration;
    }
}
