package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class LeftCommandBuilder {
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode build(String[] parts) {
        if (parts.length != 1) {
            return new ErrorState("Usage: left").toJson();
        }
        ObjectNode request = mapper.createObjectNode();
        request.put("command", "left");
        request.set("arguments", mapper.createArrayNode()); // No arguments
        return request;
    }
}
