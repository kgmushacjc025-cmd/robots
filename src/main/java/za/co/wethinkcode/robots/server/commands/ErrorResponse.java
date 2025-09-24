package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import za.co.wethinkcode.robots.server.world.World;

import java.util.Map;

public class ErrorResponse extends ServerCommands {
    private final String errorMessage;
    private final String robotName;
    private final Map<String, int[]> availableMakes; // optional

    // for server errors
    public ErrorResponse(String errorMessage, World gameWorld) {
        super(gameWorld);
        this.errorMessage = errorMessage;
        this.robotName = null;
        this.availableMakes = null;
    }

    // for client errors
    public ErrorResponse(String errorMessage, String robotName, World gameWorld) {
        super(gameWorld);
        this.errorMessage = errorMessage;
        this.robotName = robotName;
        this.availableMakes = null;
    }

    // With available makes (for LaunchCommand)
    public ErrorResponse(String errorMessage, String robotName, World gameWorld, Map<String, int[]> makes) {
        super(gameWorld);
        this.errorMessage = errorMessage;
        this.robotName = robotName;
        this.availableMakes = makes;
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

        // If available makes are provided, attach them
        if (availableMakes != null) {
            ArrayNode makesArray = getMapper().createArrayNode();
            availableMakes.forEach((name, stats) -> {
                ObjectNode makeInfo = getMapper().createObjectNode();
                makeInfo.put("make", name);
                makeInfo.put("shots", stats[0]);
                makeInfo.put("shields", stats[1]);
                makeInfo.put("maxshot", stats[2]);
                makesArray.add(makeInfo);
            });
            response.set("available_makes", makesArray);
        }

        return response;
    }
}
