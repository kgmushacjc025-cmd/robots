package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builds a "right" command
 * has no arguments, user just types "right"
 */
public class RightCommandBuilder {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * builds the right command JSON
     * @param parts user input split by spaces
     */
    public JsonNode build(String[] parts) {
        if (parts.length != 1) {
            return new ErrorState("Usage: right").toJson();
        }

        ObjectNode request = mapper.createObjectNode();
        request.put("command", "right");
        request.set("arguments", mapper.createArrayNode()); // no arguments
        return request;
    }
}
