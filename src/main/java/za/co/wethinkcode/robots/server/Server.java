package za.co.wethinkcode.robots.server;

import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.commands.ServerCommands;
import za.co.wethinkcode.robots.server.networking.ClientHandler;
import za.co.wethinkcode.robots.server.world.World;
import za.co.wethinkcode.robots.server.world.WorldConfig;

public class Server {

    @CommandLine.Command(name = "server", description = "Robot World Server")
    static class ServerCommand {
        @Option(names = {"-p", "--port"}, description = "Port to listen on", defaultValue = "5000")
        private int port;

        @Option(names = {"-c", "--config"}, description = "Path to world config file")
        private String configPath;
    }

    public static void main(String[] args) {
        ServerCommand command = new ServerCommand();
        new CommandLine(command).parseArgs(args); // populate command object

        try {
            // Resolve config file path (user-supplied or auto-detected)
            String configPath = resolveConfigPath(command.configPath);

            // Load world configuration
            WorldConfig config = WorldConfig.loadFromFile(configPath);
            World gameWorld = new World(config);

            ExecutorService executor = Executors.newCachedThreadPool();
            try (ServerSocket serverSocket = new ServerSocket(command.port)) {
                System.out.println("Server started on port " + command.port);

                // Start console input thread
                Thread consoleThread = new Thread(() -> handleConsoleInput(gameWorld));
                consoleThread.setDaemon(true);
                consoleThread.start();

                // Accept client connections
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    executor.execute(new ClientHandler(clientSocket, gameWorld));
                }
            } finally {
                executor.shutdown();
            }
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tries to resolve the path to Config.json using:
     *  1. User-supplied argument (-c)
     *  2. Current working directory
     *  3. Known source folder path
     *  4. Classpath (if running from a packaged JAR)
     */
    private static String resolveConfigPath(String userSuppliedPath) throws IOException {
        if (userSuppliedPath != null && !userSuppliedPath.isBlank()) {
            File userFile = new File(userSuppliedPath);
            if (userFile.exists()) {
                System.out.println("Using user-specified config: " + userFile.getAbsolutePath());
                return userFile.getAbsolutePath();
            }
            throw new FileNotFoundException("Config file not found at provided path: " + userSuppliedPath);
        }

        // Look in working directory
        File cwd = new File(".");
        File[] matches = cwd.listFiles((dir, name) -> name.equalsIgnoreCase("Config.json"));
        if (matches != null && matches.length > 0) {
            System.out.println("Found Config.json in working directory.");
            return matches[0].getAbsolutePath();
        }

        // Look in source folder
        File sourceConfig = new File("src/main/java/za/co/wethinkcode/robots/server/world/Config.json");
        if (sourceConfig.exists()) {
            System.out.println("Found Config.json in source folder.");
            return sourceConfig.getAbsolutePath();
        }

        // Look on classpath (for packaged JAR)
        var resource = Server.class.getClassLoader()
                .getResource("za/co/wethinkcode/robots/server/world/Config.json");
        if (resource != null) {
            System.out.println("Found Config.json on classpath.");
            return resource.getPath();
        }

        throw new FileNotFoundException("Could not find Config.json in working directory, source folder, or classpath.");
    }

    private static void handleConsoleInput(World gameWorld) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter server commands (quit, robots, dump)");
        while (true) {
            try {
                System.out.print("Server> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                JsonNode request = createServerCommandRequest(input);
                Command command = ServerCommands.create(request, gameWorld);
                JsonNode response = command.execute();
                System.out.println(response.toPrettyString());

                if (input.equalsIgnoreCase("quit")) {
                    System.exit(0);
                    break;
                }
            } catch (Exception e) {
                System.err.println("Console error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static JsonNode createServerCommandRequest(String input) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode request = mapper.createObjectNode();
        request.put("command", input.toLowerCase());
        request.set("arguments", mapper.createArrayNode());
        return request;
    }
}
