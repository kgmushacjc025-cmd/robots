package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;

import java.util.Map;

public class LeftCommand extends ClientCommands {
    private final String robotName;

    // Direction mapping for left turn
    private static final Map<String, String> LEFT_TURN = Map.of(
            "NORTH", "WEST",
            "WEST", "SOUTH",
            "SOUTH", "EAST",
            "EAST", "NORTH"
    );

    public LeftCommand(String robotName, JsonNode arguments, World gameWorld) {
        super(robotName, gameWorld);
        this.robotName = robotName;
    }

    @Override
    public JsonNode execute() {
        Robot robot = getWorld().getRobot(robotName);
        if (robot == null) {
            return new ErrorResponse("Robot not found.",getWorld()).execute();
        }

        String currentDir = robot.getDirection().toUpperCase();
        String newDir = LEFT_TURN.getOrDefault(currentDir, currentDir);

        robot.setDirection(newDir);

        ObjectNode result = getMapper().createObjectNode();
        result.put("result", "OK");

        ObjectNode data = result.putObject("data");
        data.put("oldDirection", currentDir);
        data.put("turn", "left");
        data.put("newDirection", newDir);

        result.set("state", new StateNode(robotName, getWorld()).execute());

        return result;
    }


}
