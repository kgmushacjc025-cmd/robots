package za.co.wethinkcode.robots.server.world;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.*;

/**
 * Represents the game world, containing robots and obstacles.
 * Provides methods for adding/removing robots and querying world state.
 */
public class World {

    private final int width;
    private final int height;
    private final WorldConfig config;
    private final Map<String, Robot> robotsMap; // preserves insertion order
    private final List<Robot> robots;
    private final List<Obstacle> obstacles;
    private final Map<String, int[]> makes; // makeName -> [shots, shields, maxShots]

    /**
     * Constructs a new World based on the given configuration.
     *
     * @param config The configuration object defining world size, obstacles, and robot makes.
     */
    public World(WorldConfig config) {
        this.config = config;
        this.width = config.getSize();
        this.height = config.getSize();
        this.robotsMap = new LinkedHashMap<>();
        this.robots = new ArrayList<>();
        this.obstacles = config.getObstacles();
        this.makes = config.getMakes();
    }

    /**
     * Attempts to add a robot at a random valid position in the world.
     *
     * @param robot The robot to add.
     * @return true if added successfully; false if the world is full or no valid position found.
     */
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

    /**
     * Returns a JSON representation of the world's current state, including all robots and obstacles.
     *
     * @return JsonNode representing world state.
     */
    public JsonNode getWorldState() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode worldNode = mapper.createObjectNode();

        worldNode.put("width", width);
        worldNode.put("height", height);

        // Robots
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

        // Obstacles
        ArrayNode obstacleArray = mapper.createArrayNode();
        for (Obstacle o : obstacles) {
            ObjectNode obsNode = mapper.createObjectNode();
            obsNode.put("obstacleType", o.getType().name());

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

    /**
     * Helper method to create a JSON point object.
     *
     * @param mapper ObjectMapper instance.
     * @param x      X-coordinate.
     * @param y      Y-coordinate.
     * @return ObjectNode representing the point.
     */
    private ObjectNode createPointNode(ObjectMapper mapper, int x, int y) {
        ObjectNode node = mapper.createObjectNode();
        node.put("x", x);
        node.put("y", y);
        return node;
    }

    /**
     * Checks whether a robot name is already in use.
     *
     * @param name The robot name to check.
     * @return true if taken; false otherwise.
     */
    private boolean isRobotNameTaken(String name) {
        return robots.stream().anyMatch(r -> r.getName().equals(name));
    }

    /**
     * Returns the map of available robot makes.
     *
     * @return Map of makes.
     */
    public Map<String, int[]> getMakes(){
        return makes;
    }

    /**
     * Retrieves a robot by name.
     *
     * @param name The robot's name.
     * @return Robot object or null if not found.
     */
    public Robot getRobot(String name) {
        return robotsMap.get(name);
    }

    /**
     * Returns a list of all robot names in the world.
     *
     * @return List of names.
     */
    public List<String> getRobotNames() {
        return new ArrayList<>(robotsMap.keySet());
    }

    /**
     * Removes a robot from the world by name.
     *
     * @param robotName Name of the robot to remove.
     */
    public void removeOneRobot(String robotName) {
        Robot removed = robotsMap.remove(robotName);
        if(removed != null){
            robots.remove(removed);
        }
    }

    /**
     * Removes all robots from the world.
     */
    public void clearRobots() {
        robotsMap.clear();
        robots.clear();
    }

    /**
     * Returns a list of all robots currently in the world.
     *
     * @return List of robots.
     */
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
