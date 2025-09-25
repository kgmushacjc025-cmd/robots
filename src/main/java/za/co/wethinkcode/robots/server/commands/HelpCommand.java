package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import za.co.wethinkcode.robots.server.world.World;

/**
 * Handles the "help" command for a robot.
 * Returns a list of all available commands with descriptions.
 * Can be called before a robot is launched.
 */
public class HelpCommand extends ClientCommands {
    private final String robotName;

    /**
     * Constructor for HelpCommand.
     *
     * @param robotName the name of the robot (can be null if robot not yet launched)
     * @param gameWorld reference to the world
     */
    public HelpCommand(String robotName, World gameWorld) {
        super(robotName, gameWorld);
        this.robotName = robotName;
    }

    /**
     * Executes the help command.
     * Produces a JSON response listing all available commands and their descriptions.
     *
     * @return JSON node containing the list of commands and a message
     */
    @Override
    public JsonNode execute() {
        ObjectNode response = getMapper().createObjectNode();
        response.put("result", "OK");
        ObjectNode data = response.putObject("data");

        ArrayNode commands = data.putArray("commands");

        commands.addObject()
                .put("command", "launch <make> <name>")
                .put("description", "Launch a new robot into the world. (Required before using other commands)");
        commands.addObject()
                .put("command", "forward <steps>")
                .put("description", "Move the robot forward by the given number of steps.");
        commands.addObject()
                .put("command", "back <steps>")
                .put("description", "Move the robot backward by the given number of steps.");
        commands.addObject()
                .put("command", "turn <left|right>")
                .put("description", "Turn the robot 90° left or right.");
        commands.addObject()
                .put("command", "look")
                .put("description", "Scan the world and return visible objects.");
        commands.addObject()
                .put("command", "state")
                .put("description", "Return the current state of the robot.");
        commands.addObject()
                .put("command", "fire")
                .put("description", "Fire the robot's gun in the direction it is facing.");
        commands.addObject()
                .put("command", "repair")
                .put("description", "Repair the robot's shields to maximum after a cooldown period in seconds.");
        commands.addObject()
                .put("command", "reload")
                .put("description", "Reload the robot's shots to maximum after a cooldown period in seconds.");
        commands.addObject()
                .put("command", "orientation")
                .put("description", "Return the current direction of the robot.");
        commands.addObject()
                .put("command", "quit")
                .put("description", "Remove the robot from the world and disconnect.");

        data.put("message", "Available commands:");
        return response;
    }
}