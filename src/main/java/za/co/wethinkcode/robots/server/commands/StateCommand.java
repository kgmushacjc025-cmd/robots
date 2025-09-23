package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;

public class StateCommand extends ClientCommands {
    private final String robotName;
    private final World gameWorld;

    public StateCommand(String robotName, World gameWorld) {
        super(robotName, gameWorld);
        this.robotName = robotName;
        this.gameWorld = gameWorld;
    }

    @Override
    public JsonNode execute() {
        Robot robot = gameWorld.getRobot(robotName);
        if (robot == null) {
            return new ErrorResponse("No robot provided for state", robotName, getWorld()).execute();
        }
        ObjectNode stateResponse = getMapper().createObjectNode();
        stateResponse.set("state", new StateNode(robotName, gameWorld).execute());
        stateResponse.put("result", "OK");
        return stateResponse;
    }
}