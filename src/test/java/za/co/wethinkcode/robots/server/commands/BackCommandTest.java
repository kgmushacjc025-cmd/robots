package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.world.*;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackCommandTest {

    private World world;
    private Robot robot;

    @BeforeEach
    void setup() {
        world = mock(World.class);
        robot = mock(Robot.class);

        when(world.getRobotsInWorld()).thenReturn(new ArrayList<>(Collections.singletonList(robot)));
        when(world.getWorldObstacles()).thenReturn(new ArrayList<>());
        when(world.worldWidth()).thenReturn(10);
        when(world.worldHeight()).thenReturn(10);
        when(world.getRobot("R1")).thenReturn(robot);

        when(robot.getName()).thenReturn("R1");
        when(robot.getStatus()).thenReturn("NORMAL");
        when(robot.getX()).thenReturn(0);
        when(robot.getY()).thenReturn(0);
    }

    private ArrayNode stepsArg(String steps) {
        ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        arr.add(steps);
        return arr;
    }

    @Test
    void testBackCommandNorth() {
        when(robot.getDirection()).thenReturn("NORTH");

        BackCommand cmd = new BackCommand("R1", stepsArg("2"), world);
        JsonNode result = cmd.execute();

        assertEquals("OK", result.get("result").asText());
        assertEquals("Moved successfully", result.get("data").get("outcome").asText());
        verify(robot).setPosition(0, -2);
    }

    @Test
    void testBackCommandSouth() {
        when(robot.getDirection()).thenReturn("SOUTH");

        BackCommand cmd = new BackCommand("R1", stepsArg("2"), world);
        JsonNode result = cmd.execute();

        assertEquals("OK", result.get("result").asText());
        assertEquals("Moved successfully", result.get("data").get("outcome").asText());
        verify(robot).setPosition(0, 2);
    }

    @Test
    void testBackCommandEast() {
        when(robot.getDirection()).thenReturn("EAST");

        BackCommand cmd = new BackCommand("R1", stepsArg("3"), world);
        JsonNode result = cmd.execute();

        assertEquals("OK", result.get("result").asText());
        assertEquals("Moved successfully", result.get("data").get("outcome").asText());
        verify(robot).setPosition(-3, 0);
    }

    @Test
    void testBackCommandWest() {
        when(robot.getDirection()).thenReturn("WEST");

        BackCommand cmd = new BackCommand("R1", stepsArg("3"), world);
        JsonNode result = cmd.execute();

        assertEquals("OK", result.get("result").asText());
        assertEquals("Moved successfully", result.get("data").get("outcome").asText());
        verify(robot).setPosition(3, 0);
    }


    @Test
    void testMoveOutsideWorld() {
        when(robot.getDirection()).thenReturn("NORTH");
        when(robot.getX()).thenReturn(0);
        when(robot.getY()).thenReturn(-5); // At bottom edge

        BackCommand cmd = new BackCommand("R1", stepsArg("1"), world);
        JsonNode result = cmd.execute();

        assertEquals("OK", result.get("result").asText());
        assertEquals("Cannot move: would leave the world", result.get("data").get("outcome").asText());
        verify(robot, never()).setPosition(anyInt(), anyInt()); // No movement
    }

    @Test
    void testInvalidStepArgument() {
        when(robot.getDirection()).thenReturn("EAST");

        BackCommand cmd = new BackCommand("R1", stepsArg("-3"), world);
        JsonNode result = cmd.execute();

        assertEquals("error", result.get("result").asText());
        assertTrue(result.get("message").asText().contains("Invalid number of steps"));
        verify(robot, never()).setPosition(anyInt(), anyInt()); // No movement
    }
}