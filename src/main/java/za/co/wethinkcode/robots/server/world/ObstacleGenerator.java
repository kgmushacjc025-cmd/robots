package za.co.wethinkcode.robots.server.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates obstacles for the world.
 * Ensures obstacles do not overlap and are placed randomly.
 */
public class ObstacleGenerator {
    private static final Random random = new Random();

    /**
     * Calculates the number of obstacles of each type based on world area.
     */
    public static int obstaclesPerType(int width, int height) {
        int area = width * height;
        int total = area / 33;
        return total / 3; // Divide equally among three types
    }

    /**
     * Generates a list of obstacles for the world.
     */
    public static List<Obstacle> generate(int worldWidth, int worldHeight) {
        List<Obstacle> obstacles = new ArrayList<>();
        int count = obstaclesPerType(worldWidth, worldHeight);

        for (int i = 0; i < count; i++) {
            placeObstacle(Obstacle.ObstacleType.MOUNTAIN, worldWidth, worldHeight, obstacles, 1);
            placeObstacle(Obstacle.ObstacleType.LAKE, worldWidth, worldHeight, obstacles, 1);
            placeObstacle(Obstacle.ObstacleType.PIT, worldWidth, worldHeight, obstacles, 1);
        }

        return obstacles;
    }

    /**
     * Attempts to place an obstacle without colliding with existing ones.
     */
    public static void placeObstacle(Obstacle.ObstacleType type, int width, int height,
                                     List<Obstacle> existing, int obstacleSize) {
        int tries = 0;
        while (tries < 1000) {
            int x = randomCoord(width);
            int y = randomCoord(height);

            Obstacle candidate = new Obstacle(type, x, y, obstacleSize);

            if (!collides(candidate, existing)) {
                existing.add(candidate);
                break;
            }
            tries++;
        }
    }

    /**
     * Checks if a new obstacle collides with any existing obstacles.
     */
    public static boolean collides(Obstacle newObstacle, List<Obstacle> obstacles) {
        for (Obstacle existing : obstacles) {
            if (overlap(newObstacle, existing)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if two obstacles overlap.
     */
    public static boolean overlap(Obstacle first, Obstacle second) {
        int firstLeft = Math.min(first.getTopLeftX(), first.getBottomRightX());
        int firstRight = Math.max(first.getTopLeftX(), first.getBottomRightX());
        int firstTop = Math.max(first.getTopLeftY(), first.getBottomRightY());
        int firstBottom = Math.min(first.getTopLeftY(), first.getBottomRightY());

        int secondLeft = Math.min(second.getTopLeftX(), second.getBottomRightX());
        int secondRight = Math.max(second.getTopLeftX(), second.getBottomRightX());
        int secondTop = Math.max(second.getTopLeftY(), second.getBottomRightY());
        int secondBottom = Math.min(second.getTopLeftY(), second.getBottomRightY());

        return !(firstRight < secondLeft || firstLeft > secondRight ||
                firstBottom > secondTop || firstTop < secondBottom);
    }

    /**
     * Generates a random coordinate within world bounds centered at zero.
     */
    public static int randomCoord(int bound) {
        return random.nextInt(bound) - (bound / 2);
    }
}
