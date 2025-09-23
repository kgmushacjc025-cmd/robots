package za.co.wethinkcode.robots.client;

import za.co.wethinkcode.robots.client.connection.ServerConnection;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.Scanner;

public class ClientMain {
    private ServerConnection connection;

    @CommandLine.Command(name = "client", description = "Robot World Client")
    static class ClientCommand {
        @Option(names = {"--host"}, description = "Server host", defaultValue = "127.0.0.1")
        private String host;

        @Option(names = {"--port"}, description = "Server port", defaultValue = "5000")
        private int port;
    }

    public static void main(String[] args) {
        // Parse command-line arguments
        ClientCommand command = new ClientCommand();
        new CommandLine(command).parseArgs(args);

        // Initialize server connection
        ClientMain client = new ClientMain();
        try {
            client.connection = new ServerConnection(command.host, command.port);
            System.out.println("Connected to server at " + command.host + ":" + command.port);
            client.run();
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            System.exit(1);
        }
    }

    private void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter commands (e.g., 'launch sniper Hal', 'look', 'state', 'quit' to exit):");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            try {
                // Send command to server
                String response = connection.sendCommand(input);
                System.out.println(response);

                // Break if response contains YOU DIED
                if (response.toUpperCase().contains("YOU DIED")) {
                    System.out.println("Game over. Closing connection...");
                    break;
                }

                // Break if user types quit
                if (input.equalsIgnoreCase("quit")) {
                    break;
                }

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                break; // Exit loop on connection error
            }
        }

        // Clean up AFTER loop ends
        try {
            connection.close();
        } catch (Exception ignored) {}
        scanner.close();
        System.out.println("Client terminated.");
    }
}
