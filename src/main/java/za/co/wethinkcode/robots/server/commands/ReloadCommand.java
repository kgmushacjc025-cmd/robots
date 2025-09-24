package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;

public class ReloadCommand extends ClientCommands {

    public ReloadCommand(String robotName, World gameWorld) {
        super(robotName, gameWorld);
    }

    @Override
    public JsonNode execute() {
        Robot robot = getWorld().getRobot(getRobotName());
        ObjectNode response = getMapper().createObjectNode();
        ObjectNode data = getMapper().createObjectNode();

        if (robot == null || "DEAD".equals(robot.getStatus())) {
            response.put("result", "ERROR");
            data.put("message", "Robot is dead and cannot reload.");
        } else {
            robot.reload();
            response.put("result", "OK");
            data.put("message", "Weapon reloaded to " + robot.getShots() + " shots");
        }

        response.set("data", data);
        if (robot != null && !"DEAD".equals(robot.getStatus())) {
            response.set("state", new StateNode(getRobotName(), getWorld()).execute());
        }

        return response;
    }
}
