package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builds a "turn" command
 * expects arguments "left" or "right"
 */
public class TurnCommandBuilder {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * builds the turn command JSON
     * @param parts user input split by spaces
     */
    public JsonNode build(String[] parts) {
        if (parts.length != 2) {
            return new ErrorState("Usage: turn <left|right>").toJson();
        }

        String direction = parts[1].toLowerCase();
        if (!direction.equals("left") && !direction.equals("right")) {
            return new ErrorState("Invalid direction: '" + direction + "'").toJson();
        }

        ObjectNode request = mapper.createObjectNode();
        request.put("command", "turn");
        ArrayNode arguments = mapper.createArrayNode();
        arguments.add(direction);
        request.set("arguments", arguments);
        return request;
    }
}