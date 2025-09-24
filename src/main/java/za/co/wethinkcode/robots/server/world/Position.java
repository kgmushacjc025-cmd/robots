package za.co.wethinkcode.robots.server.world;

import java.util.List;
import java.util.Objects;

/**
 * Represents a 2D coordinate in the world.
 */
public class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    /**
     * Checks if this position is inside world bounds.
     */
    public boolean isInsideWorld(int width, int height) {
        int halfW = width / 2;
        int halfH = height / 2;
        return x >= -halfW && x <= halfW && y >= -halfH && y <= halfH;
    }

    /**
     * Validates if this position is free from obstacles and other robots.
     */
    public boolean isPositionValid(int width, int height, List<Obstacle> obstacles, List<Robot> robots) {
        int halfW = width / 2;
        int minX = -halfW;
        int maxX = (width % 2 == 0) ? halfW - 1 : halfW;

        int halfH = height / 2;
        int minY = -halfH;
        int maxY = (height % 2 == 0) ? halfH - 1 : halfH;

        if (x < minX || x > maxX || y < minY || y > maxY) return false;

        if (obstacles != null) {
            for (Obstacle o : obstacles) {
                if (o.blocksPosition(x, y) && !o.canWalkThrough()) {
                    return false;
                }
            }
        }

        if (robots != null) {
            for (Robot r : robots) {
                if (!"DEAD".equals(r.getStatus()) && r.getX() == x && r.getY() == y) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position pos = (Position) o;
        return x == pos.x && y == pos.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
