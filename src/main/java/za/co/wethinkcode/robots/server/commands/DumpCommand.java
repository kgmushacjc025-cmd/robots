package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.World;

/**
 * Command to dump the current state of the world.
 * Returns all relevant world data in a JSON object.
 */
public class DumpCommand extends ServerCommands {
    private final ObjectMapper mapper;
    private final World gameWorld;

    /**
     * Constructor
     *
     * @param gameWorld reference to the world object
     */
    public DumpCommand(World gameWorld) {
        super(gameWorld);
        this.gameWorld = gameWorld;
        this.mapper = new ObjectMapper();
    }

    /**
     * Executes the dump command:
     * - collects the current world state
     * - returns JSON with result "OK" and the worldState data
     */
    @Override
    public JsonNode execute() {
        ObjectNode response = mapper.createObjectNode();
        response.put("result", "OK");

        ObjectNode data = mapper.createObjectNode();
        data.set("worldState", gameWorld.getWorldState());

        response.set("data", data);
        return response;
    }
}
