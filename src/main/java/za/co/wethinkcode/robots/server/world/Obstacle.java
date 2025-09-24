package za.co.wethinkcode.robots.server.world;

/**
 * Represents an obstacle in the world.
 * Obstacles have a type (mountain, lake, pit) and properties that affect robot movement and visibility.
 * The obstacle occupies a rectangular area defined by four corners.
 */
public class Obstacle {

    public enum ObstacleType {
        MOUNTAIN("mountain"),
        LAKE("lake"),
        PIT("bottomless pit");

        private final String displayName;

        ObstacleType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public static ObstacleType fromString(String value) {
            if (value == null) throw new IllegalArgumentException("Obstacle type cannot be null");
            String normalized = value.trim().toLowerCase();
            for (ObstacleType type : values()) {
                if (type.displayName.equalsIgnoreCase(normalized) || type.name().equalsIgnoreCase(normalized)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown obstacle type: " + value);
        }
    }

    private final ObstacleType type;
    private final boolean canKillYou;
    private final boolean canWalkThrough;
    private final boolean canSeePast;

    private final int topLeftX, topLeftY;
    private final int topRightX, topRightY;
    private final int bottomLeftX, bottomLeftY;
    private final int bottomRightX, bottomRightY;

    /**
     * Creates a square obstacle given the top-left corner and size.
     *
     * @param type     The type of the obstacle.
     * @param topLeftX X coordinate of top-left corner.
     * @param topLeftY Y coordinate of top-left corner.
     * @param size     Length of the sides of the square.
     */
    public Obstacle(ObstacleType type, int topLeftX, int topLeftY, int size) {
        this(type,
                topLeftX, topLeftY,
                topLeftX + size, topLeftY,
                topLeftX, topLeftY - size,
                topLeftX + size, topLeftY - size);
    }

    /**
     * Creates an obstacle given all four corners explicitly.
     *
     * @param type          Type of the obstacle.
     * @param topLeftX      X of top-left corner.
     * @param topLeftY      Y of top-left corner.
     * @param topRightX     X of top-right corner.
     * @param topRightY     Y of top-right corner.
     * @param bottomLeftX   X of bottom-left corner.
     * @param bottomLeftY   Y of bottom-left corner.
     * @param bottomRightX  X of bottom-right corner.
     * @param bottomRightY  Y of bottom-right corner.
     */
    public Obstacle(ObstacleType type,
                    int topLeftX, int topLeftY,
                    int topRightX, int topRightY,
                    int bottomLeftX, int bottomLeftY,
                    int bottomRightX, int bottomRightY) {

        this.type = type;
        this.topLeftX = topLeftX; this.topLeftY = topLeftY;
        this.topRightX = topRightX; this.topRightY = topRightY;
        this.bottomLeftX = bottomLeftX; this.bottomLeftY = bottomLeftY;
        this.bottomRightX = bottomRightX; this.bottomRightY = bottomRightY;

        switch (type) {
            case MOUNTAIN -> {
                this.canKillYou = false;
                this.canWalkThrough = false;
                this.canSeePast = false;
            }
            case LAKE -> {
                this.canKillYou = false;
                this.canWalkThrough = false;
                this.canSeePast = true;
            }
            case PIT -> {
                this.canKillYou = true;
                this.canWalkThrough = true;
                this.canSeePast = true;
            }
            default -> throw new IllegalArgumentException("Unknown obstacle type: " + type);
        }
    }

    /**
     * Checks if a coordinate is inside the obstacle.
     */
    public boolean containsPosition(int x, int y) {
        int left = Math.min(topLeftX, bottomLeftX);
        int right = Math.max(topRightX, bottomRightX);
        int top = Math.max(topLeftY, topRightY);
        int bottom = Math.min(bottomLeftY, bottomRightY);

        return x >= left && x <= right && y >= bottom && y <= top;
    }

    /**
     * Checks if this obstacle blocks movement at a coordinate.
     */
    public boolean blocksMovement(int x, int y) {
        return !canWalkThrough && containsPosition(x, y);
    }

    /**
     * Checks if the obstacle kills a robot standing on it.
     */
    public boolean killsRobot() {
        return canKillYou;
    }

    /**
     * Checks if the obstacle occupies a given position.
     */
    public boolean blocksPosition(int x, int y) {
        return containsPosition(x, y);
    }

    // Getters
    public ObstacleType getType() { return type; }
    public boolean canKillYou() { return canKillYou; }
    public boolean canWalkThrough() { return canWalkThrough; }
    public boolean canSeePast() { return canSeePast; }
    public int getTopLeftX() { return topLeftX; }
    public int getTopLeftY() { return topLeftY; }
    public int getTopRightX() { return topRightX; }
    public int getTopRightY() { return topRightY; }
    public int getBottomLeftX() { return bottomLeftX; }
    public int getBottomLeftY() { return bottomLeftY; }
    public int getBottomRightX() { return bottomRightX; }
    public int getBottomRightY() { return bottomRightY; }
}
