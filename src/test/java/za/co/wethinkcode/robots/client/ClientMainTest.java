package za.co.wethinkcode.robots.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import za.co.wethinkcode.robots.client.connection.ServerConnection;

import java.net.Socket;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientMainTest {

	private ClientMain client;
	private ServerConnection mockConnection;

	@BeforeEach
	void setup() throws Exception {
		client = new ClientMain();
		mockConnection = Mockito.mock(ServerConnection.class);

		// inject mock into client via reflection
		var field = ClientMain.class.getDeclaredField("connection");
		field.setAccessible(true);
		field.set(client, mockConnection);
	}


	@Test
	void testServerConnectionConstructor() throws Exception {
		// try connect to invalid host should throw
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

	// --- new tests for ClientMain ---

	@Test
	void testHandleUserInputNormalCommand() throws Exception {
		when(mockConnection.sendCommand("look")).thenReturn("OK");

		boolean keepRunning = invokeHandle("look");

		assertTrue(keepRunning); // should not quit
		verify(mockConnection).sendCommand("look");
	}

	@Test
	void testHandleUserInputQuitCommand() throws Exception {
		when(mockConnection.sendCommand("quit")).thenReturn("bye");

		boolean keepRunning = invokeHandle("quit");

		assertFalse(keepRunning); // should quit
	}

	@Test
	void testHandleUserInputDeathResponse() throws Exception {
		when(mockConnection.sendCommand("forward 4")).thenReturn("YOU DIED");

		boolean keepRunning = invokeHandle("forward 4");

		assertFalse(keepRunning); // should quit
	}

	@Test
	void testCloseResources() throws Exception {
		Scanner scanner = new Scanner("test");
		doNothing().when(mockConnection).close();

		var method = ClientMain.class.getDeclaredMethod("closeResources", Scanner.class);
		method.setAccessible(true);
		method.invoke(client, scanner);

		verify(mockConnection).close();
		assertTrue(scanner.ioException() == null); // scanner closed cleanly
	}

	// helper: call private handleUserInput
	private boolean invokeHandle(String input) throws Exception {
		var method = ClientMain.class.getDeclaredMethod("handleUserInput", String.class);
		method.setAccessible(true);
		return (boolean) method.invoke(client, input);
	}
}
