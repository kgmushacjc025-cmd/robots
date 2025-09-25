package za.co.wethinkcode.robots.server.world;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObstacleGeneratorTest {

    @Test
    void testObstaclesPerType() {
        int count = ObstacleGenerator.obstaclesPerType(10,10);
        assertEquals((10*10/33)/3, count);
    }

    @Test
    void testGenerateObstacles() {
        List<Obstacle> obstacles = ObstacleGenerator.generate(10,10);
        assertFalse(obstacles.isEmpty());
        long mountains = obstacles.stream().filter(o -> o.getType() == Obstacle.ObstacleType.MOUNTAIN).count();
        long lakes = obstacles.stream().filter(o -> o.getType() == Obstacle.ObstacleType.LAKE).count();
        long pits = obstacles.stream().filter(o -> o.getType() == Obstacle.ObstacleType.PIT).count();
        assertTrue(mountains > 0 && lakes > 0 && pits > 0);
    }

    @Test
    void testCollidesAndOverlap() {
        Obstacle o1 = new Obstacle(Obstacle.ObstacleType.MOUNTAIN, 0,0,2);
        Obstacle o2 = new Obstacle(Obstacle.ObstacleType.MOUNTAIN, 1,0,2);
        Obstacle o3 = new Obstacle(Obstacle.ObstacleType.MOUNTAIN, 3,3,2);

        List<Obstacle> list = new ArrayList<>();
        list.add(o1);
        assertTrue(ObstacleGenerator.collides(o2, list));
        assertFalse(ObstacleGenerator.collides(o3, list));
        assertTrue(ObstacleGenerator.overlap(o1,o2));
        assertFalse(ObstacleGenerator.overlap(o1,o3));
    }

    @Test
    void testRandomCoord() {
        for (int i = 0; i < 100; i++) {
            int coord = ObstacleGenerator.randomCoord(10);
            assertTrue(coord >= -5 && coord < 5);
        }
    }

    @Test
    void testPlaceObstacleAddsWithoutCollision() {
        List<Obstacle> list = new ArrayList<>();
        ObstacleGenerator.placeObstacle(Obstacle.ObstacleType.MOUNTAIN, 10, 10, list, 1);
        assertEquals(1, list.size());
    }
}
