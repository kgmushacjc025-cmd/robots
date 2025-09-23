package za.co.wethinkcode.robots.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import za.co.wethinkcode.robots.client.command.CommandBuilder;

class CommandBuilderTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testInvalidCommand() {
        CommandBuilder builder = new CommandBuilder();
        JsonNode command = builder.buildCommand("launch");

        ObjectNode expected = mapper.createObjectNode();
        expected.put("result", "ERROR");

        ObjectNode dataNode = mapper.createObjectNode();
        dataNode.put("message", "Usage: launch <make> <name>");

        expected.set("data", dataNode);

        assertEquals(expected, command);
    }

    @Test
    void testValidCommand(){
        CommandBuilder builder = new CommandBuilder();
        JsonNode command = builder.buildCommand("look");

        ObjectNode expected = mapper.createObjectNode();
        expected.put("command", "look");
        ArrayNode arguments = mapper.createArrayNode();
        expected.put("arguments", arguments);

        assertEquals(expected, command);
    }

}