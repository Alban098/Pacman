package modele;

import java.awt.*;

public class Utils {

    /**
     * Wrap a value between 2 values
     * @param val the value to wrap
     * @param min the lower bound, included
     * @param max the upper bound, also included
     * @return the wrapped value of val
     */
    public static int wrap(int val, int min, int max) {
        if (val > max)
            return val - max + min - 1;
        else if (val < min)
            return val + max - min + 1;
        return val;
    }

    /**
     * Return the distance squared between 2 points
     * @param p1 the 1st point
     * @param p2 the 2nd point
     * @return the squared distance between p1 and p2
     */
    public static float getDistSquared(Point p1, Point p2) {
        return (p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y);
    }

    /**
     * Determinate if a Point is inside a Rectangle
     * @param p the point to check
     * @param rect the Rectangle
     * @return is p inside the rect
     */
    public static boolean isInside(Point p, Rectangle rect) {
        return p.x >= rect.x && p.x <= rect.x + rect.width && p.y >= rect.y && p.y <= rect.y + rect.height;
    }

    /**
     * Determinate if a Point is inside a Triangle
     * @param p the Point to check
     * @param v1 the 1st vertex of the Triangle
     * @param v2 the 2nd vertex of the Triangle
     * @param v3 the 3rd vertex of the Triangle
     * @return is p inside the triangle made of v1, v2 and v3
     */
    public static boolean isInsideTri(Point p, Point v1, Point v2, Point v3) {
        float d1, d2, d3;
        boolean has_neg, has_pos;

        d1 = (p.x - v2.x) * (v1.y - v2.y) - (v1.x - v2.x) * (p.y - v2.y);
        d2 = (p.x - v3.x) * (v2.y - v3.y) - (v2.x - v3.x) * (p.y - v3.y);
        d3 = (p.x - v1.x) * (v3.y - v1.y) - (v3.x - v1.x) * (p.y - v1.y);

        has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(has_neg && has_pos);
    }
}
