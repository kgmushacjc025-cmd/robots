package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ForwardCommandBuilderTest {

    private ForwardCommandBuilder builder;

    @BeforeEach
    void setUp() {
        // create builder
        builder = new ForwardCommandBuilder();
    }

    @Test
    void testValidCommand() {
        // test forward 10
        String[] parts = {"forward", "10"};
        JsonNode result = builder.build(parts);

        assertEquals("forward", result.get("command").asText());
        assertTrue(result.has("arguments"));
        assertEquals(1, result.get("arguments").size());
        assertEquals(10, result.get("arguments").get(0).asInt());
    }

    @Test
    void testNoSteps() {
        // test forward with no args
        String[] parts = {"forward"};
        JsonNode result = builder.build(parts);

        assertEquals("ERROR", result.get("result").asText());
        assertTrue(result.has("data"));
        assertEquals("Usage: forward [<steps>]", result.get("data").get("message").asText());
    }

    @Test
    void testInvalidSteps() {
        // test forward with bad steps
        String[] parts = {"forward", "xyz"};
        JsonNode result = builder.build(parts);

        assertEquals("ERROR", result.get("result").asText());
        assertTrue(result.has("data"));
        assertEquals("Invalid number format for steps", result.get("data").get("message").asText());
    }
}