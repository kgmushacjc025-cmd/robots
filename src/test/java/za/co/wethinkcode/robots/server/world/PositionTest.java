package za.co.wethinkcode.robots.server.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    private Position pos;

    @BeforeEach
    void setup() {
        pos = new Position(0, 0);
    }

    @Test
    void testGetters() {
        Position p = new Position(5, -3);
        assertEquals(5, p.getX());
        assertEquals(-3, p.getY());
    }

    @Test
    void testInsideWorldTrue() {
        Position p = new Position(1, -1);
        assertTrue(p.isInsideWorld(4, 4)); // width=4,height=4, bounds -2..1
    }

    @Test
    void testInsideWorldFalse() {
        Position p = new Position(3, 0);
        assertFalse(p.isInsideWorld(4, 4));
    }

    @Test
    void testEqualsAndHashCode() {
        Position a = new Position(2, 3);
        Position b = new Position(2, 3);
        Position c = new Position(3, 2);

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    @Test
    void testToString() {
        Position p = new Position(1, -1);
        assertEquals("[1, -1]", p.toString());
    }

    @Test
    void testValidPositionNoObstaclesNoRobots() {
        Position p = new Position(1, 1);
        assertTrue(p.isPositionValid(4, 4, null, null));
    }

    @Test
    void testValidPositionWithObstacleBlocking() {
        Obstacle obstacle = Mockito.mock(Obstacle.class);
        Mockito.when(obstacle.blocksPosition(1, 1)).thenReturn(true);
        Mockito.when(obstacle.canWalkThrough()).thenReturn(false);

        Position p = new Position(1, 1);
        assertFalse(p.isPositionValid(4, 4, List.of(obstacle), null));
    }

    @Test
    void testValidPositionWithObstacleWalkThrough() {
        Obstacle obstacle = Mockito.mock(Obstacle.class);
        Mockito.when(obstacle.blocksPosition(1, 1)).thenReturn(true);
        Mockito.when(obstacle.canWalkThrough()).thenReturn(true);

        Position p = new Position(1, 1);
        assertTrue(p.isPositionValid(4, 4, List.of(obstacle), null));
    }

    @Test
    void testValidPositionWithRobotAlive() {
        Robot robot = Mockito.mock(Robot.class);
        Mockito.when(robot.getX()).thenReturn(1);
        Mockito.when(robot.getY()).thenReturn(1);
        Mockito.when(robot.getStatus()).thenReturn("ALIVE");

        Position p = new Position(1, 1);
        assertFalse(p.isPositionValid(4, 4, null, List.of(robot)));
    }

    @Test
    void testValidPositionWithRobotDead() {
        Robot robot = Mockito.mock(Robot.class);
        Mockito.when(robot.getX()).thenReturn(1);
        Mockito.when(robot.getY()).thenReturn(1);
        Mockito.when(robot.getStatus()).thenReturn("DEAD");

        Position p = new Position(1, 1);
        assertTrue(p.isPositionValid(4, 4, null, List.of(robot)));
    }

    @Test
    void testPositionOutsideBounds() {
        Position p = new Position(3, 0);
        assertFalse(p.isPositionValid(4, 4, null, null));
        Position q = new Position(0, -3);
        assertFalse(q.isPositionValid(4, 4, null, null));
    }
}
