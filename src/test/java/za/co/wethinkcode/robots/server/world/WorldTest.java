package za.co.wethinkcode.robots.server.world;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    private WorldConfig config;
    private World world;

    @BeforeEach
    void setup() {
        // Minimal config with defaultPlacement=false
        config = new WorldConfig(
                10, 5, 3, 2,
                Map.of("Sniper", new int[]{5, 5, 2}),
                List.of() // no obstacles
        );
        world = new World(config);
    }

    @Test
    void testWorldProperties() {
        assertEquals(10, world.worldWidth());
        assertEquals(10, world.worldHeight());
        assertEquals(5, world.getVisibility());
        assertEquals(3, world.getRepairTime());
        assertEquals(2, world.getReloadTime());
        assertEquals(config.getMakes(), world.getMakes());
        assertEquals(0, world.getWorldObstacles().size());
    }

    @Test
    void testAddRobotSuccessfully() {
        Robot robot = new Robot("Hal", "Sniper", 5, 5, 2);
        boolean added = world.addRobot(robot);
        assertTrue(added);
        assertEquals(robot, world.getRobot("Hal"));
        assertTrue(world.getRobotsInWorld().contains(robot));
        assertTrue(robot.getX() >= -world.worldWidth()/2 && robot.getX() <= world.worldWidth()/2);
        assertTrue(robot.getY() >= -world.worldHeight()/2 && robot.getY() <= world.worldHeight()/2);
    }

    @Test
    void testAddRobotDuplicateNameFails() {
        Robot r1 = new Robot("Hal", "Sniper", 5, 5, 2);
        Robot r2 = new Robot("Hal", "Sniper", 5, 5, 2);
        assertTrue(world.addRobot(r1));
        assertFalse(world.addRobot(r2), "Duplicate name should fail");
    }

    @Test
    void testRemoveDeadRobots() {
        Robot alive = new Robot("Alive", "Sniper", 5, 5, 2);
        Robot dead = new Robot("Dead", "Sniper", 5, 5, 2);
        dead.setStatus("DEAD");

        world.addRobot(alive);
        world.addRobot(dead);
        assertEquals(2, world.getRobotsInWorld().size());

        world.removeDeadRobots();
        assertEquals(1, world.getRobotsInWorld().size());
        assertNotNull(world.getRobot("Alive"));
        assertNull(world.getRobot("Dead"));
    }

    @Test
    void testRemoveOneRobot() {
        Robot robot = new Robot("R1", "Sniper", 5, 5, 2);
        world.addRobot(robot);
        world.removeOneRobot("R1");
        assertNull(world.getRobot("R1"));
        assertEquals(0, world.getRobotsInWorld().size());
    }

    @Test
    void testClearRobots() {
        world.addRobot(new Robot("R1", "Sniper",5,5,2));
        world.addRobot(new Robot("R2", "Sniper",5,5,2));
        assertEquals(2, world.getRobotsInWorld().size());

        world.clearRobots();
        assertEquals(0, world.getRobotsInWorld().size());
        assertTrue(world.getRobotNames().isEmpty());
    }

    @Test
    void testGetRobotNames() {
        world.addRobot(new Robot("A", "Sniper",5,5,2));
        world.addRobot(new Robot("B", "Sniper",5,5,2));
        List<String> names = world.getRobotNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("A"));
        assertTrue(names.contains("B"));
    }

    @Test
    void testGetWorldStateJson() {
        Robot r = new Robot("Hal", "Sniper",5,5,2);
        world.addRobot(r);

        JsonNode state = world.getWorldState();
        assertEquals(world.worldWidth(), state.get("width").asInt());
        assertEquals(world.worldHeight(), state.get("height").asInt());
        assertEquals(1, state.get("numRobots").asInt());
        assertTrue(state.get("robots").isArray());

        JsonNode robotNode = state.get("robots").get(0);
        assertEquals("Hal", robotNode.get("name").asText());
        assertEquals(r.getX(), robotNode.get("x").asInt());
        assertEquals(r.getY(), robotNode.get("y").asInt());

        assertEquals(world.getWorldObstacles().size(), state.get("numObstacles").asInt());
        assertTrue(state.get("obstacles").isArray());
    }

    @Test
    void testGetRobotNotFoundReturnsNull() {
        assertNull(world.getRobot("NonExistent"));
    }

    @Test
    void testMultipleRobotsAndMapIntegrity() {
        Robot r1 = new Robot("R1","Sniper",5,5,2);
        Robot r2 = new Robot("R2","Sniper",5,5,2);
        world.addRobot(r1);
        world.addRobot(r2);

        assertEquals(r1, world.getRobot("R1"));
        assertEquals(r2, world.getRobot("R2"));
        assertEquals(2, world.getRobotsInWorld().size());
    }
}
