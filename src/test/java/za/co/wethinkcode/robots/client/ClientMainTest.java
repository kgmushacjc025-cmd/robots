package za.co.wethinkcode.robots.client;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import za.co.wethinkcode.robots.client.connection.ServerConnection;

class ClientMainTest {
	@Test
	void testServerConnectionConstructor() throws Exception {
		// Mock socket and streams for ServerConnection
		Socket socket = Mockito.mock(Socket.class);
		// We cannot inject the socket directly, but we can test error handling
		Exception ex = assertThrows(Exception.class, () -> {
			new ServerConnection("invalidhost", 9999);
		});
		assertNotNull(ex);
	}

	@Test
	void testClientMainCommandClassExists() {
		ClientMain.ClientCommand command = new ClientMain.ClientCommand();
		assertNotNull(command);
	}
}