package za.co.wethinkcode.robots.server.world;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WorldConfig {
    private final int size;
    private final Map<String, int[]> makes;
    private final List<Obstacle> obstacles;

    public WorldConfig(int size, Map<String, int[]> makes, List<Obstacle> obstacles) {
        this.size = size;
        this.makes = makes;
        this.obstacles = obstacles;
    }

    public int getSize() {
        return size;
    }

    public Map<String, int[]> getMakes() {
        return makes;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public static WorldConfig loadFromFile(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(path));

        int size = root.path("world").path("size").asInt();

        Map<String, int[]> makes = new HashMap<>();
        JsonNode makesNode = root.path("makes");
        makesNode.fieldNames().forEachRemaining(makeName -> {
            JsonNode makeData = makesNode.get(makeName);
            int shots = makeData.path("shots").asInt();
            int shields = makeData.path("shields").asInt();
            int maxshot = makeData.path("maxshot").asInt();
            makes.put(makeName, new int[]{shots, shields, maxshot});
        });

        JsonNode obstaclesRoot = root.path("obstacles");
        boolean defaultPlacement = obstaclesRoot.has("defaultPlacement")
                && obstaclesRoot.get("defaultPlacement").asBoolean();

        List<Obstacle> obstacles = new ArrayList<>();

        if (defaultPlacement) {
            obstacles.addAll(ObstacleGenerator.generate(size, size));
        }

        JsonNode obstacleTypes = obstaclesRoot.path("types");
        for (JsonNode obstacleNode : obstacleTypes) {
            String typeStr = obstacleNode.path("type").asText();
            int count = obstacleNode.path("count").asInt();
            int obstacleSize = obstacleNode.path("size").asInt();
            Obstacle.ObstacleType type = Obstacle.ObstacleType.fromString(typeStr);

            if (obstacleNode.has("positions") && obstacleNode.path("positions").size() > 0) {
                for (JsonNode posNode : obstacleNode.path("positions")) {
                    JsonNode topLeft = posNode.path("topLeft");
                    JsonNode topRight = posNode.path("topRight");
                    JsonNode bottomLeft = posNode.path("bottomLeft");
                    JsonNode bottomRight = posNode.path("bottomRight");

                    Obstacle candidate = new Obstacle(
                            type,
                            topLeft.path("x").asInt(), topLeft.path("y").asInt(),
                            topRight.path("x").asInt(), topRight.path("y").asInt(),
                            bottomLeft.path("x").asInt(), bottomLeft.path("y").asInt(),
                            bottomRight.path("x").asInt(), bottomRight.path("y").asInt()
                    );

                    if (!ObstacleGenerator.collides(candidate, obstacles)) {
                        obstacles.add(candidate);
                    }
                }
            } else if (count > 0) {
                for (int i = 0; i < count; i++) {
                    ObstacleGenerator.placeObstacle(type, size, size, obstacles, obstacleSize);
                }
            }
        }

        return new WorldConfig(size, makes, obstacles);
    }
}
