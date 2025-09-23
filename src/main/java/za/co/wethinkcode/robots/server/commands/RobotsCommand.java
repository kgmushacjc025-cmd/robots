package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.World;

/**
 * Command that returns the state of all robots in the world.
 */
public class RobotsCommand extends ServerCommands {
    private final ObjectMapper mapper;
    private final World gameWorld;

    public RobotsCommand(World gameWorld) {
        super(gameWorld);
        this.gameWorld = gameWorld;
        this.mapper = new ObjectMapper();
    }

    @Override
    public JsonNode execute() {
        ObjectNode response = mapper.createObjectNode();
        response.put("result", "OK");

        ObjectNode data = mapper.createObjectNode();
        ArrayNode robotsArray = mapper.createArrayNode();

        // Build state for each robot using StateNode (reuses existing logic)
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
