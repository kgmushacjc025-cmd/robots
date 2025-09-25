package za.co.wethinkcode.robots.client.connection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.client.command.CommandBuilder;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServerConnectionTest {

    private Socket mockSocket;
    private PrintWriter mockWriter;
    private BufferedReader mockReader;
    private CommandBuilder mockBuilder;
    private ServerConnection connection;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockSocket = mock(Socket.class);
        mockWriter = mock(PrintWriter.class);
        mockReader = mock(BufferedReader.class);
        mockBuilder = mock(CommandBuilder.class);
        mapper = new ObjectMapper();

        connection = new ServerConnection(mockSocket, mockBuilder, mockReader, mockWriter);
    }

    @Test
    void testSendCommandSuccess() throws Exception {
        String input = "state";
        JsonNode requestNode = mapper.createObjectNode().put("command", "state");
        when(mockBuilder.buildCommand(input)).thenReturn(requestNode);
        when(mockReader.readLine()).thenReturn("{\"result\":\"OK\"}");

        String response = connection.sendCommand(input);

        assertTrue(response.contains("\"Result\" : \"OK\""));
        verify(mockWriter).println(anyString());
    }

    @Test
    void testSendCommandInvalid() throws Exception {
        when(mockBuilder.buildCommand("bad")).thenReturn(null);

        String response = connection.sendCommand("bad");

        assertEquals("Invalid command format", response);
        verifyNoInteractions(mockWriter);
    }

    @Test
    void testSendCommandServerDisconnected() throws Exception {
        String input = "state";
        JsonNode requestNode = mapper.createObjectNode().put("command", "state");
        when(mockBuilder.buildCommand(input)).thenReturn(requestNode);
        when(mockReader.readLine()).thenReturn(null);

        Exception ex = assertThrows(Exception.class, () -> connection.sendCommand(input));
        assertEquals("Server disconnected", ex.getMessage());
        verify(mockWriter).println(anyString());
    }

    // -------------------- Private helper tests --------------------

    @Test
    void testAddRobots() throws Exception {
        String json = "{\"robots\":[{\"name\":\"Robo1\",\"position\":[1,2],\"direction\":\"N\",\"shields\":5,\"shots\":3,\"status\":\"OK\"}]}";
        JsonNode data = mapper.readTree(json);
        ObjectNode dataNode = mapper.createObjectNode();

        Method method = ServerConnection.class.getDeclaredMethod("addRobots", JsonNode.class, ObjectNode.class);
        method.setAccessible(true);
        method.invoke(connection, data, dataNode);

        assertTrue(dataNode.has("Robots"));
        assertEquals("Robo1", dataNode.get("Robots").get(0).get("name").asText());
    }

    @Test
    void testAddObstacles() throws Exception {
        String json = "{\"obstacles\":[{\"obstacleType\":\"Wall\",\"corners\":{\"topLeft\":{\"x\":0,\"y\":0}},\"canKillYou\":true,\"canWalkThrough\":false,\"canSeePast\":true}]}";
        JsonNode data = mapper.readTree(json);
        ObjectNode dataNode = mapper.createObjectNode();

        Method method = ServerConnection.class.getDeclaredMethod("addObstacles", JsonNode.class, ObjectNode.class);
        method.setAccessible(true);
        method.invoke(connection, data, dataNode);

        assertTrue(dataNode.has("Obstacles"));
        assertEquals("Wall", dataNode.get("Obstacles").get(0).get("type").asText());
        assertTrue(dataNode.get("Obstacles").get(0).get("canKillYou").asBoolean());
    }

    @Test
    void testAddNumbers() throws Exception {
        String json = "{\"maxDistance\":10,\"distanceTraveled\":5,\"shotsUsed\":3,\"steps\":2,\"shields\":7}";
        JsonNode data = mapper.readTree(json);
        ObjectNode dataNode = mapper.createObjectNode();

        Method method = ServerConnection.class.getDeclaredMethod("addNumbers", JsonNode.class, ObjectNode.class);
        method.setAccessible(true);
        method.invoke(connection, data, dataNode);

        assertEquals(10, dataNode.get("maxDistance").asInt());
        assertEquals(5, dataNode.get("distanceTraveled").asInt());
        assertEquals(7, dataNode.get("shields").asInt());
    }
}
