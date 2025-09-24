package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Handles invalid command states
 * basically just wraps a message in a JSON structure
 */
public class ErrorState {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final String message;

    public ErrorState(String message) {
        this.message = message;
    }

    /** turn the error into a JSON node so server/client can read it */
    public JsonNode toJson() {
        ObjectNode response = mapper.createObjectNode();
        response.put("result", "ERROR");
        ObjectNode dataNode = mapper.createObjectNode();
        dataNode.put("message", message);
        response.set("data", dataNode);
        return response;
    }
}
