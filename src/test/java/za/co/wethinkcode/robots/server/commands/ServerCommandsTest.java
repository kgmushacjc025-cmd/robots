package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServerCommandsTest {

    @Test
    void testCreateDump() {
        World mockWorld = mock(World.class);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode request = mapper.createObjectNode();
        request.put("command", "dump");
        request.set("arguments", mapper.createObjectNode());  // empty args

        Command result = ServerCommands.create(request, mockWorld);

        assertNotNull(result);
        assertTrue(result instanceof DumpCommand);
    }

    @Test
    void testCreateQuit(){
        World mockWorld = mock(World.class);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode request = mapper.createObjectNode();
        request.put("command", "quit");
        request.set("arguments", mapper.createObjectNode());  // empty args

        Command result = ServerCommands.create(request, mockWorld);

        assertNotNull(result);
        assertTrue(result instanceof ServerQuitCommand);

    }
}
