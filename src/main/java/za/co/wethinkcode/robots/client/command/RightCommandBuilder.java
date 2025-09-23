package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RightCommandBuilder {
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode build(String[] parts) {
        if (parts.length != 1) {
            return createErrorResponse("Usage: right");
        }
        ObjectNode request = mapper.createObjectNode();
        request.put("command", "right");
        request.set("arguments", mapper.createArrayNode()); // No arguments
        return request;
    }

    private JsonNode createErrorResponse(String message) {
        ObjectNode response = mapper.createObjectNode();
        response.put("result", "ERROR");
        response.putObject("data").put("message", message);
        return response;
    }
}