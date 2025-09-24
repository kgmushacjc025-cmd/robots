package za.co.wethinkcode.robots.server.world;

/**
 * Represents a robot in the world.
 */
public class Robot {
    private final String name;
    private int x, y;
    private String direction;
    private int shields;
    private int shots;
    private int maxShots;
    private final int maxShotDistance;
    private String status;

    public Robot(String name, String make, int shields, int shots, int maxShots) {
        this.name = name;
        this.direction = "NORTH";
        this.shields = shields;
        this.shots = shots;
        this.maxShots = maxShots;

        // Map maxShots to maxShotDistance
        this.maxShotDistance = switch (maxShots) {
            case 1 -> 5;
            case 2 -> 4;
            case 3 -> 3;
            case 4 -> 2;
            case 5 -> 1;
            default -> 0; // no shooting capability
        };

        this.status = "NORMAL";
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void consumeShots(int count) {
        if (count > 0) {
            shots -= count;
            if (shots < 0) shots = 0;
        }
    }

    public void damage(int shotsFired) {
        if (shields > 0) {
            shields -= shotsFired;
            if (shields < 0) shields = 0;
        }
        if (shields <= 0) {
            status = "DEAD";
        }
    }

    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getDirection() { return direction; }
    public int getShields() { return shields; }
    public int getShots() { return shots; }
    public String getStatus() { return status; }
    public int getMaxShotDistance() { return maxShotDistance; }
    public int getMaxShots() { return maxShots; }

    public void setDirection(String dir) { this.direction = dir; }
    public void setStatus(String newStatus) { this.status = newStatus; }

    public Position getPosition() {
        return new Position(x, y);
    }

    public boolean canFire() {
        return shots > 0 && maxShotDistance > 0 && !"DEAD".equals(status);
    }
}
