package za.co.wethinkcode.robots.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.wethinkcode.robots.server.world.Obstacle;
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;
import za.co.wethinkcode.robots.server.world.WorldConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    private World world;
    private WorldConfig config;
    private List<Obstacle> obstacles;
    private Map<String, int[]> makes;

    @BeforeEach
    void setUp() {
        // Initialize real obstacles and makes
        obstacles = new ArrayList<>();
        obstacles.add(new Obstacle(Obstacle.ObstacleType.MOUNTAIN, -2, 3, 2));
        makes = new HashMap<>();
        makes.put("testMake", new int[] { 5, 3, 10 }); // shots, shields, maxShots

        // Initialize WorldConfig and World
        config = new WorldConfig(10, makes, obstacles); // 10x10 world
        world = new World(config);
    }

    @Test
    void testConstructor() {
        assertEquals(10, world.worldWidth(), "World width should be 10");
        assertEquals(10, world.worldHeight(), "World height should be 10");
        assertEquals(1, world.getWorldObstacles().size(), "Should have 1 obstacle");
        assertEquals(obstacles, world.getWorldObstacles(), "Obstacles should match config");
        assertEquals(makes, world.getMakes(), "Makes should match config");
        assertTrue(world.getRobotsInWorld().isEmpty(), "Robot list should be empty");
        assertTrue(world.getRobotNames().isEmpty(), "Robot names should be empty");
    }

    @Test
    void testAddRobotSuccess() {
        Robot robot = new Robot("Robot1", "testMake", 3, 5, 10);
        boolean result = world.addRobot(robot);

        assertTrue(result, "Robot should be added successfully");
        assertEquals(1, world.getRobotsInWorld().size(), "Should have 1 robot");
        assertEquals(robot, world.getRobot("Robot1"), "Robot should be retrievable by name");
        assertTrue(new Position(robot.getX(), robot.getY()).isPositionValid(10, 10, obstacles, new ArrayList<>()),
                "Robot position should be valid");
    }

    @Test
    void testAddRobotDuplicateName() {
        Robot robot1 = new Robot("Robot1", "testMake", 3, 5, 10);
        world.addRobot(robot1);

        Robot robot2 = new Robot("Robot1", "testMake", 3, 5, 10);
        boolean result = world.addRobot(robot2);

        assertFalse(result, "Should not add robot with duplicate name");
        assertEquals(1, world.getRobotsInWorld().size(), "Should still have 1 robot");
        assertEquals(robot1, world.getRobot("Robot1"), "Original robot should remain");
    }

    @Test
    void testAddRobotNoValidPosition() {
        // Create a world with obstacles covering all positions
        List<Obstacle> fullObstacles = new ArrayList<>();
        fullObstacles.add(new Obstacle(Obstacle.ObstacleType.MOUNTAIN, -5, 4, 11)); // Covers entire 10x10 world
        WorldConfig fullConfig = new WorldConfig(10, makes, fullObstacles);
        World fullWorld = new World(fullConfig);

        Robot robot = new Robot("Robot1", "testMake", 3, 5, 10);
        boolean result = fullWorld.addRobot(robot);

        assertFalse(result, "Should fail if no valid position is found");
        assertTrue(fullWorld.getRobotsInWorld().isEmpty(), "Robot list should remain empty");
    }



    @Test
    void testGetRobot() {
        Robot robot = new Robot("Robot1", "testMake", 3, 5, 10);
        world.addRobot(robot);

        assertEquals(robot, world.getRobot("Robot1"), "Should return correct robot");
        assertNull(world.getRobot("NonExistent"), "Should return null for non-existent robot");
    }

    @Test
    void testGetRobotNames() {
        Robot robot = new Robot("Robot1", "testMake", 3, 5, 10);
        world.addRobot(robot);

        List<String> names = world.getRobotNames();
        assertEquals(List.of("Robot1"), names, "Should return list with Robot1");
    }

     @Test
     void testRemoveOneRobot() {
         Robot robot = new Robot("Robot1", "testMake", 3, 5, 10);
         world.addRobot(robot);

         world.removeOneRobot("Robot1");
         assertTrue(world.getRobotsInWorld().isEmpty(), "Robot list should be empty");
     }

     @Test
     void testClearRobots(){

         Robot robot = new Robot("Robot", "testMake", 3, 5, 10);
         Robot robot1 = new Robot("Robot1", "testMake", 3, 5, 10);
         Robot robot2 = new Robot("Robot2", "testMake", 3, 5, 10);

         world.addRobot(robot);
         world.addRobot(robot1);
         world.addRobot(robot2);

         world.clearRobots();
         assertTrue(world.getRobotsInWorld().isEmpty(), "Robot list should be empty");
         assertNull(world.getRobot("Robot1"), "Robot should be removed");
     }

    @Test
    void testGetMakes() {
        Map<String, int[]> retrievedMakes = world.getMakes();
        assertEquals(1, retrievedMakes.size(), "Should have 1 make");
        assertArrayEquals(new int[] { 5, 3, 10 }, retrievedMakes.get("testMake"), "Make data should match");
    }

    @Test
    void testGetWorldObstacles() {
        List<Obstacle> retrievedObstacles = world.getWorldObstacles();
        assertEquals(1, retrievedObstacles.size(), "Should have 1 obstacle");
        assertEquals(obstacles.get(0), retrievedObstacles.get(0), "Obstacle should match config");
    }

    @Test
    void testWorldWidthAndHeight() {
        assertEquals(10, world.worldWidth(), "World width should be 10");
        assertEquals(10, world.worldHeight(), "World height should be 10");
    }

    @Test
    void testAddRobotWithDifferentObstacleTypes() {
        // Create a world with different obstacle types
        List<Obstacle> mixedObstacles = new ArrayList<>();
        mixedObstacles.add(new Obstacle(Obstacle.ObstacleType.PIT, -2, 3, 2)); // PIT: canWalkThrough=true
        WorldConfig pitConfig = new WorldConfig(10, makes, mixedObstacles);
        World pitWorld = new World(pitConfig);

        Robot robot = new Robot("Robot1", "testMake", 3, 5, 10);
        boolean result = pitWorld.addRobot(robot);

        assertTrue(result, "Should add robot even with PIT obstacle (canWalkThrough=true)");
        assertEquals(1, pitWorld.getRobotsInWorld().size(), "Should have 1 robot");
    }

     @Test
     void testAddRobotAtOccupiedPosition() {
     Robot robot1 = new Robot("Robot1", "testMake", 3, 5, 10);
     Robot robot2 = new Robot("Robot2", "testMake", 3, 5, 10);

     robot1.setPosition(0,0);
     robot2.setPosition(0,0);

     world.addRobot(robot1);
     boolean result = world.addRobot(robot2);

     assertTrue(result, "Should not add robot at occupied position");



     }
}