package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.World;

/**
 * Command that returns the current state of all robots in the world.
 * Uses StateNode to generate each robot's individual state.
 * The response contains a "robots" array under "data" with each robot's details.
 */
public class RobotsCommand extends ServerCommands {
    private final ObjectMapper mapper;
    private final World gameWorld;

    /**
     * Constructor for RobotsCommand.
     *
     * @param gameWorld the reference to the world containing robots
     */
    public RobotsCommand(World gameWorld) {
        super(gameWorld);
        this.gameWorld = gameWorld;
        this.mapper = new ObjectMapper();
    }

    /**
     * Executes the RobotsCommand.
     * Iterates over all robots in the world, collects their state, and returns a JSON response.
     *
     * @return JsonNode containing the result "OK" and the list of robots under "data"
     */
    @Override
    public JsonNode execute() {
        ObjectNode response = mapper.createObjectNode();
        response.put("result", "OK");

        ObjectNode data = mapper.createObjectNode();
        ArrayNode robotsArray = mapper.createArrayNode();

        // Build state for each robot using StateNode
        for (String name : gameWorld.getRobotNames()) {
            JsonNode robotState = new StateNode(name, gameWorld).execute();
            ((ObjectNode) robotState).put("name", name);
            robotsArray.add(robotState);
        }

        data.set("robots", robotsArray);
        response.set("data", data);
        return response;
    }
}
