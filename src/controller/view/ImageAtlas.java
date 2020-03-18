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

    /**
     * Initialize the starting time of the animation
     */
    public void startAnimation() {
        animationStart = System.currentTimeMillis();
    }

    /**
     * Return the current Frame the animation
     * @return the correct Image to draw
     */
    public Image getImage() {
        float percent = (System.currentTimeMillis() - animationStart) % animationDuration / (float)animationDuration;
        return frames[(int) (percent * frames.length)];
    }

    /**
     * Return the duration the animation
     * @return the Animation duration
     */
    public int getDuration() {
        return animationDuration;
    }
}
