package za.co.wethinkcode.robots.server.world;

import java.util.List;
import java.util.Objects;

public class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }


    public boolean isInsideWorld(int width, int height) {
        return !(this.x < -(width / 2) || this.x > width / 2 ||
                this.y < -(height / 2) || this.y > height / 2);
    }

    public boolean isPositionValid(int width, int height, List<Obstacle> obstacles, List<Robot> robots) {
        // Compute inclusive min/max for a centered grid
        int halfW = width / 2;
        int minX = -halfW;
        int maxX = (width % 2 == 0) ? halfW - 1 : halfW;

        int halfH = height / 2;
        int minY = -halfH;
        int maxY = (height % 2 == 0) ? halfH - 1 : halfH;

        // Out of bounds?
        if (x < minX || x > maxX || y < minY || y > maxY) {
            return false;
        }

        if (obstacles != null) {
            for (Obstacle o : obstacles) {
                if (!o.blocksPosition(x, y)) continue;
                if (!o.canWalkThrough()) {
                    return false;
                }
            }
        }

        // Robots: position is invalid if any robot already occupies it
        if (robots != null) {
            for (Robot r : robots) {
                if (r.getX() == x && r.getY() == y) {
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
        return Objects.hash(x,y);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
