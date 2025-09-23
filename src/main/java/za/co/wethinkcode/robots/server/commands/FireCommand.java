package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;

import java.util.List;

public class FireCommand extends ClientCommands {
    private final String robotName;

    public FireCommand(String robotName, JsonNode arguments, World gameWorld) {
        super(robotName, gameWorld);
        this.robotName = robotName;
    }

    @Override
    public JsonNode execute() {
        Robot shooter = getWorld().getRobot(robotName);
        if (shooter == null || !shooter.canFire()) {
            return makeErrorResponse("Cannot fire: no shots remaining. Consider reloading.");
        }

        int maxDistance = shooter.getMaxShotDistance();
        int shotsToConsume = calculateShotsConsumed(maxDistance);
        int startShots = shooter.getShots();

        // Check ammo BEFORE firing
        if (startShots < shotsToConsume) {
            return makeErrorResponse(
                    "Not enough shots to fire at distance " + maxDistance +
                            " (need " + shotsToConsume + ", have " + startShots + "). " +
                            "Please reload before firing again."
            );
        }

        // Consume shots now
        shooter.consumeShots(shotsToConsume);

        int startX = shooter.getX();
        int startY = shooter.getY();

        // Determine bullet direction
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

        // Simulate the shot path
        for (int i = 1; i <= maxDistance; i++) {
            x += dx;
            y += dy;
            distanceTraveled = i;
            hitX = x;
            hitY = y;

            List<Robot> robots = getWorld().getRobotsInWorld();
            for (Robot target : robots) {
                if (target != shooter && target.getX() == x && target.getY() == y) {
                    target.damage(shotsToConsume);
                    hit = true;
                    targetHit = target.getName();
                    if ("DEAD".equals(target.getStatus())) {
                        getWorld().removeOneRobot(target.getName());
                    }
                    break;
                }
            }
            if (hit) break;
        }

        // Build response JSON
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

        result.set("state", new StateNode(robotName, getWorld()).execute());

        return result;
    }

    private int calculateShotsConsumed(int distance) {
        return switch (distance) {
            case 5 -> 1;
            case 4 -> 2;
            case 3 -> 3;
            case 2 -> 4;
            case 1 -> 5;
            default -> 0; // cannot fire if 0
        };
    }

    private JsonNode makeErrorResponse(String message) {
        ObjectNode response = getMapper().createObjectNode();
        response.put("result", "ERROR");
        ObjectNode data = response.putObject("data");
        data.put("message", message);
        return response;
    }
}
