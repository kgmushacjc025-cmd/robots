package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BackCommandBuilderTest {

    private BackCommandBuilder builder;

    @BeforeEach
    void setUp() {
        // create builder
        builder = new BackCommandBuilder();
    }

    @Test
    void testValidCommand() {
        // test back 5
        String[] parts = {"back", "5"};
        JsonNode result = builder.build(parts);

        assertEquals("back", result.get("command").asText());
        assertTrue(result.has("arguments"));
        assertEquals(1, result.get("arguments").size());
        assertEquals(5, result.get("arguments").get(0).asInt());
    }

    @Test
    void testNoSteps() {
        // test back with no args
        String[] parts = {"back"};
        JsonNode result = builder.build(parts);

        assertEquals("ERROR", result.get("result").asText());
        assertTrue(result.has("data"));
        assertEquals("Usage: back [<steps>]", result.get("data").get("message").asText());
    }

    @Test
    void testInvalidSteps() {
        // test back with bad steps
        String[] parts = {"back", "abc"};
        JsonNode result = builder.build(parts);

        assertEquals("ERROR", result.get("result").asText());
        assertTrue(result.has("data"));
        assertEquals("Invalid number format for steps", result.get("data").get("message").asText());
    }
}