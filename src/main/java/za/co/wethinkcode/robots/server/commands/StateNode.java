package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;

public class StateNode extends ClientCommands {
    private final String robotName;
    private final World gameWorld;

    public StateNode(String robotName, World gameWorld) {
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
        ObjectNode state = getMapper().createObjectNode();

        state.putArray("position").add(robot.getX()).add(robot.getY());
        state.put("direction", robot.getDirection());
        state.put("shields", robot.getShields());
        state.put("shots", robot.getShots());
        state.put("maxShots", robot.getMaxShots());
        state.put("status", robot.getStatus());
        state.put("dead", "DEAD".equals(robot.getStatus()));

        return state;
    }
}