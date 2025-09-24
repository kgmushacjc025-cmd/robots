package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builds a "back" command
 * can handle an optional step count (default 1)
 */
public class BackCommandBuilder {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * builds the back command JSON
     * @param parts user input split by spaces
     */
    public JsonNode build(String[] parts) {
        if (parts.length > 2) {
            return new ErrorState("Usage: back [<steps>]").toJson();
        }

        ObjectNode request = mapper.createObjectNode();
        request.put("command", "back");
        ArrayNode arguments = mapper.createArrayNode();

        try {
            int steps = parts.length == 2 ? Integer.parseInt(parts[1]) : 1;
            arguments.add(steps);
        } catch (NumberFormatException e) {
            return new ErrorState("Invalid number format for steps").toJson();
        }

        request.set("arguments", arguments);
        return request;
    }
}
