package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.wethinkcode.robots.server.world.World;

public class BackCommand extends ClientCommands {
    private final String robotName;
    private final JsonNode arguments;

    public BackCommand(String robotName, JsonNode arguments, World gameWorld) {
        super(robotName, gameWorld);
        this.robotName = robotName;
        this.arguments = arguments;
    }

    @Override
    public JsonNode execute() {
        return new ForwardCommand(robotName, arguments, getWorld(), true).execute();
    }
}
