package za.co.wethinkcode.robots.client;

import za.co.wethinkcode.robots.client.connection.ServerConnection;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.Scanner;

/**
 * This is the client program for Robot World
 * - connects to the server
 * - lets you type commands in the console
 * - sends commands to the server and shows the response
 * - closes when you quit or when you die
 */
public class ClientMain {
    private ServerConnection connection;

    /**
     * just holds the command line options like host and port
     * default host is 127.0.0.1 and port is 5000
     */
    @CommandLine.Command(name = "client", description = "Robot World Client")
    static class ClientCommand {
        @Option(names = {"--host"}, description = "Server host", defaultValue = "127.0.0.1")
        private String host;

        @Option(names = {"--port"}, description = "Server port", defaultValue = "5000")
        private int port;
    }

    /**
     * main entry point
     * here we read args, try connect to server, and start the run loop
     */
    public static void main(String[] args) {
        ClientCommand command = new ClientCommand();
        new CommandLine(command).parseArgs(args);

        ClientMain client = new ClientMain();
        try {
            client.connection = new ServerConnection(command.host, command.port);
            System.out.println("Connected to server at " + command.host + ":" + command.port);
            client.run();
        } catch (Exception e) {
            System.err.println("Could not connect to server: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * main loop for the client
     * keeps asking for commands until you quit or die
     */
    private void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter commands (like: 'launch sniper Hal', 'look', 'state', 'quit' to exit):");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            // if handleUserInput says false, then break out of the loop
            if (!handleUserInput(input)) {
                break;
            }
        }

        closeResources(scanner);
        System.out.println("Client closed.");
    }

    /**
     * send one command to the server and show the response
     * returns false if we should stop running
     */
    private boolean handleUserInput(String input) {
        try {
            String response = connection.sendCommand(input);
            System.out.println(response);

            // if the server says YOU DIED then stop
            if (response.toUpperCase().contains("YOU DIED")) {
                System.out.println("Game over, closing...");
                return false;
            }

            // quit if user typed quit
            if (input.equalsIgnoreCase("quit")) {
                return false;
            }

            return true; // keep running
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false; // stop on error
        }
    }

    /**
     * closes the server connection and the scanner
     */
    private void closeResources(Scanner scanner) {
        try {
            connection.close();
        } catch (Exception ignored) {
        }
        scanner.close();
    }
}
