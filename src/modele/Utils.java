package modele;

import java.awt.*;

public class Utils {

    public static int wrap(int val, int min, int max) {
        if (val > max)
            return val - max + min - 1;
        else if (val < min)
            return val + max - min + 1;
        return val;
    }

    public static float getDistSquared(Point p1, Point p2) {
        return (p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y);
    }
}
