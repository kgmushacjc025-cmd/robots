package za.co.wethinkcode.robots.server.commands;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientCommandsTest {

    @Test
    void testCreateLaunch() {
        World mockWorld = mock(World.class);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode request = mapper.createObjectNode();
        request.put("command", "look");
        request.set("arguments", mapper.createObjectNode());

        String robotName = "Hal";
        Command result = ClientCommands.create(request, mockWorld, robotName);

        assertNotNull(result);
        assertTrue(result instanceof LookCommand);
    }

    @Test
    void testCreateQuit(){
        World mockWorld = mock(World.class);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode request = mapper.createObjectNode();
        request.put("command", "quit");
        request.set("arguments", mapper.createObjectNode());

        String robotName = "Hal";
        Command result = ClientCommands.create(request, mockWorld, robotName);

        assertNotNull(result);
        assertTrue(result instanceof ClientQuitCommand);

    }
}
