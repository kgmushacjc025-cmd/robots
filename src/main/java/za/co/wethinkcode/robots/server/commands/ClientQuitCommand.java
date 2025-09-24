package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.World;

/**
 * Command to quit the game for a specific robot.
 * Removes the robot from the world and returns a shutdown message.
 */
public class ClientQuitCommand extends ClientCommands {
    private final World gameWorld;
    private final String robotName;
    private final ObjectMapper mapper;

    /**
     * Constructor
     *
     * @param robotName the name of the robot quitting
     * @param gameWorld reference to the world object
     */
    public ClientQuitCommand(String robotName, World gameWorld) {
        super(robotName, gameWorld);
        this.gameWorld = gameWorld;
        this.robotName = robotName;
        this.mapper = new ObjectMapper();
    }

    /**
     * Executes the quit command:
     * - removes the robot from the world
     * - returns JSON with result, message, and exit flag
     */
    @Override
    public JsonNode execute() {
        // Remove robot from the world
        gameWorld.removeOneRobot(robotName);

        // Build response
        ObjectNode response = mapper.createObjectNode();
        response.put("result", "OK");
        response.putObject("data").put("message", "Robot shutting down and exiting the world.");
        response.put("exitWorld", "TRUE");

        return response;
    }
}
