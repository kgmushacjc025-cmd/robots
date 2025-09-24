package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.*;

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
        if (robot == null) return makeErrorResponse("Robot not found.");

        int steps = parseSteps();
        if (steps < 0) return makeErrorResponse("Invalid number of steps: " + arguments);

        int[] delta = getDirectionDelta(robot.getDirection());
        if (delta == null) return makeErrorResponse("Unknown direction: " + robot.getDirection());

        if (reverse) {
            delta[0] = -delta[0];
            delta[1] = -delta[1];
        }

        // Pre-validate path for non-lethal blocks
        PathCheckResult check = validatePath(robot, steps, delta[0], delta[1]);
        if (!check.valid) {
            return buildResponse(robot, 0, friendlyOutcome(check.outcome));
        }

        // Actual movement — deadly obstacles are applied here
        int currentX = robot.getX();
        int currentY = robot.getY();
        int stepsTaken = 0;
        String outcome = "success";

        List<Robot> robots = getWorld().getRobotsInWorld();
        List<Obstacle> obstacles = getWorld().getWorldObstacles();

        for (int i = 0; i < steps; i++) {
            int nextX = currentX + delta[0];
            int nextY = currentY + delta[1];

            // Check deadly obstacles
            boolean fell = obstacles.stream()
                    .anyMatch(o -> o.blocksPosition(nextX, nextY) && o.canKillYou());
            if (fell) {
                robot.setPosition(nextX, nextY);
                robot.setStatus("DEAD");
                stepsTaken++;
                outcome = "fell";
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

            // Valid step
            currentX = nextX;
            currentY = nextY;
            stepsTaken++;
        }

        // Apply final position if not dead
        if (!"fell".equals(outcome)) {
            robot.setPosition(currentX, currentY);
        }

        return buildResponse(robot, stepsTaken, friendlyOutcome(outcome));
    }

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

    // Pre-validation for path: world bounds & solid obstacles
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

    private boolean blockedByObstacle(int x, int y, List<Obstacle> obstacles) {
        return obstacles.stream()
                .anyMatch(o -> o.blocksPosition(x, y) && !o.canWalkThrough());
    }

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

    private JsonNode makeErrorResponse(String message) {
        ObjectNode response = getMapper().createObjectNode();
        response.put("result", "error");
        response.put("message", message);
        return response;
    }

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
