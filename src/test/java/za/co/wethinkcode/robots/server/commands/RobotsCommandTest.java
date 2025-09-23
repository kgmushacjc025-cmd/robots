package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import za.co.wethinkcode.robots.server.world.World;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RobotsCommandTest {

    private World mockWorld;
    private RobotsCommand robotsCommand;

    @BeforeEach
    void setup() {
        mockWorld = mock(World.class);
        robotsCommand = new RobotsCommand(mockWorld);
    }

    @Test
    void testNoRobotsReturnsEmptyArray() {
        when(mockWorld.getRobotNames()).thenReturn(List.of());

        JsonNode result = robotsCommand.execute();

        assertEquals("OK", result.get("result").asText());
        assertEquals(0, result.get("data").get("robots").size());
        verify(mockWorld, times(1)).getRobotNames();
    }

    @Test
    void testRobotsArrayContainsRobotNames() {
        when(mockWorld.getRobotNames()).thenReturn(List.of("Alpha", "Beta"));

        try (MockedConstruction<StateNode> mockedStateNode = mockConstruction(
                StateNode.class,
                (mock, context) -> {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode fakeState = mapper.createObjectNode();
                    fakeState.put("status", "READY");
                    when(mock.execute()).thenReturn(fakeState);
                })) {

            JsonNode result = robotsCommand.execute();

            assertEquals("OK", result.get("result").asText());
            assertTrue(result.get("data").get("robots").isArray());
            assertEquals(2, result.get("data").get("robots").size());

            // Verify that both names are present in response
            List<String> names = result.get("data").get("robots")
                    .findValuesAsText("name");
            assertTrue(names.contains("Alpha"));
            assertTrue(names.contains("Beta"));

            verify(mockWorld, times(1)).getRobotNames();
            assertEquals(2, mockedStateNode.constructed().size(),
                    "StateNode should be constructed once per robot");
        }
    }
}
