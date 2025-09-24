package za.co.wethinkcode.robots.server.world;

public class Robot {
    private final String name;
    private int x, y;
    private String direction;
    private int shields;
    private int maxShields;          // store original max shields
    private int shots;
    private int maxShots;
    private final int maxShotDistance;
    private int reloadTime;          // seconds until next reload
    private int repairTime;          // seconds until next repair
    private String status;

    public Robot(String name, String make, int shields, int shots, int maxShots) {
        this.name = name;
        this.direction = "NORTH";
        this.shields = shields;
        this.maxShields = shields;
        this.shots = shots;
        this.maxShots = maxShots;

        this.maxShotDistance = switch (maxShots) {
            case 1 -> 5;
            case 2 -> 4;
            case 3 -> 3;
            case 4 -> 2;
            case 5 -> 1;
            default -> 0;
        };

        this.reloadTime = 0;
        this.repairTime = 0;
        this.status = "NORMAL";
    }

    // Consume shots when firing
    public void consumeShots(int count) {
        if (count > 0) {
            shots -= count;
            if (shots < 0) shots = 0;
        }
    }

    // Take damage; if shields hit 0, robot dies
    public void damage(int damage) {
        shields -= damage;
        if (shields <= 0) {
            shields = 0;
            status = "DEAD";
        }
    }

    // Repair shields up to max, pausing for repairTime seconds
    public void repair() {
        if (!"DEAD".equals(status)) {
            try {
                if (repairTime > 0) {
                    Thread.sleep(repairTime * 1000L);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            shields = maxShields;
        }
    }

    // Reload shots, pausing for reloadTime seconds
    public void reload() {
        if (!"DEAD".equals(status)) {
            try {
                if (reloadTime > 0) {
                    Thread.sleep(reloadTime * 1000L);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            shots = maxShots;
        }
    }

    public boolean canFire() {
        return shots > 0 && maxShotDistance > 0 && !"DEAD".equals(status);
    }

    // Getters and setters
    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getDirection() { return direction; }
    public int getShields() { return shields; }
    public int getMaxShields() { return maxShields; }
    public int getShots() { return shots; }
    public int getMaxShots() { return maxShots; }
    public int getMaxShotDistance() { return maxShotDistance; }
    public String getStatus() { return status; }
    public void setDirection(String direction) { this.direction = direction; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setStatus(String status) { this.status = status; }

    // New setters for reload and repair times
    public void setReloadTime(int reloadTime) { this.reloadTime = reloadTime; }
    public void setRepairTime(int repairTime) { this.repairTime = repairTime; }
}
