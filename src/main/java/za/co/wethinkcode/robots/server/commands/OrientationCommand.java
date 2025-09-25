package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;

public class OrientationCommand extends ClientCommands {

    public OrientationCommand(String robotName, World gameWorld) {
        super(robotName, gameWorld);
    }

    @Override
    public JsonNode execute() {
        Robot robot = getWorld().getRobot(getRobotName());
        ObjectNode response = getMapper().createObjectNode();
        ObjectNode data = getMapper().createObjectNode();

        if (robot == null || "DEAD".equals(robot.getStatus())) {
            response.put("result", "ERROR");
            data.put("message", "Robot is dead and cannot check orientation.");
        } else {
            response.put("result", "OK");
            data.put("message", "robot direction " + robot.getDirection());
            response.set("state", new StateNode(getRobotName(), getWorld()).execute());
        }

        response.set("data", data);
        return response;
    }
}