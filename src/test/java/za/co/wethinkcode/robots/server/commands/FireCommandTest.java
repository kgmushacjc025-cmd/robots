package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FireCommandTest {

    private World world;
    private Robot shooter;
    private Robot target;

    @BeforeEach
    void setup() {
        world = mock(World.class);
        shooter = mock(Robot.class);
        target = mock(Robot.class);

        when(world.getRobotsInWorld()).thenReturn(List.of(shooter, target));
        when(world.getRobot("Shooter")).thenReturn(shooter);

        when(shooter.getName()).thenReturn("Shooter");
        when(shooter.getShots()).thenReturn(5);
        when(shooter.canFire()).thenReturn(true);
        when(shooter.getX()).thenReturn(5);
        when(shooter.getY()).thenReturn(5);
        when(shooter.getDirection()).thenReturn("NORTH");

        when(target.getName()).thenReturn("Target");
        when(target.getStatus()).thenReturn("ALIVE");
        when(target.getX()).thenReturn(5);
        when(target.getY()).thenReturn(7);
    }

    @Test
    void testFireHitsTarget() {
        when(shooter.getMaxShotDistance()).thenReturn(3);

        FireCommand cmd = new FireCommand("Shooter", JsonNodeFactory.instance.arrayNode(), world);
        JsonNode result = cmd.execute();

        assertEquals("OK", result.get("result").asText());
        assertTrue(result.get("data").get("hit").asBoolean());
        assertEquals("Target", result.get("data").get("target").asText());
        verify(target).damage(anyInt());
        verify(shooter).consumeShots(anyInt());
    }

    @Test
    void testFireMissesTarget() {
        when(shooter.getMaxShotDistance()).thenReturn(1);
        when(target.getX()).thenReturn(10);
        when(target.getY()).thenReturn(10);

        FireCommand cmd = new FireCommand("Shooter", JsonNodeFactory.instance.arrayNode(), world);
        JsonNode result = cmd.execute();

        assertEquals("OK", result.get("result").asText());
        assertFalse(result.get("data").get("hit").asBoolean());
        assertEquals("none", result.get("data").get("target").asText());
        verify(target, never()).damage(anyInt());
        verify(shooter).consumeShots(anyInt());
    }

    @Test
    void testCannotFireNoShots() {
        when(shooter.canFire()).thenReturn(false);

        FireCommand cmd = new FireCommand("Shooter", JsonNodeFactory.instance.arrayNode(), world);
        JsonNode result = cmd.execute();

        assertEquals("ERROR", result.get("result").asText());
        assertTrue(result.get("data").get("message").asText().contains("Cannot fire"));
        verify(shooter, never()).consumeShots(anyInt());
    }

    @Test
    void testNotEnoughShots() {
        when(shooter.getMaxShotDistance()).thenReturn(2); // consumes 4 shots
        when(shooter.getShots()).thenReturn(3);

        FireCommand cmd = new FireCommand("Shooter", JsonNodeFactory.instance.arrayNode(), world);
        JsonNode result = cmd.execute();

        assertEquals("ERROR", result.get("result").asText());
        assertTrue(result.get("data").get("message").asText().contains("Not enough shots"));
        verify(shooter, never()).consumeShots(anyInt());
    }


    @Test
    void testShootUntilTargetDies() {
        when(shooter.getMaxShotDistance()).thenReturn(3);
        when(shooter.getShots()).thenReturn(5);

        doAnswer(invocation -> {
            when(target.getStatus()).thenReturn("DEAD");
            return null;
        }).when(target).damage(anyInt());

        FireCommand cmd = new FireCommand("Shooter", JsonNodeFactory.instance.arrayNode(), world);
        JsonNode result = cmd.execute();

        assertEquals("OK", result.get("result").asText());
        assertTrue(result.get("data").get("hit").asBoolean());
        assertEquals("Target", result.get("data").get("target").asText());
        assertEquals("Hit Target", result.get("data").get("outcome").asText());
        verify(target).damage(anyInt());
        verify(shooter).consumeShots(anyInt());
    }

    @Test
    void testShootContinuesAfterTargetDead() {
        when(shooter.getMaxShotDistance()).thenReturn(3);
        when(shooter.getShots()).thenReturn(5);
        when(target.getStatus()).thenReturn("DEAD");

        FireCommand cmd = new FireCommand("Shooter", JsonNodeFactory.instance.arrayNode(), world);
        JsonNode result = cmd.execute();

        assertEquals("OK", result.get("result").asText());
        assertFalse(result.get("data").get("hit").asBoolean());
        assertEquals("none", result.get("data").get("target").asText());
        verify(target, never()).damage(anyInt());
        verify(shooter).consumeShots(anyInt());
    }
}
