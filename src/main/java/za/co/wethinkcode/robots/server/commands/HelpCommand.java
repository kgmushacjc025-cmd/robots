package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import za.co.wethinkcode.robots.server.world.World;

public class HelpCommand extends ClientCommands {
    private final String robotName;

    public HelpCommand(String robotName, World gameWorld) {
        super(robotName, gameWorld);
        this.robotName = robotName;
    }

    @Override
    public JsonNode execute() {
        ObjectNode response = getMapper().createObjectNode();
        response.put("result", "OK");
        ObjectNode data = response.putObject("data");

        ArrayNode commands = data.putArray("commands");

        // Always include launch first
        commands.addObject()
                .put("command", "launch <make> <name> [<shields>]")
                .put("description", "Launch a new robot into the world. (Required before using other commands)");


        commands.addObject().put("command", "forward <steps>")
                .put("description", "Move the robot forward by the given number of steps.");
        commands.addObject().put("command", "back <steps>")
                .put("description", "Move the robot backward by the given number of steps.");
        commands.addObject().put("command", "left")
                .put("description", "Turn the robot 90° left.");
        commands.addObject().put("command", "right")
                .put("description", "Turn the robot 90° right.");
        commands.addObject().put("command", "look")
                .put("description", "Scan the world and return visible objects.");
        commands.addObject().put("command", "state")
                .put("description", "Return the current state of the robot.");
        commands.addObject().put("command", "fire")
                .put("description", "Fire the robot's gun in the direction it is facing.");
        commands.addObject().put("command", "quit")
                .put("description", "Remove the robot from the world and disconnect.");


        data.put("message", "Available commands:");
        return response;
    }
}
