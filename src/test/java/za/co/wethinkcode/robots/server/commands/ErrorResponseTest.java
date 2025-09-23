package za.co.wethinkcode.robots.server.commands;


import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ErrorResponseTest {

    private World mockWorld;

    @BeforeEach
    void setup() {
        mockWorld = mock(World.class);
    }

    @Test
    void NoRobotNameError() {
        String errorMessage = "Something went wrong";

        ErrorResponse errorResponse = new ErrorResponse(errorMessage, mockWorld);

        JsonNode result = errorResponse.execute();

        assertEquals("ERROR", result.get("result").asText());

        JsonNode data = result.get("data");
        String resultMessage = data.get("message").asText();
        assertNotNull(data);
        assertEquals(errorMessage, resultMessage);
        assertNull(data.get("robotName"));

    }

    @Test
    void RobotNameError() {
        String errorMessage = "Failed to execute command";
        String robotName = "RobotX";

        ErrorResponse errorResponse = new ErrorResponse(errorMessage, robotName, mockWorld);

        JsonNode result = errorResponse.execute();

        assertEquals("ERROR", result.get("result").asText());

        JsonNode data = result.get("data");
        String resultMessage = data.get("message").asText();
        assertNotNull(data);
        assertEquals(errorMessage, resultMessage);
        assertNotNull(data.get("robotName"));
    }
}
