package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.World;

public class ServerQuitCommand extends ServerCommands {

    public ServerQuitCommand(World gameWorld) {
        super(gameWorld);
    }

    @Override
    public JsonNode execute() {
        ObjectNode response = getMapper().createObjectNode();
        response.put("result", "OK");
        response.putObject("data").put("message", "World terminated");

        return response;
    }
}
