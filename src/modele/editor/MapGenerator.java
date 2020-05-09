package modele.editor;

import modele.game.entities.StaticEntity;
import modele.game.enums.Movement;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MapGenerator {

    private int[][] states;
    private Map<Integer, Shape> shapes;
    private int tileMapSizeX;
    private int tileMapSizeY;
    private int shapeMapSizeX;
    private int shapeMapSizeY;

    public MapGenerator() {
        shapes = new HashMap<>();
    }

    /**
     * <pre>
     * Generate a Map usable by the Game of size (x, y)
     * It will first generate the left side of a Tetris like grid with a fixed tile representing the Ghost's starting position
     * __________________
     * |  |___  |     |  |
     * |   __|  |_____|  |
     * |__|  |__|  |_____|
     * |   __|   __|     | <- Ghost
     * |__|  |__|  |_____| <- starting
     * |  |_____|________|
     * |  |___  |___  |__|
     * |_____|_____|_____|
     * The grid is mirrored to generate a symmetric map and the shapes are scaled by 2 to avoid 0 thickness Paths and Walls
     * The edges are then converted to Paths and the inside of the Shapes to Walls
     * This generate a set of path with no dead ends
     * </pre>
     * @param x width of the map
     * @param y height of the map
     * @param movementMap Map to populate
     */
    public void generateMap(int x, int y, Map<Point, StaticEntity> movementMap) {
        tileMapSizeX = x;
        tileMapSizeY = y;
        int nbShapes = shapeFill();
        scaleShapes();
        symmetric(++nbShapes);
        constructMap(movementMap);
    }

    /**
     * Generate list of Tetris like shapes filling a (X/2; Y) grid
     * @return the number a Shape generated
     */
    private int shapeFill() {
        shapeMapSizeX = (tileMapSizeX + 1) / 4;
        shapeMapSizeY = tileMapSizeY / 2;
        int nextId = 1;
        states = new int[shapeMapSizeX][shapeMapSizeY];
        shapes = new HashMap<>();
        /* Place the ghost's starting block */
        states[shapeMapSizeX - 1][shapeMapSizeY /2] = nextId;
        states[shapeMapSizeX - 1][shapeMapSizeY /2 - 1] = nextId;
        states[shapeMapSizeX - 2][shapeMapSizeY /2] = nextId;
        states[shapeMapSizeX - 2][shapeMapSizeY /2 - 1] = nextId;
        shapes.put(nextId++, new Shape());

        List<Point> remaining;
        do {
            /* Get the remaining tiles to fill */
            remaining = new ArrayList<>();
            for (int i = 0; i < shapeMapSizeX; i++) {
                for (int j = 0; j < shapeMapSizeY; j++) {
                    if (states[i][j] == 0)
                        remaining.add(new Point(i, j));
                }
            }

            /* Picking a random empty tile */
            Point pos = remaining.get(new Random().nextInt(remaining.size()));
            Point current = new Point(pos.x, pos.y);
            int id = nextId++;
            shapes.put(id, new Shape());
            /* Generate a shape of variable length (2 - 4)*/
            for (int i = 0; i < new Random().nextInt(2) + 2; i++) {
                remaining.remove(current);
                states[current.x][current.y] = id;
                ArrayList<Movement> legal = legalMoves(current);
                /* If the shape can't extend anymore we break */
                if (legal.size() == 0) {
                    /* If the shape is to small we scrap it */
                    if (i < 2)
                        shapes.remove(id);
                    break;
                }
                /* we extend the shape */
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

        /* We generate the edges of the shapes*/
        for (int i = 0; i < shapeMapSizeX; i++) {
            for (int j = 0; j < shapeMapSizeY; j++) {
                Shape shape = shapes.get(states[i][j]);
                if (shape != null) {
                    if (j == 0 || states[i][j-1] != states[i][j]) shape.addEdge(new Edge(new Point(i, j), new Point(i+1, j)));
                    if (i == shapeMapSizeX - 1 || states[i+1][j] != states[i][j]) shape.addEdge(new Edge(new Point(i+1, j+1), new Point(i+1, j)));
                    if (j == shapeMapSizeY - 1 || states[i][j+1] != states[i][j]) shape.addEdge(new Edge(new Point(i, j+1), new Point(i+1, j+1)));
                    if (i == 0 || states[i-1][j] != states[i][j]) shape.addEdge(new Edge(new Point(i, j), new Point(i, j+1)));
                }
            }
        }

        /* Removing the edges at the symmetry axe and avoiding dead ends*/
        for (int i = 0; i < shapeMapSizeY; i++) {
            Shape shape = shapes.get(states[shapeMapSizeX -1][i]);
            if (shape != null && (i == shapeMapSizeY /2 || i == shapeMapSizeY /2 - 1 || states[shapeMapSizeX -2][i] == states[shapeMapSizeX -1][i]))
                shape.removeEdge(new Edge(new Point(shapeMapSizeX, i+1), new Point(shapeMapSizeX, i)));
        }
        return nextId;
    }

    /**
     * Return whether or not the tile at a direction is available
     * @param dir the direction to look at
     * @param pos the current position
     * @return is the move possible
     */
    private boolean canMove(Movement dir, Point pos) {
        if (pos.x < 0 || pos.x >= shapeMapSizeX || pos.y < 0 || pos.y >= shapeMapSizeY)
            return false;
        switch (dir) {
            case UP:
                return pos.y + 1 < shapeMapSizeY && states[pos.x][pos.y+1] == 0;
            case DOWN:
                return pos.y - 1 >= 0 && states[pos.x][pos.y-1] == 0;
            case LEFT:
                return pos.x - 1 >= 0 && states[pos.x-1][pos.y] == 0;
            case RIGHT:
                return pos.x + 1 < shapeMapSizeX && states[pos.x+1][pos.y] == 0;
        }
        return false;
    }

    /**
     * Return a list of possible moves from a position on the Shape Grid
     * @param pos the current position
     * @return list of possible moves
     */
    private ArrayList<Movement> legalMoves(Point pos) {
        ArrayList<Movement> legalMoves = new ArrayList<>();
        for (Movement move : Movement.values())
            if (canMove(move, pos))
                legalMoves.add(move);
        return legalMoves;
    }

    /**
     * Generate the symmetric of the current Shape list
     * @param startId the first available ID for the next Shape
     */
    private void symmetric(int startId) {
        List<Shape> sym = new ArrayList<>();
        for (int shape : shapes.keySet()) {
            Shape newShape = new Shape();
            for (Edge e : shapes.get(shape).getEdges()) {
                Point start = new Point(tileMapSizeX - (tileMapSizeX %2 == 0 ? 2 : 1) - e.start.x, e.start.y);
                Point end = new Point(tileMapSizeX - (tileMapSizeX %2 == 0 ? 2 : 1) - e.end.x, e.end.y);
                newShape.addEdge(new Edge(start, end));
            }
            sym.add(newShape);
        }
        for (Shape shape : sym)
            shapes.put(startId++, shape);
    }

    /**
     * Generate a usable Map from the list of Shapes previously generated
     * And add the essential Key Point :
     * Player's spawn point
     * Ghost's spawn point
     * Ghost's starting target
     * Ghost's home gate
     * @param movementMap the map to populate
     */
    private void constructMap(Map<Point, StaticEntity> movementMap) {
        movementMap.clear();
        /* Cast the shapes' shadows on a map */
        boolean[][] grid = new boolean[tileMapSizeX][tileMapSizeY];
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
        /* Populating the map using the casted shadow of the shapes */
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j])
                    movementMap.put(new Point(i + 2, j + 4), StaticEntity.GUM);
                else
                    movementMap.put(new Point(i + 2, j + 4), StaticEntity.WALL);
            }
        }
        /* Applying a wall around the map and an empty ring around those walls */
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

        /* Placing the key point on the map */
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


        /* Generating random portals around the edges of the map */
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

    /**
     * Scale the Shape map by 2 to simplify the conversion into a usable map
     */
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
