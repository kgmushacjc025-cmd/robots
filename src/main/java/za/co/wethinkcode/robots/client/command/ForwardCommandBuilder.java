package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builds a "forward" command JSON request for the robot.
 */
public class ForwardCommandBuilder {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Build the forward command JSON.
     * @param parts the command split by space (e.g., ["forward", "2"])
     * @return JsonNode representing the command or an error
     */
    public JsonNode build(String[] parts) {
        if (parts.length > 2) {
            return new ErrorState("Usage: forward [<steps>]").toJson();
        }

        ObjectNode request = mapper.createObjectNode();
        request.put("command", "forward");
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
