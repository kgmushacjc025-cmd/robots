package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ErrorState {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final String message;

    public ErrorState(String message) {
        this.message = message;
    }

    public JsonNode toJson() {
        ObjectNode response = mapper.createObjectNode();
        response.put("result", "ERROR");
        ObjectNode dataNode = mapper.createObjectNode();
        dataNode.put("message", message);
        response.set("data", dataNode);
        return response;
    }
}
