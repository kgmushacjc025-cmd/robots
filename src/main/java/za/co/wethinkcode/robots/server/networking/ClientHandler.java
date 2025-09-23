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

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final World gameWorld;
    private final ObjectMapper mapper;
    private String robotName;

    public ClientHandler(Socket clientSocket, World gameWorld) {
        this.clientSocket = clientSocket;
        this.gameWorld = gameWorld;
        this.mapper = new ObjectMapper();
        this.robotName = null;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                JsonNode request = mapper.readTree(inputLine);
                Command command = ClientCommands.create(request, gameWorld, robotName);
                JsonNode response = command.execute();

                // Send the JSON response
                String jsonResponse = mapper.writeValueAsString(response);
                out.println(jsonResponse);
                out.flush();

                // Capture robotName after successful launch
                if (response.has("result") && response.get("result").asText().equals("OK")) {
                    JsonNode dataNode = response.get("data");
                    if (dataNode != null && dataNode.has("robotName")) {
                        robotName = dataNode.get("robotName").asText();
                    }
                }

                // 🚨 Break loop & close connection if robot is dead
                if (response.has("state") &&
                        response.get("state").has("dead") &&
                        response.get("state").get("dead").asBoolean()) {

                    System.out.println("Robot " + (robotName != null ? robotName : "") + " died. Closing connection...");
                    break; // exit the while-loop
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
