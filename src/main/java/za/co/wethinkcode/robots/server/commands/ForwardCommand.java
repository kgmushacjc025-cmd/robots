package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.Obstacle;
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;

import java.util.List;

public class ForwardCommand extends ClientCommands {
    private final String robotName;
    private final JsonNode arguments;
    private final boolean reverse;

    public ForwardCommand(String robotName, JsonNode arguments, World gameWorld) {
        this(robotName, arguments, gameWorld, false);
    }

    public ForwardCommand(String robotName, JsonNode arguments, World gameWorld, boolean reverse) {
        super(robotName, gameWorld);
        this.robotName = robotName;
        this.arguments = arguments;
        this.reverse = reverse;
    }

    @Override
    public JsonNode execute() {
        Robot robot = getWorld().getRobot(robotName);
        if (robot == null) {
            return makeErrorResponse("Robot not found.");
        }

        // Parse steps (default to 1)
        int steps;
        String stepsText = "1";
        if (arguments != null && arguments.size() > 0 && arguments.get(0) != null) {
            stepsText = arguments.get(0).asText();
        }
        try {
            steps = Integer.parseInt(stepsText);
            if (steps < 0) {
                return makeErrorResponse("Invalid number of steps (negative): " + stepsText);
            }
        } catch (NumberFormatException e) {
            return makeErrorResponse("Invalid number of steps: " + stepsText);
        }

        // Determine dx/dy based on robot direction
        int dx = 0, dy = 0;
        String dir = robot.getDirection() == null ? "" : robot.getDirection().toUpperCase();
        switch (dir) {
            case "NORTH": dy = 1; break;
            case "SOUTH": dy = -1; break;
            case "EAST":  dx = 1; break;
            case "WEST":  dx = -1; break;
            default:
                return makeErrorResponse("Unknown direction: " + robot.getDirection());
        }

        if (reverse) {
            dx = -dx;
            dy = -dy;
        }

        int startX = robot.getX();
        int startY = robot.getY();
        int currentX = startX;
        int currentY = startY;

        int stepsTaken = 0;
        String outcome = "success";

        List<Robot> robots = getWorld().getRobotsInWorld();
        List<Obstacle> obstacles = getWorld().getWorldObstacles();
        int width = getWorld().worldWidth();
        int height = getWorld().worldHeight();

        for (int i = 0; i < steps; i++) {
            int nextX = currentX + dx;
            int nextY = currentY + dy;
            Position nextPosition = new Position(nextX, nextY);

            // Check world bounds
            if (!nextPosition.isInsideWorld(width, height)) {
                outcome = "outside";
                break;
            }

            // Deadly obstacles (pit, lake, etc.)
            boolean fell = obstacles.stream()
                    .anyMatch(o -> o.blocksPosition(nextX, nextY) && o.canKillYou());
            if (fell) {
                robot.setPosition(nextX, nextY);
                robot.setStatus("DEAD");
                stepsTaken++;
                outcome = "fell";
                break;
            }

            // Solid obstacles
            boolean blockedByObstacle = obstacles.stream()
                    .anyMatch(o -> o.blocksPosition(nextX, nextY) && !o.canWalkThrough());
            if (blockedByObstacle) {
                outcome = "blocked by obstacle";
                break;
            }

            // Other robots
            boolean blockedByRobot = robots.stream()
                    .anyMatch(r -> !r.getName().equalsIgnoreCase(robotName)
                            && r.getX() == nextX && r.getY() == nextY);
            if (blockedByRobot) {
                outcome = "blocked by robot";
                break;
            }

            // Valid step — advance
            currentX = nextX;
            currentY = nextY;
            stepsTaken++;
        }

        // Apply final position unless robot died
        if (!"fell".equals(outcome)) {
            robot.setPosition(currentX, currentY);
        }

        // Build JSON response
        ObjectNode result = getMapper().createObjectNode();
        result.put("result", "OK");

        ObjectNode data = result.putObject("data");
        data.put("steps", stepsTaken);
        data.put("outcome", outcome);

        ObjectNode start = data.putObject("start");
        start.put("x", startX);
        start.put("y", startY);

        ObjectNode end = data.putObject("end");
        end.put("x", robot.getX());
        end.put("y", robot.getY());

        data.putArray("position").add(robot.getX()).add(robot.getY());

        result.set("state", new StateNode(robotName, getWorld()).execute());

        return result;
    }

    private JsonNode makeErrorResponse(String message) {
        ObjectNode response = getMapper().createObjectNode();
        response.put("result", "error");
        response.put("message", message);
        return response;
    }
}
