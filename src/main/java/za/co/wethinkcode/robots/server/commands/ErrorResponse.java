package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.World;

public class ErrorResponse extends ServerCommands {
    private final String errorMessage;
    private final String robotName;

    // Constructor for server
    public ErrorResponse(String errorMessage, World gameWorld) {
        super(gameWorld);
        this.errorMessage = errorMessage;
        this.robotName = null;
    }

    // Constructor for client
    public ErrorResponse(String errorMessage, String robotName, World gameWorld) {
        super(gameWorld);
        this.errorMessage = errorMessage;
        this.robotName = robotName;
    }

    @Override
    public JsonNode execute() {
        ObjectNode response = getMapper().createObjectNode();
        response.put("result", "ERROR");
        ObjectNode data = getMapper().createObjectNode();
        data.put("message", errorMessage);
        if (robotName != null) {
            data.put("robotName", robotName);
        }
        response.set("data", data);
        return response;
    }
}