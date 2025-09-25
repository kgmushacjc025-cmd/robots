package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.world.Obstacle;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ForwardCommandTest {

    private World world;
    private Robot robot;

    @BeforeEach
    void setup() {
        world = mock(World.class);
        robot = mock(Robot.class);

        when(robot.getName()).thenReturn("R1");
        when(world.getRobot("R1")).thenReturn(robot);

        when(world.getWorldObstacles()).thenReturn(List.of());
        when(world.getRobotsInWorld()).thenReturn(List.of(robot));
        when(world.worldWidth()).thenReturn(20);
        when(world.worldHeight()).thenReturn(20);
    }

    @Test
    void moveNorth() {
        when(robot.getX()).thenReturn(5);
        when(robot.getY()).thenReturn(5);
        when(robot.getDirection()).thenReturn("NORTH");

        ArrayNode args = JsonNodeFactory.instance.arrayNode().add(1);
        ForwardCommand cmd = new ForwardCommand("R1", args, world);
        var result = cmd.execute();

        assertEquals("Moved successfully", result.get("data").get("outcome").asText());
    }

    @Test
    void moveSouth() {
        when(robot.getX()).thenReturn(5);
        when(robot.getY()).thenReturn(5);
        when(robot.getDirection()).thenReturn("SOUTH");

        ArrayNode args = JsonNodeFactory.instance.arrayNode().add(1);
        ForwardCommand cmd = new ForwardCommand("R1", args, world);
        var result = cmd.execute();

        assertEquals("Moved successfully", result.get("data").get("outcome").asText());
    }

    @Test
    void moveEast() {
        when(robot.getX()).thenReturn(5);
        when(robot.getY()).thenReturn(5);
        when(robot.getDirection()).thenReturn("EAST");

        ArrayNode args = JsonNodeFactory.instance.arrayNode().add(1);
        ForwardCommand cmd = new ForwardCommand("R1", args, world);
        var result = cmd.execute();

        assertEquals("Moved successfully", result.get("data").get("outcome").asText());
    }

    @Test
    void moveWest() {
        when(robot.getX()).thenReturn(5);
        when(robot.getY()).thenReturn(5);
        when(robot.getDirection()).thenReturn("WEST");

        ArrayNode args = JsonNodeFactory.instance.arrayNode().add(1);
        ForwardCommand cmd = new ForwardCommand("R1", args, world);
        var result = cmd.execute();

        assertEquals("Moved successfully", result.get("data").get("outcome").asText());
    }


    @Test
    void blockedByRobot() {
        Robot other = mock(Robot.class);
        when(other.getName()).thenReturn("R2");
        when(other.getStatus()).thenReturn("ALIVE");
        when(other.getX()).thenReturn(5);
        when(other.getY()).thenReturn(6); // in front

        when(world.getRobotsInWorld()).thenReturn(List.of(robot, other));

        when(robot.getX()).thenReturn(5);
        when(robot.getY()).thenReturn(5);
        when(robot.getDirection()).thenReturn("NORTH");

        ArrayNode args = JsonNodeFactory.instance.arrayNode().add(1);
        ForwardCommand cmd = new ForwardCommand("R1", args, world);
        var result = cmd.execute();

        assertEquals("Blocked by another robot", result.get("data").get("outcome").asText());
    }

}
