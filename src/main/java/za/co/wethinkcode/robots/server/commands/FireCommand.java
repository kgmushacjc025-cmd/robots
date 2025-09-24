package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;

import java.util.List;

/**
 * Handles the "fire" command for a robot.
 * Checks if the robot can fire, calculates distance, and applies damage to any hit robot.
 */
public class FireCommand extends ClientCommands {
    private final String robotName;

    /**
     * Constructor for FireCommand.
     *
     * @param robotName the name of the robot firing
     * @param arguments unused for now (can hold future options)
     * @param gameWorld reference to the world
     */
    public FireCommand(String robotName, JsonNode arguments, World gameWorld) {
        super(robotName, gameWorld);
        this.robotName = robotName;
    }

    /**
     * Executes the fire command.
     * Determines if the robot can fire, calculates trajectory, updates hit robot if any,
     * and returns detailed info including shots used, distance traveled, and outcome.
     *
     * @return JSON node with fire result and updated state
     */
    @Override
    public JsonNode execute() {
        Robot shooter = getWorld().getRobot(robotName);

        // check if robot can fire
        if (shooter == null || !shooter.canFire()) {
            return makeErrorResponse("Cannot fire: no shots remaining. Consider reloading.");
        }

        int maxDistance = shooter.getMaxShotDistance();
        int shotsToConsume = calculateShotsConsumed(maxDistance);
        int startShots = shooter.getShots();

        if (startShots < shotsToConsume) {
            return makeErrorResponse(
                    "Not enough shots to fire at distance " + maxDistance +
                            " (need " + shotsToConsume + ", have " + startShots + ")."
            );
        }

        shooter.consumeShots(shotsToConsume);

        int startX = shooter.getX();
        int startY = shooter.getY();

        int dx = 0, dy = 0;
        switch (shooter.getDirection().toUpperCase()) {
            case "NORTH" -> dy = 1;
            case "SOUTH" -> dy = -1;
            case "EAST"  -> dx = 1;
            case "WEST"  -> dx = -1;
        }

        int x = startX;
        int y = startY;
        int distanceTraveled = 0;
        boolean hit = false;
        String targetHit = null;
        int hitX = x;
        int hitY = y;

        List<Robot> robots = getWorld().getRobotsInWorld();

        // trace shot along the path
        for (int i = 1; i <= maxDistance; i++) {
            x += dx;
            y += dy;
            distanceTraveled = i;
            hitX = x;
            hitY = y;

            for (Robot target : robots) {
                if (target != shooter && !"DEAD".equals(target.getStatus())
                        && target.getX() == x && target.getY() == y) {
                    target.damage(shotsToConsume);
                    hit = true;
                    targetHit = target.getName();
                    break;
                }
            }
            if (hit) break;
        }

        // prepare result JSON
        ObjectNode result = getMapper().createObjectNode();
        result.put("result", "OK");

        ObjectNode data = result.putObject("data");
        data.put("fired", true);
        data.put("startX", startX);
        data.put("startY", startY);
        data.put("maxDistance", maxDistance);
        data.put("distanceTraveled", distanceTraveled);
        data.put("hitX", hitX);
        data.put("hitY", hitY);
        data.put("hit", hit);
        data.put("target", targetHit != null ? targetHit : "none");
        data.put("shotsUsed", shotsToConsume);
        data.put("startShots", startShots);
        data.put("remainingShots", shooter.getShots());
        data.put("outcome", hit ? "Hit " + targetHit : "Missed");

        // add robot state after firing
        result.set("state", new StateNode(robotName, getWorld()).execute());

        return result;
    }

    /**
     * Calculate the number of shots consumed based on distance.
     *
     * @param distance distance of the shot
     * @return shots to consume
     */
    private int calculateShotsConsumed(int distance) {
        return switch (distance) {
            case 5 -> 1;
            case 4 -> 2;
            case 3 -> 3;
            case 2 -> 4;
            case 1 -> 5;
            default -> 0;
        };
    }

    /**
     * Create a standard error response JSON.
     *
     * @param message error message to return
     * @return JSON node with error
     */
    private JsonNode makeErrorResponse(String message) {
        ObjectNode response = getMapper().createObjectNode();
        response.put("result", "ERROR");
        ObjectNode data = response.putObject("data");
        data.put("message", message);
        return response;
    }
}
