package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import za.co.wethinkcode.robots.server.networking.ClientHandler;
import za.co.wethinkcode.robots.server.world.World;


import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ClientHandlerTest {
    @Test
    void testConstructor() {
        Socket socket = Mockito.mock(Socket.class);
        World world = Mockito.mock(World.class);
        ClientHandler handler = new ClientHandler(socket, world);
        assertNotNull(handler);
    }
}
