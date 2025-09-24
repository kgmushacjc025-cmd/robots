package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import za.co.wethinkcode.robots.server.world.World;

import java.util.Map;

/**
 * Represents an error response sent to the client.
 * Can be used for general errors, client-specific errors, or launch-related errors.
 */
public class ErrorResponse extends ServerCommands {
    private final String errorMessage;
    private final String robotName;
    private final Map<String, int[]> availableMakes; // optional, for launch info

    /**
     * Constructor for server-wide errors.
     *
     * @param errorMessage the error message
     * @param gameWorld reference to the world
     */
    public ErrorResponse(String errorMessage, World gameWorld) {
        super(gameWorld);
        this.errorMessage = errorMessage;
        this.robotName = null;
        this.availableMakes = null;
    }

    /**
     * Constructor for client-specific errors.
     *
     * @param errorMessage the error message
     * @param robotName the robot associated with the error
     * @param gameWorld reference to the world
     */
    public ErrorResponse(String errorMessage, String robotName, World gameWorld) {
        super(gameWorld);
        this.errorMessage = errorMessage;
        this.robotName = robotName;
        this.availableMakes = null;
    }

    /**
     * Constructor for errors that also provide available robot makes (used in launch command).
     *
     * @param errorMessage the error message
     * @param robotName the robot associated with the error
     * @param gameWorld reference to the world
     * @param makes map of robot makes to their stats [shots, shields, maxShots]
     */
    public ErrorResponse(String errorMessage, String robotName, World gameWorld, Map<String, int[]> makes) {
        super(gameWorld);
        this.errorMessage = errorMessage;
        this.robotName = robotName;
        this.availableMakes = makes;
    }

    /**
     * Build the JSON error response.
     * Includes message, optional robotName, and optionally available makes.
     *
     * @return JSON node representing the error
     */
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

        // attach available makes if provided
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
