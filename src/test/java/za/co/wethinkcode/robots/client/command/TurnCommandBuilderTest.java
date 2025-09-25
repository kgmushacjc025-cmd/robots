package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class TurnCommandBuilderTest {
    private TurnCommandBuilder builder;
    private ObjectMapper mapper;

    // setup builder and mapper
    @BeforeEach
    void setUp() {
        builder = new TurnCommandBuilder();
        mapper = new ObjectMapper();
    }

    // test valid turn left command
    @Test
    void testTurnLeft() {
        // input: turn left
        String[] parts = {"turn", "left"};

        JsonNode result = builder.build(parts);

        ObjectNode expected = mapper.createObjectNode();
        expected.put("command", "turn");
        ArrayNode args = mapper.createArrayNode();
        args.add("left");
        expected.set("arguments", args);

        assertEquals(expected, result);
    }

    // test valid turn right command
    @Test
    void testTurnRight() {
        // input: turn right
        String[] parts = {"turn", "right"};

        // execute
        JsonNode result = builder.build(parts);

        ObjectNode expected = mapper.createObjectNode();
        expected.put("command", "turn");
        ArrayNode args = mapper.createArrayNode();
        args.add("right");
        expected.set("arguments", args);

        assertEquals(expected, result);
    }

    // test invalid direction
    @Test
    void testInvalidDirection() {
        // input: turn up
        String[] parts = {"turn", "up"};

        JsonNode result = builder.build(parts);

        ObjectNode expected = mapper.createObjectNode();
        expected.put("result", "ERROR");
        ObjectNode data = mapper.createObjectNode();
        data.put("message", "Invalid direction: 'up'");
        expected.set("data", data);

        assertEquals(expected, result);
    }

    // test wrong number of arguments
    @Test
    void testWrongArgumentCount() {
        // input: turn (no direction)
        String[] parts = {"turn"};

        JsonNode result = builder.build(parts);

        ObjectNode expected = mapper.createObjectNode();
        expected.put("result", "ERROR");
        ObjectNode data = mapper.createObjectNode();
        data.put("message", "Usage: turn <left|right>");
        expected.set("data", data);

        assertEquals(expected, result);
    }
}