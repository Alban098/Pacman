package modele;

import com.sun.javafx.geom.Vec3d;
import com.sun.javafx.geom.Vec3f;
import javafx.scene.paint.Color;

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

    public static boolean isInside(Point p, Rectangle rect) {
        return p.x >= rect.x && p.x <= rect.x + rect.width && p.y >= rect.y && p.y <= rect.y + rect.height;
    }



    public static boolean isInsideTri(Point pt, Point v1, Point v2, Point v3)
    {
        float d1, d2, d3;
        boolean has_neg, has_pos;

        d1 = (pt.x - v2.x) * (v1.y - v2.y) - (v1.x - v2.x) * (pt.y - v2.y);
        d2 = (pt.x - v3.x) * (v2.y - v3.y) - (v2.x - v3.x) * (pt.y - v3.y);
        d3 = (pt.x - v1.x) * (v3.y - v1.y) - (v3.x - v1.x) * (pt.y - v1.y);

        has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(has_neg && has_pos);
    }
}
