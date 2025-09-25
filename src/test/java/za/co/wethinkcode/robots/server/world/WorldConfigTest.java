package za.co.wethinkcode.robots.server.world;

import org.junit.jupiter.api.*;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorldConfigTest {

    private File tempFile;

    @BeforeEach
    void setup() throws Exception {
        tempFile = File.createTempFile("worldConfigFull", ".json");
        tempFile.deleteOnExit();

        String json = """
        {
          "world": {
            "size": 20,
            "visibility": 20,
            "repairTime": 3,
            "reloadTime": 5
          },
          "makes": {
            "Sniper": { "shots": 15, "shields": 5, "maxshot": 3 },
            "Tank": { "shots": 5, "shields": 15, "maxshot": 1 },
            "Defender": { "shots": 5, "shields": 5, "maxshot": 2 }
          },
          "obstacles": {
            "defaultPlacement": false,
            "types": [
              { "type": "pit", "count": 0, "size": 1, "positions": [] },
              { "type": "lake", "count": 1, "size": 3, "positions": [
                  {
                    "topLeft": { "x": -4, "y": 5 },
                    "topRight": { "x": -1, "y": 5 },
                    "bottomLeft": { "x": -4, "y": 2 },
                    "bottomRight": { "x": -1, "y": 2 }
                  }
              ]},
              { "type": "mountain", "count": 2, "size": 2, "positions": [
                  {
                    "topLeft": { "x": 1, "y": -1 },
                    "topRight": { "x": 3, "y": -1 },
                    "bottomLeft": { "x": 1, "y": -3 },
                    "bottomRight": { "x": 3, "y": -3 }
                  },
                  {
                    "topLeft": { "x": -6, "y": -4 },
                    "topRight": { "x": -4, "y": -4 },
                    "bottomLeft": { "x": -6, "y": -6 },
                    "bottomRight": { "x": -4, "y": -6 }
                  }
              ]}
            ]
          }
        }
        """;

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(json);
        }
    }

    @Test
    void testWorldProperties() throws Exception {
        WorldConfig config = WorldConfig.loadFromFile(tempFile.getAbsolutePath());

        assertEquals(20, config.getSize());
        assertEquals(20, config.getVisibility());
        assertEquals(3, config.getRepairTime());
        assertEquals(5, config.getReloadTime());
    }

    @Test
    void testRobotMakes() throws Exception {
        WorldConfig config = WorldConfig.loadFromFile(tempFile.getAbsolutePath());

        Map<String, int[]> makes = config.getMakes();
        assertEquals(3, makes.size());
        assertArrayEquals(new int[]{15,5,3}, makes.get("Sniper"));
        assertArrayEquals(new int[]{5,15,1}, makes.get("Tank"));
        assertArrayEquals(new int[]{5,5,2}, makes.get("Defender"));
    }

    @Test
    void testObstaclesPositions() throws Exception {
        WorldConfig config = WorldConfig.loadFromFile(tempFile.getAbsolutePath());
        List<Obstacle> obstacles = config.getObstacles();

        assertNotNull(obstacles);

        // Check counts by type
        long pitCount = obstacles.stream().filter(o -> o.getType() == Obstacle.ObstacleType.PIT).count();
        long lakeCount = obstacles.stream().filter(o -> o.getType() == Obstacle.ObstacleType.LAKE).count();
        long mountainCount = obstacles.stream().filter(o -> o.getType() == Obstacle.ObstacleType.MOUNTAIN).count();

        assertEquals(0, pitCount);
        assertEquals(1, lakeCount);
        assertEquals(2, mountainCount);


        // Validate one mountain
        Obstacle mountain1 = obstacles.stream().filter(o -> o.getType() == Obstacle.ObstacleType.MOUNTAIN)
                .filter(o -> o.getTopLeftX() == 1).findFirst().orElseThrow();
        assertEquals(-1, mountain1.getTopLeftY());
        assertEquals(3, mountain1.getTopRightX());
        assertEquals(-1, mountain1.getTopRightY());
        assertEquals(1, mountain1.getBottomLeftX());
        assertEquals(-3, mountain1.getBottomLeftY());
        assertEquals(3, mountain1.getBottomRightX());
        assertEquals(-3, mountain1.getBottomRightY());

    }

    @Test
    void testVisibilityClampedToSize() throws Exception {
        String json = """
        {
            "world": { "size": 10, "visibility": 50 },
            "makes": {},
            "obstacles": { "defaultPlacement": false, "types": [] }
        }
        """;
        try (FileWriter writer = new FileWriter(tempFile)) { writer.write(json); }

        WorldConfig config = WorldConfig.loadFromFile(tempFile.getAbsolutePath());
        assertEquals(10, config.getVisibility()); // clamped
    }

    @Test
    void testDefaultPlacementGeneratesObstacles() throws Exception {
        String json = """
        {
            "world": { "size": 10, "visibility": 5 },
            "makes": {},
            "obstacles": { "defaultPlacement": true, "types": [] }
        }
        """;

        try (FileWriter writer = new FileWriter(tempFile)) { writer.write(json); }

        WorldConfig config = WorldConfig.loadFromFile(tempFile.getAbsolutePath());
        List<Obstacle> obstacles = config.getObstacles();

        assertNotNull(obstacles);
        int size = config.getSize();
        int perType = ObstacleGenerator.obstaclesPerType(size, size);
        int expectedTotal = perType * 3;
        assertEquals(expectedTotal, obstacles.size(), "Total obstacles count should match formula");

        long pitCount = obstacles.stream().filter(o -> o.getType() == Obstacle.ObstacleType.PIT).count();
        long lakeCount = obstacles.stream().filter(o -> o.getType() == Obstacle.ObstacleType.LAKE).count();
        long mountainCount = obstacles.stream().filter(o -> o.getType() == Obstacle.ObstacleType.MOUNTAIN).count();

        assertTrue(pitCount > 0, "Should have PIT obstacles");
        assertTrue(lakeCount > 0, "Should have LAKE obstacles");
        assertTrue(mountainCount > 0, "Should have MOUNTAIN obstacles");
    }

}
