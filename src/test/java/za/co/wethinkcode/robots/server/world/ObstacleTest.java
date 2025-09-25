package za.co.wethinkcode.robots.server.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObstacleTest {

    @Test
    void testSquareObstacleConstructor() {
        Obstacle o = new Obstacle(Obstacle.ObstacleType.MOUNTAIN, 0, 0, 2);
        assertEquals(0, o.getTopLeftX());
        assertEquals(0, o.getTopLeftY());
        assertEquals(2, o.getTopRightX());
        assertEquals(0, o.getTopRightY());
        assertEquals(0, o.getBottomLeftX());
        assertEquals(-2, o.getBottomLeftY());
        assertEquals(2, o.getBottomRightX());
        assertEquals(-2, o.getBottomRightY());
        assertFalse(o.canWalkThrough());
        assertFalse(o.canKillYou());
        assertFalse(o.canSeePast());
    }

    @Test
    void testContainsPosition() {
        Obstacle o = new Obstacle(Obstacle.ObstacleType.MOUNTAIN, 0,0,2);
        assertTrue(o.containsPosition(1, -1));
        assertFalse(o.containsPosition(3, 0));
        assertFalse(o.containsPosition(0, -3));
    }

    @Test
    void testBlocksMovementAndPosition() {
        Obstacle mountain = new Obstacle(Obstacle.ObstacleType.MOUNTAIN, 0,0,2);
        Obstacle pit = new Obstacle(Obstacle.ObstacleType.PIT, 0,0,2);
        assertTrue(mountain.blocksMovement(1,-1));
        assertFalse(pit.blocksMovement(1,-1));
        assertTrue(mountain.blocksPosition(1,-1));
        assertTrue(pit.blocksPosition(1,-1));
    }

    @Test
    void testKillsRobot() {
        Obstacle pit = new Obstacle(Obstacle.ObstacleType.PIT, 0,0,1);
        Obstacle mountain = new Obstacle(Obstacle.ObstacleType.MOUNTAIN, 0,0,1);
        assertTrue(pit.killsRobot());
        assertFalse(mountain.killsRobot());
    }

    @Test
    void testFromString() {
        assertEquals(Obstacle.ObstacleType.MOUNTAIN, Obstacle.ObstacleType.fromString("mountain"));
        assertEquals(Obstacle.ObstacleType.LAKE, Obstacle.ObstacleType.fromString("LAKE"));
        assertEquals(Obstacle.ObstacleType.PIT, Obstacle.ObstacleType.fromString("bottomless pit"));
        assertThrows(IllegalArgumentException.class, () -> Obstacle.ObstacleType.fromString("unknown"));
        assertThrows(IllegalArgumentException.class, () -> Obstacle.ObstacleType.fromString(null));
    }
}
