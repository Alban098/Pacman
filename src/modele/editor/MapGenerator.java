package modele.editor;

import modele.game.entities.StaticEntity;
import modele.game.enums.Movement;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MapGenerator {
    int next = 6;
    float[][] states;
    Map<Integer, Shape> shapes;
    int outputX;
    int outputY;
    int sizeX;
    int sizeY;

    public MapGenerator() {
        shapes = new HashMap<>();
    }

    public void generateMap(int x, int y, Map<Point, StaticEntity> movementMap) {
        outputX = x;
        outputY = y;
        shapeFill(x, y);
        scaleShapes();
        symetric();
        constructMap(movementMap);
    }

    private void shapeFill(int x, int y) {
        sizeX = (x + 1)/4;
        sizeY = y/2;
        next = 6;
        states = new float[sizeX][sizeY];
        shapes = new HashMap<>();
        states[sizeX - 1][sizeY/2] = 5;
        states[sizeX - 1][sizeY/2 - 1] = 5;
        states[sizeX - 2][sizeY/2] = 5;
        states[sizeX - 2][sizeY/2 - 1] = 5;
        shapes.put(5, new Shape());

        List<Point> remaining;
        do {
            remaining = new ArrayList<>();
            for (int i = 0; i < sizeX; i++) {
                for (int j = 0; j < sizeY; j++) {
                    if (states[i][j] == 0)
                        remaining.add(new Point(i, j));
                }
            }
            Point pos = remaining.get(new Random().nextInt(remaining.size()));
            Point current = new Point(pos.x, pos.y);
            int id = next++;
            shapes.put(id, new Shape());
            for (int i = 0; i < new Random().nextInt(2) + 2; i++) {
                remaining.remove(current);
                states[current.x][current.y] = id;
                ArrayList<Movement> legal = legalMoves(current);
                if (legal.size() == 0) {
                    if (i < 2)
                        shapes.remove(id);
                    break;
                }
                switch (legal.get(new Random().nextInt(legal.size()))) {
                    case UP:
                        current.y++;
                        break;
                    case DOWN:
                        current.y--;
                        break;
                    case LEFT:
                        current.x--;
                        break;
                    case RIGHT:
                        current.x++;
                        break;
                }
            }
        } while (remaining.size() > 0);
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                Shape shape = shapes.get((int)states[i][j]);
                if (shape != null) {
                    if (j == 0 || states[i][j-1] != states[i][j]) shape.addEdge(new Edge(new Point(i, j), new Point(i+1, j)));
                    if (i == sizeX - 1 || states[i+1][j] != states[i][j]) shape.addEdge(new Edge(new Point(i+1, j+1), new Point(i+1, j)));
                    if (j == sizeY - 1 || states[i][j+1] != states[i][j]) shape.addEdge(new Edge(new Point(i, j+1), new Point(i+1, j+1)));
                    if (i == 0 || states[i-1][j] != states[i][j]) shape.addEdge(new Edge(new Point(i, j), new Point(i, j+1)));
                }
            }
        }
        for (int i = 0; i < sizeY; i++) {
            Shape shape = shapes.get((int)states[sizeX-1][i]);
            if (shape != null && (i == sizeY/2 || i == sizeY/2 - 1 || states[sizeX-2][i] == states[sizeX-1][i]))
                shape.removeEdge(new Edge(new Point(sizeX, i+1), new Point(sizeX, i)));
        }
    }

    private boolean canMove(Movement dir, Point pos) {
        if (pos.x < 0 || pos.x >= sizeX || pos.y < 0 || pos.y >= sizeY)
            return false;
        switch (dir) {
            case UP:
                return pos.y + 1 < sizeY && states[pos.x][pos.y+1] == 0;
            case DOWN:
                return pos.y - 1 >= 0 && states[pos.x][pos.y-1] == 0;
            case LEFT:
                return pos.x - 1 >= 0 && states[pos.x-1][pos.y] == 0;
            case RIGHT:
                return pos.x + 1 < sizeX && states[pos.x+1][pos.y] == 0;
        }
        return false;
    }

    private ArrayList<Movement> legalMoves(Point pos) {
        ArrayList<Movement> legalMoves = new ArrayList<>();
        for (Movement move : Movement.values())
            if (canMove(move, pos))
                legalMoves.add(move);
        return legalMoves;
    }

    public void symetric() {
        List<Shape> sym = new ArrayList<>();
        for (int shape : shapes.keySet()) {
            Shape newShape = new Shape();
            for (Edge e : shapes.get(shape).getEdges()) {
                Point start = new Point(outputX - (outputX%2 == 0 ? 2 : 1) - e.start.x, e.start.y);
                Point end = new Point(outputX - (outputX%2 == 0 ? 2 : 1) - e.end.x, e.end.y);
                newShape.addEdge(new Edge(start, end));
            }
            sym.add(newShape);
        }
        for (Shape shape : sym)
            shapes.put(next++, shape);
    }

    public void constructMap(Map<Point, StaticEntity> movementMap) {
        movementMap.clear();
        boolean[][] grid = new boolean[outputX][outputY];
        for (int shape : shapes.keySet()) {
            for (Edge e : shapes.get(shape).getEdges()) {
                int dirX = (e.end.x == e.start.x ? 0 : Math.abs(e.end.x - e.start.x)/(e.end.x - e.start.x));
                int dirY = (e.end.y == e.start.y ? 0 : Math.abs(e.end.y - e.start.y)/(e.end.y - e.start.y));
                Point cur = new Point(e.start.x, e.start.y);
                grid[e.end.x][e.end.y] = true;
                while (!cur.equals(e.end)) {
                    grid[cur.x][cur.y] = true;
                    cur.x += dirX;
                    cur.y += dirY;
                }
            }
        }
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j])
                    movementMap.put(new Point(i + 2, j + 4), StaticEntity.GUM);
                else
                    movementMap.put(new Point(i + 2, j + 4), StaticEntity.WALL);
            }
        }
        for (int i = 1; i < grid.length + 3; i++) {
            movementMap.put(new Point(i, grid[0].length + 4), StaticEntity.WALL);
            movementMap.put(new Point(i, 3), StaticEntity.WALL);
        }
        for (int i = 3; i < grid[0].length + 5; i++) {
            movementMap.put(new Point(grid.length + 2, i), StaticEntity.WALL);
            movementMap.put(new Point(1, i), StaticEntity.WALL);
        }
        for (int i = 0; i < grid.length + 4; i++) {
            movementMap.put(new Point(i, grid[0].length + 5), StaticEntity.EMPTY);
            movementMap.put(new Point(i, 2), StaticEntity.EMPTY);
        }
        for (int i = 2; i < grid[0].length + 6; i++) {
            movementMap.put(new Point(grid.length + 3, i), StaticEntity.EMPTY);
            movementMap.put(new Point(0, i), StaticEntity.EMPTY);
        }
        Point center = new Point (grid.length/2 + 2, grid[0].length / 2 + 3);
        if ((grid[0].length + 2) / 2 % 2 == 1)
            center.y++;
        movementMap.put(new Point(center.x, center.y - 2), StaticEntity.GHOST_HOME);
        movementMap.put(new Point(center.x, center.y - 1), StaticEntity.GATE);
        movementMap.put(new Point(center.x, center.y ), StaticEntity.GHOST_SPAWN);
        movementMap.put(new Point(center.x, center.y + 2), StaticEntity.PLAYER_SPAWN);
        movementMap.put(new Point(center.x - 1, center.y ), StaticEntity.EMPTY);
        movementMap.put(new Point(center.x + 1, center.y ), StaticEntity.EMPTY);

        if (movementMap.get(new Point(center.x - 3, center.y)) == StaticEntity.WALL)
            movementMap.put(new Point(center.x - 2, center.y ), StaticEntity.EMPTY);
        if (movementMap.get(new Point(center.x + 3, center.y)) == StaticEntity.WALL)
            movementMap.put(new Point(center.x + 2, center.y ), StaticEntity.EMPTY);

        int horizontalPortals = grid[0].length / 25 + 1;
        int verticalPortals = grid.length / 25 + 1;

        for (int i = 0; i < horizontalPortals; i++) {
            int width = grid[0].length / horizontalPortals;
            int start = i * width;
            int candidate = new Random().nextInt(width) + start;
            if (movementMap.get(new Point(2, candidate)) == StaticEntity.GUM && movementMap.get(new Point(grid.length + 1, candidate)) == StaticEntity.GUM) {
                movementMap.put(new Point(0, candidate - 1), StaticEntity.WALL);
                movementMap.put(new Point(1, candidate), StaticEntity.EMPTY);
                movementMap.put(new Point(0, candidate + 1), StaticEntity.WALL);
                movementMap.put(new Point(grid.length + 3, candidate - 1), StaticEntity.WALL);
                movementMap.put(new Point(grid.length + 2, candidate), StaticEntity.EMPTY);
                movementMap.put(new Point(grid.length + 3, candidate + 1), StaticEntity.WALL);
            }
        }
        for (int i = 0; i < verticalPortals; i++) {
            int width = grid.length / verticalPortals;
            int start = i * width;
            int candidate = new Random().nextInt(width) + start;
            if (movementMap.get(new Point(candidate, 4)) == StaticEntity.GUM && movementMap.get(new Point(candidate, grid[0].length + 3)) == StaticEntity.GUM) {
                movementMap.put(new Point(candidate - 1, 2), StaticEntity.WALL);
                movementMap.put(new Point(candidate, 3), StaticEntity.EMPTY);
                movementMap.put(new Point(candidate + 1, 2), StaticEntity.WALL);
                movementMap.put(new Point(candidate - 1, grid[0].length + 5), StaticEntity.WALL);
                movementMap.put(new Point(candidate, grid[0].length + 4), StaticEntity.EMPTY);
                movementMap.put(new Point(candidate + 1, grid[0].length + 5), StaticEntity.WALL);
            }
        }
    }

    private void scaleShapes() {
        for (int i : shapes.keySet()) {
            shapes.get(i).scale(2);
        }
    }
}

class Shape {

    private List<Edge> edges;

    public Shape() {
        edges = new ArrayList<>();
    }

    public void addEdge(Edge e) {
        edges.add(e);
    }

    public void removeEdge(Edge e) {
        edges.remove(e);
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void scale(int i) {
        for (Edge e : edges) {
            e.start.x *= i;
            e.start.y *= i;
            e.end.x *= i;
            e.end.y *= i;

        }
    }
}

class Edge {
    public Point start;
    public Point end;

    public Edge(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Edge))
            return false;
        Edge e = (Edge)o;
        return e.start.equals(start) && e.end.equals(end);
    }
}
