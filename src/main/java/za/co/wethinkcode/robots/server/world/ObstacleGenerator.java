package za.co.wethinkcode.robots.server.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ObstacleGenerator {
    private static Random random = new Random();

    public static int obstaclesPerType(int width, int height) {
        int area = width * height;
        int total = area / 33;
        return total / 3;
    }

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

    public static boolean collides(Obstacle newObstacle, List<Obstacle> obstacles) {
        for (Obstacle existing : obstacles) {
            if (overlap(newObstacle, existing)) {
                return true;
            }
        }
        return false;
    }

    public static boolean overlap(Obstacle firstObstacle, Obstacle secondObstacle) {
        int firstLeftEdge = Math.min(firstObstacle.getTopLeftX(), firstObstacle.getBottomRightX());
        int firstRightEdge = Math.max(firstObstacle.getTopLeftX(), firstObstacle.getBottomRightX());
        int firstTopEdge = Math.max(firstObstacle.getTopLeftY(), firstObstacle.getBottomRightY());
        int firstBottomEdge = Math.min(firstObstacle.getTopLeftY(), firstObstacle.getBottomRightY());

        int secondLeftEdge = Math.min(secondObstacle.getTopLeftX(), secondObstacle.getBottomRightX());
        int secondRightEdge = Math.max(secondObstacle.getTopLeftX(), secondObstacle.getBottomRightX());
        int secondTopEdge = Math.max(secondObstacle.getTopLeftY(), secondObstacle.getBottomRightY());
        int secondBottomEdge = Math.min(secondObstacle.getTopLeftY(), secondObstacle.getBottomRightY());

        return !(firstRightEdge < secondLeftEdge ||
                firstLeftEdge > secondRightEdge ||
                firstBottomEdge > secondTopEdge ||
                firstTopEdge < secondBottomEdge);
    }

    public static int randomCoord(int bound) {
        return random.nextInt(bound) - (bound / 2);
    }
}
