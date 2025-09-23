package za.co.wethinkcode.robots.server.world;

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
            if (value == null) {
                throw new IllegalArgumentException("Obstacle type cannot be null");
            }
            String normalized = value.trim().toLowerCase();
            for (ObstacleType type : values()) {
                if (type.displayName.equalsIgnoreCase(normalized) ||
                        type.name().equalsIgnoreCase(normalized)) {
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

    // Four corners of the square
    private final int topLeftX;
    private final int topLeftY;
    private final int topRightX;
    private final int topRightY;
    private final int bottomLeftX;
    private final int bottomLeftY;
    private final int bottomRightX;
    private final int bottomRightY;

    /**
     * Constructor for square obstacle (given top-left + size).
     */
    public Obstacle(ObstacleType type, int topLeftX, int topLeftY, int size) {
        this(
                type,
                topLeftX, topLeftY,
                topLeftX + size, topLeftY,
                topLeftX, topLeftY - size,
                topLeftX + size, topLeftY - size
        );
    }

    /**
     * Constructor for square obstacle (given all four corners explicitly).
     */
    public Obstacle(ObstacleType type,
                    int topLeftX, int topLeftY,
                    int topRightX, int topRightY,
                    int bottomLeftX, int bottomLeftY,
                    int bottomRightX, int bottomRightY) {

        this.type = type;
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.topRightX = topRightX;
        this.topRightY = topRightY;
        this.bottomLeftX = bottomLeftX;
        this.bottomLeftY = bottomLeftY;
        this.bottomRightX = bottomRightX;
        this.bottomRightY = bottomRightY;

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

    public boolean containsPosition(int x, int y) {
        int left = Math.min(topLeftX, bottomLeftX);
        int right = Math.max(topRightX, bottomRightX);
        int top = Math.max(topLeftY, topRightY);
        int bottom = Math.min(bottomLeftY, bottomRightY);

        return x >= left && x <= right && y >= bottom && y <= top;
    }

    public boolean blocksMovement(int x, int y) {
        return !canWalkThrough && containsPosition(x, y);
    }

    public boolean killsRobot() {
        return canKillYou;
    }

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
