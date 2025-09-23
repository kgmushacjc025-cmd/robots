package za.co.wethinkcode.robots.server.commands;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StateNodeTest {

    private World mockWorld;
    private Robot mockRobot;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockWorld = mock(World.class);
        mockRobot = mock(Robot.class);
    }

    @Test
    void testRobotStateResponse() {
        String robotName = "TestBot";

        when(mockWorld.getRobot(robotName)).thenReturn(mockRobot);
        when(mockRobot.getX()).thenReturn(5);
        when(mockRobot.getY()).thenReturn(10);
        when(mockRobot.getDirection()).thenReturn("NORTH");
        when(mockRobot.getShields()).thenReturn(3);
        when(mockRobot.getShots()).thenReturn(5);
        when(mockRobot.getStatus()).thenReturn("OK");

        StateNode stateNode = new StateNode(robotName, mockWorld);

        JsonNode result = stateNode.execute();

        assertTrue(result.has("position"));

        assertEquals(5, result.get("position").get(0).asInt());
        assertEquals(10, result.get("position").get(1).asInt());

        assertEquals("NORTH", result.get("direction").asText());
        assertEquals(3, result.get("shields").asInt());
        assertEquals(5, result.get("shots").asInt());
    }

    @Test
    void RobotIsNullErrorResponse() {
        String robotName = "GhostBot";
        when(mockWorld.getRobot(robotName)).thenReturn(null);

        StateCommand stateCommand = new StateCommand(robotName, mockWorld);

        JsonNode result = stateCommand.execute();

        assertEquals("ERROR", result.get("result").asText());
        JsonNode data = result.get("data");
        assertEquals("No robot provided for state", data.get("message").asText());
        assertEquals("GhostBot", data.get("robotName").asText());
    }
}