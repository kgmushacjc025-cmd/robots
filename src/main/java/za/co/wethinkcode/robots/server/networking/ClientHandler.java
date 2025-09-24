package za.co.wethinkcode.robots.server.networking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import za.co.wethinkcode.robots.server.commands.ClientCommands;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.world.World;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles communication with a single client.
 * Receives JSON commands, executes them, and sends JSON responses.
 * Tracks the robotName for this client and handles robot death/disconnection.
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final World gameWorld;
    private final ObjectMapper mapper;
    private String robotName;

    /**
     * Constructs a ClientHandler for a given socket and world.
     *
     * @param clientSocket The client's socket connection.
     * @param gameWorld    The world instance shared across clients.
     */
    public ClientHandler(Socket clientSocket, World gameWorld) {
        this.clientSocket = clientSocket;
        this.gameWorld = gameWorld;
        this.mapper = new ObjectMapper();
        this.robotName = null;
    }

    /**
     * Main run loop for the client handler.
     * Reads JSON commands, executes them, sends JSON responses.
     * Updates robotName after launch and closes connection if robot dies.
     */
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Parse client input
                JsonNode request = mapper.readTree(inputLine);

                // Create appropriate command
                Command command = ClientCommands.create(request, gameWorld, robotName);

                // Execute command and build response
                JsonNode response = command.execute();

                // Send response back to client
                String jsonResponse = mapper.writeValueAsString(response);
                out.println(jsonResponse);
                out.flush();

                // Capture robotName if command launched a robot
                if (response.has("result") && "OK".equals(response.get("result").asText())) {
                    JsonNode dataNode = response.get("data");
                    if (dataNode != null && dataNode.has("robotName")) {
                        robotName = dataNode.get("robotName").asText();
                    }
                }

                // Close connection if robot is dead
                if (response.has("state") &&
                        response.get("state").has("dead") &&
                        response.get("state").get("dead").asBoolean()) {

                    System.out.println("Robot " + (robotName != null ? robotName : "") + " died. Closing connection...");
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (Exception ignored) {}
        }
    }
}
