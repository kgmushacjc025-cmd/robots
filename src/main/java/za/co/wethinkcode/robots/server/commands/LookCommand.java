package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.ObjectInView;
import za.co.wethinkcode.robots.server.world.Obstacle;
import za.co.wethinkcode.robots.server.world.Robot;
import za.co.wethinkcode.robots.server.world.World;

import java.util.ArrayList;
import java.util.List;

public class LookCommand extends ClientCommands {
    private final String robotName;
    private final World gameWorld;
    private final ObjectMapper mapper;

    public LookCommand(String robotName, World gameWorld) {
        super(robotName, gameWorld);
        this.robotName = robotName;
        this.gameWorld = gameWorld;
        this.mapper = new ObjectMapper();
    }

    @Override
    public JsonNode execute() {
        Robot robot = gameWorld.getRobot(robotName);
        List<ObjectInView> objects = look(robot);

        ObjectNode response = mapper.createObjectNode();
        response.put("result", "OK");

        ObjectNode data = mapper.createObjectNode();
        ArrayNode objectsArray = mapper.createArrayNode();

        for (ObjectInView obj : objects) {
            ObjectNode objNode = mapper.createObjectNode();
            objNode.put("direction", obj.direction);
            objNode.put("type", obj.type);
            objNode.put("distance", obj.distance);
            objectsArray.add(objNode);
        }

        data.set("objects", objectsArray);
        response.set("data", data);
        response.set("state", new StateNode(robotName, gameWorld).execute());

        return response;
    }


    public List<ObjectInView> look(Robot robot) {
        List<ObjectInView> objects = new ArrayList<>();
        int x = robot.getX();
        int y = robot.getY();

        for (String direction : new String[]{"NORTH", "SOUTH", "EAST", "WEST"}) {
            objects.addAll(lookInDirection(x, y, direction));
        }
        return objects;
    }

    private List<ObjectInView> lookInDirection(int x, int y, String direction) {
        List<ObjectInView> objects = new ArrayList<>();
        int width = gameWorld.worldWidth();
        int height = gameWorld.worldHeight();

        List<Robot> robots = gameWorld.getRobotsInWorld();
        List<Obstacle> obstacles = gameWorld.getWorldObstacles();

        int dx = 0, dy = 0;
        switch (direction) {
            case "NORTH": dy = 1; break;
            case "SOUTH": dy = -1; break;
            case "EAST":  dx = 1; break;
            case "WEST":  dx = -1; break;
        }

        int distance = 1;
        while (true) {
            int newX = x + dx * distance;
            int newY = y + dy * distance;

            // look out for edge
            if (newX < -(width / 2) || newX >= width / 2 ||
                    newY < -(height / 2) || newY >= height / 2) {
                objects.add(new ObjectInView(direction, "EDGE", distance));
                break;
            }

            // Robots check
            for (Robot r : robots) {
                if (r.getX() == newX && r.getY() == newY) {
                    objects.add(new ObjectInView(direction, "ROBOT", distance));
                    return objects;
                }
            }

            // obstacle check
            for (Obstacle o : obstacles) {
                if (o.blocksPosition(newX, newY)) {
                    String type = o.getType().toString();
                    String obstacleType = Obstacle.ObstacleType.fromString(type).toString();
                    objects.add(new ObjectInView(direction, obstacleType.toUpperCase(), distance));
                    if (!o.canSeePast()) {
                        return objects;
                    }
                }
            }

            distance++;
        }
        return objects;
    }
}
