package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BackCommandBuilder {
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode build(String[] parts) {
        if (parts.length > 2) {
            return createErrorResponse("Usage: back [<steps>]");
        }
        ObjectNode request = mapper.createObjectNode();
        request.put("command", "back");
        ArrayNode arguments = mapper.createArrayNode();
        try {
            int steps = parts.length == 2 ? Integer.parseInt(parts[1]) : 1;
            arguments.add(steps);
        } catch (NumberFormatException e) {
            return createErrorResponse("Invalid number format for steps");
        }
        request.set("arguments", arguments);
        return request;
    }

    private JsonNode createErrorResponse(String message) {
        ObjectNode response = mapper.createObjectNode();
        response.put("result", "ERROR");
        response.putObject("data").put("message", message);
        return response;
    }
}