package za.co.wethinkcode.robots.server.world;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.*;

public class World {

    private final int width;
    private final int height;
    private final WorldConfig config;
    private final Map<String, Robot> robotsMap; // preserves insertion order
    private final List<Robot> robots;
    private final List<Obstacle> obstacles;
    private final Map<String, int[]> makes;       // makeName -> [shots, shields, maxShots]

    // --- Constructor that accepts a WorldConfig ---
    public World(WorldConfig config) {
        this.config = config;
        this.width = config.getSize();
        this.height = config.getSize();
        this.robotsMap = new LinkedHashMap<>(); // preserves insertion order
        this.robots = new ArrayList<>();
        this.obstacles = config.getObstacles();
        this.makes = config.getMakes();
    }

    public boolean addRobot(Robot robot){
        Random random = new Random();
        int maxAttempts = width * height;
        while (maxAttempts-- > 0) {
            int x = random.nextInt(width) - (width / 2);
            int y = random.nextInt(height) - (height / 2);
            Position position = new Position(x, y);
            boolean valid = position.isPositionValid(width, height, obstacles, robots);
            if (valid && !isRobotNameTaken(robot.getName())) {
                robot.setPosition(x, y);
                robots.add(robot);
                robotsMap.put(robot.getName(), robot);
                return true;
            }
        }
        return false;
    }

    public JsonNode getWorldState() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode worldNode = mapper.createObjectNode();

        worldNode.put("width", width);
        worldNode.put("height", height);

        ArrayNode robotArray = mapper.createArrayNode();
        for (Robot robot : robotsMap.values()) {
            ObjectNode robotNode = mapper.createObjectNode();
            robotNode.put("name", robot.getName());
            robotNode.put("x", robot.getX());
            robotNode.put("y", robot.getY());
            robotArray.add(robotNode);
        }
        worldNode.put("numRobots", robotsMap.size());
        worldNode.set("robots", robotArray);

        ArrayNode obstacleArray = mapper.createArrayNode();
        for (Obstacle o : obstacles) {
            ObjectNode obsNode = mapper.createObjectNode();
            obsNode.put("obstacleType", o.getType().name());

            // Group corners together for neat JSON output
            ObjectNode cornersNode = mapper.createObjectNode();
            cornersNode.set("topLeft", createPointNode(mapper, o.getTopLeftX(), o.getTopLeftY()));
            cornersNode.set("topRight", createPointNode(mapper, o.getTopRightX(), o.getTopRightY()));
            cornersNode.set("bottomLeft", createPointNode(mapper, o.getBottomLeftX(), o.getBottomLeftY()));
            cornersNode.set("bottomRight", createPointNode(mapper, o.getBottomRightX(), o.getBottomRightY()));
            obsNode.set("corners", cornersNode);

            obsNode.put("canKillYou", o.canKillYou());
            obsNode.put("canWalkThrough", o.canWalkThrough());
            obsNode.put("canSeePast", o.canSeePast());
            obstacleArray.add(obsNode);
        }
        worldNode.put("numObstacles", obstacles.size());
        worldNode.set("obstacles", obstacleArray);

        return worldNode;
    }

    private ObjectNode createPointNode(ObjectMapper mapper, int x, int y) {
        ObjectNode node = mapper.createObjectNode();
        node.put("x", x);
        node.put("y", y);
        return node;
    }

    private boolean isRobotNameTaken(String name) {
        return robots.stream().anyMatch(r -> r.getName().equals(name));
    }

    public Map<String, int[]> getMakes(){
        return makes;
    }

    public Robot getRobot(String name) {
        return robotsMap.get(name);
    }

    public List<String> getRobotNames() {
        return new ArrayList<>(robotsMap.keySet());
    }

    // removes a robot by name
    public void removeOneRobot(String robotName) {
        Robot removed = robotsMap.remove(robotName);
        if(removed != null){
            robots.remove(removed);
        }
    }

    // clears all robots
    public void clearRobots() {
        robotsMap.clear();
        robots.clear();
    }

    public List<Robot> getRobotsInWorld(){
        return robots;
    }

    public List<Obstacle> getWorldObstacles(){
        return obstacles;
    }

    public int worldWidth(){
        return width;
    }

    public int worldHeight(){
        return height;
    }
}
