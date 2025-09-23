package za.co.wethinkcode.robots.server.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ObstacleGeneratorTest {

    private Random mockRandom;

    @BeforeEach
    void setUp() throws Exception {
        mockRandom = mock(Random.class);
        Field randomField = ObstacleGenerator.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(null, mockRandom);
    }

    @Test
    void testObstaclesPerType() {
        assertEquals(1, ObstacleGenerator.obstaclesPerType(10, 10), "10x10 world should have 1 obstacle per type");
        assertEquals(6, ObstacleGenerator.obstaclesPerType(25, 25), "25x25 world should have 6 obstacles per type");
    }

    @Test
    void testGenerateObstacles() {
        when(mockRandom.nextInt(10)).thenReturn(2, 4, 6, 2, 4, 6);
        List<Obstacle> obstacles = ObstacleGenerator.generate(10, 10);

        assertEquals(3, obstacles.size(), "Should generate 3 obstacles for 10x10 world");
        long mountainCount = obstacles.stream().filter(o -> o.getType() == Obstacle.ObstacleType.MOUNTAIN).count();
        long lakeCount = obstacles.stream().filter(o -> o.getType() == Obstacle.ObstacleType.LAKE).count();
        long pitCount = obstacles.stream().filter(o -> o.getType() == Obstacle.ObstacleType.PIT).count();
        assertEquals(1, mountainCount, "Should have 1 MOUNTAIN obstacle");
        assertEquals(1, lakeCount, "Should have 1 LAKE obstacle");
        assertEquals(1, pitCount, "Should have 1 PIT obstacle");

        // no overlaps
        for (int i = 0; i < obstacles.size(); i++) {
            for (int j = i + 1; j < obstacles.size(); j++) {
                assertFalse(ObstacleGenerator.overlap(obstacles.get(i), obstacles.get(j)), "Obstacles should not overlap");
            }
        }
    }

    @Test
    void testPlaceObstacleSuccess() {
        when(mockRandom.nextInt(10)).thenReturn(2);
        List<Obstacle> existing = new ArrayList<>();

        ObstacleGenerator.placeObstacle(Obstacle.ObstacleType.MOUNTAIN, 10, 10, existing, 2);

        assertEquals(1, existing.size(), "Should place one obstacle");
        Obstacle obstacle = existing.get(0);
        assertEquals(Obstacle.ObstacleType.MOUNTAIN, obstacle.getType());
        assertEquals(-3, obstacle.getTopLeftX());
        assertEquals(-3, obstacle.getTopLeftY());
    }

    @Test
    void testPlaceObstacleMaxTries() {
        when(mockRandom.nextInt(10)).thenReturn(2);
        List<Obstacle> existing = new ArrayList<>();
        existing.add(new Obstacle(Obstacle.ObstacleType.MOUNTAIN, -3, -3, 2));

        ObstacleGenerator.placeObstacle(Obstacle.ObstacleType.LAKE, 10, 10, existing, 2);

        assertEquals(1, existing.size(), "Should not place obstacle after 1000 collision tries");
    }

    @Test
    void testCollidesTrue() {
        List<Obstacle> existing = new ArrayList<>();
        Obstacle existingObstacle = new Obstacle(Obstacle.ObstacleType.MOUNTAIN, 0, 0, 2);
        existing.add(existingObstacle);
        Obstacle newObstacle = new Obstacle(Obstacle.ObstacleType.LAKE, 1, -1, 2);
        assertTrue(ObstacleGenerator.collides(newObstacle, existing), "Should detect collision");
    }

    @Test
    void testCollidesFalse() {
        List<Obstacle> existing = new ArrayList<>();
        Obstacle existingObstacle = new Obstacle(Obstacle.ObstacleType.MOUNTAIN, 0, 0, 2);
        existing.add(existingObstacle);
        Obstacle newObstacle = new Obstacle(Obstacle.ObstacleType.LAKE, 5, 5, 2);
        assertFalse(ObstacleGenerator.collides(newObstacle, existing), "Should not detect collision");
    }

    @Test
    void testOverlapTrue() {
        Obstacle first = new Obstacle(Obstacle.ObstacleType.MOUNTAIN, 0, 0, 2);
        Obstacle second = new Obstacle(Obstacle.ObstacleType.LAKE, 1, -1, 2);
        assertTrue(ObstacleGenerator.overlap(first, second), "Obstacles should overlap");
    }

    @Test
    void testOverlapFalse() {
        Obstacle first = new Obstacle(Obstacle.ObstacleType.MOUNTAIN, 0, 0, 2);
        Obstacle second = new Obstacle(Obstacle.ObstacleType.LAKE, 5, 5, 2);
        assertFalse(ObstacleGenerator.overlap(first, second), "Obstacles should not overlap");
    }

    @Test
    void testRandomCoord() {
        when(mockRandom.nextInt(10)).thenReturn(0, 5, 9);
        assertEquals(-5, ObstacleGenerator.randomCoord(10));
        assertEquals(0, ObstacleGenerator.randomCoord(10));
        assertEquals(4, ObstacleGenerator.randomCoord(10));
    }
}
