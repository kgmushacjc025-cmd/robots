package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandBuilderTest {
    private CommandBuilder builder;

    @BeforeEach
    void setup() {
        builder = new CommandBuilder();
    }

    @Test
    void testEmptyCommand() {
        JsonNode result = builder.buildCommand("");
        assertEquals("ERROR", result.get("result").asText());
    }

    @Test
    void testLaunchValid() {
        JsonNode result = builder.buildCommand("launch sniper Bob");
        assertEquals("launch", result.get("command").asText());
        assertEquals("Bob", result.get("robot").asText());
        assertEquals("sniper", result.get("arguments").get(0).asText());
        assertEquals("Bob", result.get("arguments").get(1).asText());
    }

    @Test
    void testLaunchInvalidUsage() {
        JsonNode result = builder.buildCommand("launch onlyOneArg");
        assertEquals("ERROR", result.get("result").asText());
        assertTrue(result.get("data").get("message").asText().contains("Usage"));
    }

    @Test
    void testForward() {
        JsonNode result = builder.buildCommand("forward 3");
        assertEquals("forward", result.get("command").asText());
        assertEquals(3, result.get("arguments").get(0).asInt());
    }

    @Test
    void testBack() {
        JsonNode result = builder.buildCommand("back 2");
        assertEquals("back", result.get("command").asText());
        assertEquals(2, result.get("arguments").get(0).asInt());
    }

    @Test
    void testUnknownCommand() {
        JsonNode result = builder.buildCommand("fly");
        assertEquals("ERROR", result.get("result").asText());
        assertTrue(result.get("data").get("message").asText().contains("Unknown"));
    }
}
