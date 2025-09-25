package za.co.wethinkcode.robots.server.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RobotTest {

    private Robot robot;

    @BeforeEach
    void setup() {
        robot = new Robot("Hal", "Sniper", 5, 10, 3);
    }

    @Test
    void testInitialValues() {
        assertEquals("Hal", robot.getName());
        assertEquals("NORTH", robot.getDirection());
        assertEquals(5, robot.getShields());
        assertEquals(5, robot.getMaxShields());
        assertEquals(10, robot.getShots());
        assertEquals(3, robot.getMaxShots());
        assertEquals(3, robot.getMaxShotDistance());
        assertEquals("NORMAL", robot.getStatus());
    }

    @Test
    void testConsumeShotsNormal() {
        robot.consumeShots(2);
        assertEquals(8, robot.getShots());
    }

    @Test
    void testConsumeShotsExceed() {
        robot.consumeShots(20);
        assertEquals(0, robot.getShots());
    }

    @Test
    void testDamageNormal() {
        robot.damage(3);
        assertEquals(2, robot.getShields());
        assertEquals("NORMAL", robot.getStatus());
    }

    @Test
    void testDamageKill() {
        robot.damage(10);
        assertEquals(0, robot.getShields());
        assertEquals("DEAD", robot.getStatus());
    }

    @Test
    void testRepairWhenAlive() {
        robot.damage(3);
        robot.setRepairTime(0); // skip delay
        robot.repair();
        assertEquals(robot.getMaxShields(), robot.getShields());
        assertEquals("NORMAL", robot.getStatus());
    }

    @Test
    void testRepairWhenDead() {
        robot.damage(10);
        robot.setRepairTime(0); // skip delay
        robot.repair();
        assertEquals(0, robot.getShields()); // dead cannot repair
        assertEquals("DEAD", robot.getStatus());
    }

    @Test
    void testReloadWhenAlive() {
        robot.consumeShots(5);
        robot.setReloadTime(0); // skip delay
        robot.reload();
        assertEquals(robot.getMaxShots(), robot.getShots());
    }

    @Test
    void testReloadWhenDead() {
        robot.damage(10);
        robot.setReloadTime(0); // skip delay
        robot.reload();
        assertEquals(10, robot.getShots()); // shots do not reset for dead
        assertEquals("DEAD", robot.getStatus());
    }

    @Test
    void testCanFire() {
        assertTrue(robot.canFire());
        robot.consumeShots(robot.getShots());
        assertFalse(robot.canFire());
        Robot deadRobot = new Robot("R2D2", "Tank", 5, 5, 1);
        deadRobot.setStatus("DEAD");
        assertFalse(deadRobot.canFire());
    }

    @Test
    void testSettersAndPosition() {
        robot.setDirection("EAST");
        assertEquals("EAST", robot.getDirection());

        robot.setPosition(2, 3);
        assertEquals(2, robot.getX());
        assertEquals(3, robot.getY());

        robot.setStatus("CUSTOM");
        assertEquals("CUSTOM", robot.getStatus());

    }
}
