package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.world.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ServerCommandsTest {

    private World world;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        // Setup a minimal WorldConfig for testing
        Map<String, int[]> makes = new HashMap<>();
        makes.put("Sniper", new int[]{15, 5, 3});
        List<Obstacle> obstacles = new ArrayList<>();
        WorldConfig config = new WorldConfig(5, 3, 2, 4, makes, obstacles);
        world = new World(config);
        mapper = new ObjectMapper();
    }

    @Test
    void testServerQuitCommandNormal() {
        ServerQuitCommand quit = new ServerQuitCommand(world);
        JsonNode response = quit.execute();
        assertEquals("OK", response.get("result").asText());
        assertEquals("World terminated", response.get("data").get("message").asText());
    }

    @Test
    void testDumpCommandEmptyWorld() {
        DumpCommand dump = new DumpCommand(world);
        JsonNode response = dump.execute();

        assertEquals("OK", response.get("result").asText());
        JsonNode worldState = response.get("data").get("worldState");
        assertEquals(5, worldState.get("width").asInt());
        assertEquals(5, worldState.get("height").asInt());
        assertEquals(0, worldState.get("numRobots").asInt());
        assertEquals(0, worldState.get("robots").size());
        assertEquals(0, worldState.get("numObstacles").asInt());
        assertEquals(0, worldState.get("obstacles").size());
    }

    @Test
    void testDumpCommandWithRobots() {
        Robot r1 = new Robot("R1", "Sniper", 15, 5, 3);
        Robot r2 = new Robot("R2", "Sniper", 10, 5, 3);
        world.addRobot(r1);
        world.addRobot(r2);

        DumpCommand dump = new DumpCommand(world);
        JsonNode response = dump.execute();

        assertEquals(2, response.get("data").get("worldState").get("numRobots").asInt());
        assertTrue(response.get("data").get("worldState").get("robots").toString().contains("R1"));
        assertTrue(response.get("data").get("worldState").get("robots").toString().contains("R2"));
    }

    @Test
    void testRobotsCommandRemovesDeadRobots() {
        Robot r1 = new Robot("R1", "Sniper", 15, 5, 3);
        Robot r2 = new Robot("R2", "Sniper", 10, 5, 3);
        world.addRobot(r1);
        world.addRobot(r2);

        r2.setStatus("DEAD"); // edge case: dead robot
        RobotsCommand robotsCmd = new RobotsCommand(world);
        JsonNode response = robotsCmd.execute();

        assertEquals("OK", response.get("result").asText());
        JsonNode robotsArray = response.get("data").get("robots");
        assertEquals(1, robotsArray.size());
        assertEquals("R1", robotsArray.get(0).get("name").asText());
    }

    @Test
    void testRobotsCommandEmptyWorld() {
        RobotsCommand robotsCmd = new RobotsCommand(world);
        JsonNode response = robotsCmd.execute();
        assertEquals("OK", response.get("result").asText());
        assertEquals(0, response.get("data").get("robots").size());
    }

    @Test
    void testServerCommandsCreateValidAndInvalid() throws Exception {
        // Valid quit command
        ObjectNode validRequest = mapper.createObjectNode();
        validRequest.put("command", "quit");
        Command command = ServerCommands.create(validRequest, world);
        assertTrue(command instanceof ServerQuitCommand);

        // Invalid command
        ObjectNode invalidRequest = mapper.createObjectNode();
        invalidRequest.put("command", "nonexistent");
        command = ServerCommands.create(invalidRequest, world);
        JsonNode result = command.execute();
        assertEquals("ERROR", result.get("result").asText());
        assertTrue(result.get("data").get("message").asText().contains("Unsupported server command"));

        // Null request edge case
        command = ServerCommands.create(null, world);
        result = command.execute();
        assertEquals("ERROR", result.get("result").asText());
        assertTrue(result.get("data").get("message").asText().contains("Could not parse arguments"));
    }


    @Test
    void testRemoveDeadRobotsMultiple() {
        Robot r1 = new Robot("R1", "Sniper", 15, 5, 3);
        Robot r2 = new Robot("R2", "Sniper", 10, 5, 3);
        Robot r3 = new Robot("R3", "Sniper", 5, 5, 3);
        world.addRobot(r1);
        world.addRobot(r2);
        world.addRobot(r3);

        r1.setStatus("DEAD");
        r3.setStatus("DEAD");

        world.removeDeadRobots();

        List<Robot> remaining = world.getRobotsInWorld();
        assertEquals(1, remaining.size());
        assertEquals("R2", remaining.get(0).getName());
    }
}
