package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;

import java.util.Map;

public class LaunchCommand extends ClientCommands {
    private final JsonNode arguments;
    private final String robotName;
    private final ObjectMapper mapper;

    public LaunchCommand(String robotName, JsonNode arguments, World gameWorld) {
        super(arguments.get(1).asText(), gameWorld);
        this.arguments = arguments;
        this.robotName = arguments.get(1).asText();
        this.mapper = new ObjectMapper();
    }

    @Override
    public JsonNode execute() {
        World world = getWorld();
        Map<String, int[]> makes = world.getMakes();

        // first argument is the make
        String makeInput = arguments.get(0).asText();
        String make = makes.keySet().stream()
                .filter(key -> key.equalsIgnoreCase(makeInput))
                .findFirst()
                .orElse(null);

        if (make == null) {
            // Unknown make -> ErrorResponse with available makes
            return new ErrorResponse(
                    "Unknown make: " + makeInput + ". Use: launch <make> <name>",
                    robotName,
                    world,
                    makes
            ).execute();
        }

        int[] stats = makes.get(make);
        int shots = stats[0];
        int shields = stats[1];
        int maxShot = stats[2];

        // Build Robot with values from config
        Robot robot = new Robot(robotName, make, shields, shots, maxShot);

        // Check if name is already taken
        if (world.getRobot(robotName) != null) {
            return new ErrorResponse(
                    "Name taken! Pick a new one for your robot.",
                    robotName,
                    world
            ).execute();
        }

        // Check if world has room
        if (!world.addRobot(robot)) {
            return new ErrorResponse(
                    "Looks like this world is full!",
                    robotName,
                    world
            ).execute();
        }

        // Build success response
        ObjectNode response = mapper.createObjectNode();
        response.put("result", "OK");

        ObjectNode data = mapper.createObjectNode();
        data.putArray("position").add(robot.getX()).add(robot.getY());
        data.put("robotName", robotName);
        data.put("visibility", world.worldHeight());
        data.put("reload", 5);
        data.put("repair", 3);
        data.put("shields", shields);
        data.put("shots", shots);
        data.put("maxshot", maxShot);

        response.set("data", data);
        response.set("state", new StateNode(robotName, world).execute());

        return response;
    }
}
