package za.co.wethinkcode.robots.server.world;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.world.Obstacle.ObstacleType;

import static org.junit.jupiter.api.Assertions.*;

public class ObstacleTest {

    @Test
    void testContainsPositionInsideSquare() {
//        2x2 mountain obstacle at (0,0)
        Obstacle obstacle = new Obstacle(ObstacleType.MOUNTAIN, 0, 0, 2);

        // Test points inside the square
        assertTrue(obstacle.containsPosition(0, 0), "Position (0,0) should be inside");
        assertTrue(obstacle.containsPosition(1, -1), "Position (1,-1) should be inside");
        assertTrue(obstacle.containsPosition(2, -2), "Position (2,-2) should be inside");
    }

    @Test
    void testContainsPositionOutsideSquare() {
        // 2x2 mountain obstacle at (0,0)
        Obstacle obstacle = new Obstacle(ObstacleType.MOUNTAIN, 0, 0, 2);

        // Act & Assert: Test points outside the square
        assertFalse(obstacle.containsPosition(3, 0), "Position (3,0) should be outside");
        assertFalse(obstacle.containsPosition(0, 1), "Position (0,1) should be outside");
        assertFalse(obstacle.containsPosition(-1, -3), "Position (-1,-3) should be outside");
    }

    @Test
    void testContainsPositionOnBoundary() {
        // Arrange: Create a 2x2 mountain obstacle at (0,0)
        Obstacle obstacle = new Obstacle(ObstacleType.MOUNTAIN, 0, 0, 2);

        // Test points on the boundary
        assertTrue(obstacle.containsPosition(0, -2), "Position (0,-2) on boundary should be included");
        assertTrue(obstacle.containsPosition(2, 0), "Position (2,0) on boundary should be included");
    }

    @Test
    void testPositionCustomCorners() {
        // mountain obstacle
        Obstacle obstacle = new Obstacle(
                ObstacleType.MOUNTAIN,
                0, 0,   // topLeft
                2, 0,   // topRight
                0, -2,  // bottomLeft
                2, -2   // bottomRight
        );

        assertTrue(obstacle.containsPosition(1, -1), "Position (1,-1) should be inside");
        assertFalse(obstacle.containsPosition(3, 0), "Position (3,0) should be outside");
    }

    @Test
    void testBlocksMovementMountain() {
        //  mountain obstacle
        Obstacle mountain = new Obstacle(ObstacleType.MOUNTAIN, 0, 0, 2);

        assertTrue(mountain.blocksMovement(1, -1), "Mountain should block movement at (1,-1)");
        assertFalse(mountain.blocksMovement(3, 0), "Mountain should not block movement at (3,0)");
    }

    @Test
    void testBlocksMovementLake() {
        // lake obstacle
        Obstacle lake = new Obstacle(ObstacleType.LAKE, 0, 0, 2);

        assertTrue(lake.blocksMovement(1, -1), "Lake should block movement at (1,-1)");
        assertFalse(lake.blocksMovement(3, 0), "Lake should not block movement at (3,0)");
    }

    @Test
    void testBlocksMovementPit() {
        // pit obstacle)
        Obstacle pit = new Obstacle(ObstacleType.PIT, 0, 0, 2);

        assertFalse(pit.blocksMovement(1, -1), "Pit should not block movement at (1,-1)");
        assertFalse(pit.blocksMovement(3, 0), "Pit should not block movement at (3,0)");
    }

    @Test
    void testKillsRobot() {
        Obstacle mountain = new Obstacle(ObstacleType.MOUNTAIN, 0, 0, 2);
        Obstacle lake = new Obstacle(ObstacleType.LAKE, 0, 0, 2);
        Obstacle pit = new Obstacle(ObstacleType.PIT, 0, 0, 2);

        assertFalse(mountain.killsRobot(), "Mountain should not kill robot");
        assertFalse(lake.killsRobot(), "Lake should not kill robot");
        assertTrue(pit.killsRobot(), "Pit should kill robot");
    }


    @Test
    void testObstacleTypeFromString() {
        // Act & Assert
        assertEquals(ObstacleType.MOUNTAIN, ObstacleType.fromString("mountain"));
        assertEquals(ObstacleType.LAKE, ObstacleType.fromString("LAKE"));
        assertEquals(ObstacleType.PIT, ObstacleType.fromString("bottomless pit"));
        assertThrows(IllegalArgumentException.class, () -> ObstacleType.fromString("invalid"), "Invalid obstacle type should throw exception");
        assertThrows(IllegalArgumentException.class, () -> ObstacleType.fromString(null), "Null obstacle type should throw exception");
    }
}