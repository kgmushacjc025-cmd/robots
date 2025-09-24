package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.*;

import java.util.List;

/**
 * Handles the "forward" command for a robot.
 * Moves the robot forward a specified number of steps, handling obstacles, other robots,
 * and deadly pits. Can also be used in reverse mode to move backward.
 */
public class ForwardCommand extends ClientCommands {
    private final String robotName;
    private final JsonNode arguments;
    private final boolean reverse;

    /**
     * ForwardCommand with default forward movement.
     *
     * @param robotName the name of the robot
     * @param arguments optional arguments (steps)
     * @param gameWorld reference to the world
     */
    public ForwardCommand(String robotName, JsonNode arguments, World gameWorld) {
        this(robotName, arguments, gameWorld, false);
    }

    /**
     * ForwardCommand with reverse option.
     *
     * @param robotName the name of the robot
     * @param arguments optional arguments (steps)
     * @param gameWorld reference to the world
     * @param reverse if true, moves robot backward
     */
    public ForwardCommand(String robotName, JsonNode arguments, World gameWorld, boolean reverse) {
        super(robotName, gameWorld);
        this.robotName = robotName;
        this.arguments = arguments;
        this.reverse = reverse;
    }

    /**
     * Executes the forward or backward movement command.
     * Validates path, handles collisions, updates robot position and state, and returns result.
     *
     * @return JSON node with movement result, steps taken, and updated state
     */
    @Override
    public JsonNode execute() {
        Robot robot = getWorld().getRobot(robotName);
        if (robot == null) return makeErrorResponse("Robot not found.");

        int steps = parseSteps();
        if (steps < 0) return makeErrorResponse("Invalid number of steps: " + arguments);

        int[] delta = getDirectionDelta(robot.getDirection());
        if (delta == null) return makeErrorResponse("Unknown direction: " + robot.getDirection());

        if (reverse) {
            delta[0] = -delta[0];
            delta[1] = -delta[1];
        }

        PathCheckResult check = validatePath(robot, steps, delta[0], delta[1]);
        if (!check.valid) {
            return buildResponse(robot, 0, friendlyOutcome(check.outcome));
        }

        // actual movement
        int currentX = robot.getX();
        int currentY = robot.getY();
        int stepsTaken = 0;
        String outcome = "success";

        List<Robot> robots = getWorld().getRobotsInWorld();
        List<Obstacle> obstacles = getWorld().getWorldObstacles();

        for (int i = 0; i < steps; i++) {
            int nextX = currentX + delta[0];
            int nextY = currentY + delta[1];

            boolean fell = obstacles.stream()
                    .anyMatch(o -> o.blocksPosition(nextX, nextY) && o.canKillYou());
            if (fell) {
                robot.setPosition(nextX, nextY);
                robot.setStatus("DEAD");
                stepsTaken++;
                outcome = "fell";
                break;
            }

            boolean blockedByRobot = robots.stream()
                    .anyMatch(r -> !r.getName().equalsIgnoreCase(robotName)
                            && !"DEAD".equals(r.getStatus())
                            && r.getX() == nextX && r.getY() == nextY);
            if (blockedByRobot) {
                outcome = "blocked by robot";
                break;
            }

            currentX = nextX;
            currentY = nextY;
            stepsTaken++;
        }

        if (!"fell".equals(outcome)) {
            robot.setPosition(currentX, currentY);
        }

        return buildResponse(robot, stepsTaken, friendlyOutcome(outcome));
    }

    /**
     * Parses the number of steps from command arguments.
     *
     * @return number of steps to move; -1 if invalid
     */
    private int parseSteps() {
        String stepsText = (arguments != null && arguments.size() > 0 && arguments.get(0) != null)
                ? arguments.get(0).asText()
                : "1";
        try {
            int steps = Integer.parseInt(stepsText);
            return steps >= 0 ? steps : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Converts a direction string to x,y delta values.
     *
     * @param direction direction string
     * @return int array [dx, dy] or null if unknown direction
     */
    private int[] getDirectionDelta(String direction) {
        if (direction == null) return null;
        switch (direction.toUpperCase()) {
            case "NORTH": return new int[]{0, 1};
            case "SOUTH": return new int[]{0, -1};
            case "EAST":  return new int[]{1, 0};
            case "WEST":  return new int[]{-1, 0};
            default: return null;
        }
    }

    /**
     * Pre-validates path for obstacles and world boundaries.
     *
     * @param robot the robot
     * @param steps number of steps to move
     * @param dx delta X
     * @param dy delta Y
     * @return PathCheckResult indicating validity
     */
    private PathCheckResult validatePath(Robot robot, int steps, int dx, int dy) {
        int currentX = robot.getX();
        int currentY = robot.getY();

        List<Obstacle> obstacles = getWorld().getWorldObstacles();
        int width = getWorld().worldWidth();
        int height = getWorld().worldHeight();

        for (int i = 0; i < steps; i++) {
            int nextX = currentX + dx;
            int nextY = currentY + dy;
            Position nextPos = new Position(nextX, nextY);

            if (!nextPos.isInsideWorld(width, height)) {
                return new PathCheckResult(false, "outside", currentX, currentY);
            }

            if (blockedByObstacle(nextX, nextY, obstacles)) {
                return new PathCheckResult(false, "blocked by obstacle", currentX, currentY);
            }

            currentX = nextX;
            currentY = nextY;
        }

        return new PathCheckResult(true, "success", currentX, currentY);
    }

    /**
     * Checks if position is blocked by a solid obstacle.
     */
    private boolean blockedByObstacle(int x, int y, List<Obstacle> obstacles) {
        return obstacles.stream()
                .anyMatch(o -> o.blocksPosition(x, y) && !o.canWalkThrough());
    }

    /**
     * Converts raw outcome string to user-friendly message.
     */
    private String friendlyOutcome(String outcome) {
        switch (outcome) {
            case "outside": return "Cannot move: would leave the world";
            case "blocked by obstacle": return "Blocked by obstacle";
            case "fell": return "Fell into a bottomless pit";
            case "blocked by robot": return "Blocked by another robot";
            case "success": return "Moved successfully";
            default: return outcome;
        }
    }

    /**
     * Builds the JSON response after movement.
     */
    private JsonNode buildResponse(Robot robot, int stepsTaken, String outcome) {
        ObjectNode response = getMapper().createObjectNode();
        response.put("result", "OK");

        ObjectNode data = response.putObject("data");
        data.put("steps", stepsTaken);
        data.put("outcome", outcome);

        ObjectNode start = data.putObject("start");
        start.put("x", robot.getX());
        start.put("y", robot.getY());

        ObjectNode end = data.putObject("end");
        end.put("x", robot.getX());
        end.put("y", robot.getY());

        data.putArray("position").add(robot.getX()).add(robot.getY());
        response.set("state", new StateNode(robotName, getWorld()).execute());

        return response;
    }

    /**
     * Creates a standard error response JSON.
     */
    private JsonNode makeErrorResponse(String message) {
        ObjectNode response = getMapper().createObjectNode();
        response.put("result", "error");
        response.put("message", message);
        return response;
    }

    /**
     * Helper class for path validation results.
     */
    private static class PathCheckResult {
        boolean valid;
        String outcome;
        int finalX;
        int finalY;

        PathCheckResult(boolean valid, String outcome, int finalX, int finalY) {
            this.valid = valid;
            this.outcome = outcome;
            this.finalX = finalX;
            this.finalY = finalY;
        }
    }
}
