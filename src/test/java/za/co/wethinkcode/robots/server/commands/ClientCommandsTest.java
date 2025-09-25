package za.co.wethinkcode.robots.server.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.world.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClientCommandsTest {

    private World world;
    private Robot testRobot;

    @BeforeEach
    void setUp() {
        // World config with Sniper make
        Map<String, int[]> makes = new HashMap<>();
        makes.put("Sniper", new int[]{15, 5, 3}); // shots, shields, maxShots

        WorldConfig config = new WorldConfig(
                10,      // size
                5,       // visibility
                3,       // repairTime seconds
                5,       // reloadTime seconds
                makes,
                Collections.emptyList() // no obstacles
        );

        world = new World(config);

        // Create a test robot with Sniper stats
        int[] sniperStats = makes.get("Sniper");
        testRobot = new Robot("Robo1", "Sniper", sniperStats[1], sniperStats[0], sniperStats[2]);
        testRobot.setRepairTime(world.getRepairTime());
        testRobot.setReloadTime(world.getReloadTime());

        world.addRobot(testRobot);
    }

    @Test
    void testRepairCommandRestoresShields() {
        testRobot.damage(3); // shields drop from 5 -> 2
        assertEquals(2, testRobot.getShields());

        RepairCommand repair = new RepairCommand("Robo1", world);
        repair.execute();
        assertEquals(testRobot.getMaxShields(), testRobot.getShields());
    }

    @Test
    void testRepairCommandDoesNotExceedMax() {
        assertEquals(testRobot.getMaxShields(), testRobot.getShields());

        RepairCommand repair = new RepairCommand("Robo1", world);
        repair.execute();
        assertEquals(testRobot.getMaxShields(), testRobot.getShields());
    }

    @Test
    void testReloadCommandRestoresShots() {
        testRobot.consumeShots(2); // shots drop from 15 -> 13
        assertEquals(13, testRobot.getShots());

        ReloadCommand reload = new ReloadCommand("Robo1", world);
        reload.execute();
        assertEquals(testRobot.getMaxShots(), testRobot.getShots());
    }


    @Test
    void testRepairAndReloadDeadRobotHasNoEffect() {
        testRobot.setStatus("DEAD");

        int shieldsBefore = testRobot.getShields();
        int shotsBefore = testRobot.getShots();

        RepairCommand repair = new RepairCommand("Robo1", world);
        ReloadCommand reload = new ReloadCommand("Robo1", world);

        repair.execute();
        reload.execute();

        assertEquals(shieldsBefore, testRobot.getShields());
        assertEquals(shotsBefore, testRobot.getShots());
    }

}
