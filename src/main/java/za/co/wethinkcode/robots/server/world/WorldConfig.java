package za.co.wethinkcode.robots.server.world;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Represents the configuration of a World, including its size, robot makes, and obstacles.
 * Supports loading configuration from a JSON file.
 */
public class WorldConfig {

    private final int size;
    private final int visibility;
    private final int repairTime;
    private final int reloadTime;
    private final Map<String, int[]> makes;
    private final List<Obstacle> obstacles;

    public WorldConfig(int size, int visibility, int repairTime, int reloadTime, Map<String, int[]> makes, List<Obstacle> obstacles) {
        this.size = size;
        this.visibility = visibility;
        this.repairTime = repairTime;
        this.reloadTime = reloadTime;
        this.makes = makes;
        this.obstacles = obstacles;
    }

    public int getSize() { return size; }
    public int getVisibility() { return visibility; }
    public int getRepairTime() { return repairTime; }
    public int getReloadTime() { return reloadTime; }
    public Map<String, int[]> getMakes() { return makes; }
    public List<Obstacle> getObstacles() { return obstacles; }

    /**
     * Loads a WorldConfig from a JSON file.
     *
     * @param path Path to the JSON configuration file.
     * @return a configured WorldConfig object
     * @throws IOException if the file cannot be read
     */
    public static WorldConfig loadFromFile(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(path));

        JsonNode worldNode = root.path("world");
        int size = worldNode.path("size").asInt();

        // Load visibility, clamp to world size if necessary
        int visibility = worldNode.path("visibility").asInt(20); // default 20
        if (visibility > size) {
            visibility = size;
        }

        int repairTime = worldNode.path("repairTime").asInt(3);  // default 3
        int reloadTime = worldNode.path("reloadTime").asInt(5);  // default 5

        Map<String, int[]> makes = parseMakes(root.path("makes"));
        List<Obstacle> obstacles = parseObstacles(root.path("obstacles"), size);

        return new WorldConfig(size, visibility, repairTime, reloadTime, makes, obstacles);
    }

    /**
     * Parses the "makes" JSON node into a map of make names to stats array.
     *
     * @param makesNode JSON node containing makes
     * @return Map where key = make name, value = [shots, shields, maxshot]
     */
    private static Map<String, int[]> parseMakes(JsonNode makesNode) {
        Map<String, int[]> makes = new HashMap<>();
        makesNode.fieldNames().forEachRemaining(makeName -> {
            JsonNode makeData = makesNode.get(makeName);
            int shots = makeData.path("shots").asInt();
            int shields = makeData.path("shields").asInt();
            int maxshot = makeData.path("maxshot").asInt();
            makes.put(makeName, new int[]{shots, shields, maxshot});
        });
        return makes;
    }

    /**
     * Parses obstacles from JSON and returns a list of obstacles.
     *
     * @param obstaclesRoot JSON node containing obstacles config
     * @param worldSize     size of the world (width = height = worldSize)
     * @return list of obstacles
     */
    private static List<Obstacle> parseObstacles(JsonNode obstaclesRoot, int worldSize) {
        List<Obstacle> obstacles = new ArrayList<>();

        boolean defaultPlacement = obstaclesRoot.has("defaultPlacement")
                && obstaclesRoot.get("defaultPlacement").asBoolean();
        if (defaultPlacement) {
            obstacles.addAll(ObstacleGenerator.generate(worldSize, worldSize));
        }

        JsonNode obstacleTypes = obstaclesRoot.path("types");
        for (JsonNode obstacleNode : obstacleTypes) {
            Obstacle.ObstacleType type = Obstacle.ObstacleType.fromString(obstacleNode.path("type").asText());
            int count = obstacleNode.path("count").asInt();
            int obstacleSize = obstacleNode.path("size").asInt();

            addFixedObstacles(obstacleNode, type, obstacles);
            addRandomObstacles(obstacleNode, type, count, obstacleSize, worldSize, obstacles);
        }

        return obstacles;
    }

    /**
     * Adds obstacles with fixed positions.
     */
    private static void addFixedObstacles(JsonNode obstacleNode, Obstacle.ObstacleType type, List<Obstacle> obstacles) {
        if (!obstacleNode.has("positions")) return;
        for (JsonNode posNode : obstacleNode.path("positions")) {
            Obstacle candidate = new Obstacle(
                    type,
                    posNode.path("topLeft").path("x").asInt(), posNode.path("topLeft").path("y").asInt(),
                    posNode.path("topRight").path("x").asInt(), posNode.path("topRight").path("y").asInt(),
                    posNode.path("bottomLeft").path("x").asInt(), posNode.path("bottomLeft").path("y").asInt(),
                    posNode.path("bottomRight").path("x").asInt(), posNode.path("bottomRight").path("y").asInt()
            );
            if (!ObstacleGenerator.collides(candidate, obstacles)) {
                obstacles.add(candidate);
            }
        }
    }

    /**
     * Adds randomly placed obstacles according to the count specified.
     */
    private static void addRandomObstacles(JsonNode obstacleNode, Obstacle.ObstacleType type, int count, int size,
                                           int worldSize, List<Obstacle> obstacles) {
        if (obstacleNode.has("positions") || count <= 0) return;
        for (int i = 0; i < count; i++) {
            ObstacleGenerator.placeObstacle(type, worldSize, worldSize, obstacles, size);
        }
    }
}
