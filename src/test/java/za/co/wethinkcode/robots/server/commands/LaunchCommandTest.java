package za.co.wethinkcode.robots.server.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.world.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LaunchCommandTest {

    private World world;

    @BeforeEach
    void setup() {
        world = new World(new WorldConfig(
                5, 5, 1, 1,
                Map.of("Sniper", new int[]{10, 5, 3}),
                List.of()
        ));
    }

    @Test
    void launchRobotSuccessfully() {
        Robot robot = new Robot("R1", "Sniper", 5, 3, 3);
        boolean added = world.addRobot(robot);
        assertTrue(added);
        assertEquals(robot, world.getRobot("R1"));
        assertNotNull(robot.getX());
        assertNotNull(robot.getY());
    }

    @Test
    void cannotLaunchRobotWithDuplicateName() {
        Robot r1 = new Robot("R1", "Sniper", 5, 3, 3);
        Robot r2 = new Robot("R1", "Sniper", 5, 3, 3);
        assertTrue(world.addRobot(r1));
        assertFalse(world.addRobot(r2));
    }

    @Test
    void cannotLaunchRobotIfWorldFull() {
        World smallWorld = new World(new WorldConfig(
                1, 1, 1, 1,
                Map.of("Sniper", new int[]{10, 5, 3}),
                List.of()
        ));
        Robot r1 = new Robot("R1", "Sniper", 5, 3, 3);
        Robot r2 = new Robot("R2", "Sniper", 5, 3, 3);
        assertTrue(smallWorld.addRobot(r1));
        assertFalse(smallWorld.addRobot(r2));
    }
    
}
