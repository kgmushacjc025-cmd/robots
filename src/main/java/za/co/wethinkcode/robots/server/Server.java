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

import za.co.wethinkcode.flow.Recorder;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.commands.ServerCommands;
import za.co.wethinkcode.robots.server.networking.ClientHandler;
import za.co.wethinkcode.robots.server.world.World;
import za.co.wethinkcode.robots.server.world.WorldConfig;

/**
 * Robot World Server.
 * Responsibilities:
 * 1. Parse command-line arguments for port and config path.
 * 2. Load world configuration.
 * 3. Accept client connections and delegate to ClientHandler.
 * 4. Handle console input for server admin commands.
 */
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
        new CommandLine(command).parseArgs(args);

        try {
            String configPath = resolveConfigPath(command.configPath);
            WorldConfig config = WorldConfig.loadFromFile(configPath);
            World gameWorld = new World(config);

            startServer(command.port, gameWorld);
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Starts the server: listens for clients and handles console input.
     *
     * @param port      TCP port to listen on.
     * @param gameWorld The game world instance.
     * @throws IOException If ServerSocket fails.
     */
    private static void startServer(int port, World gameWorld) throws IOException {
        ExecutorService executor = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

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
    }

    /**
     * Resolves the path to Config.json using several strategies.
     *
     * @param userSuppliedPath Optional path supplied by user.
     * @return Absolute path to the config file.
     * @throws IOException If config file cannot be found.
     */
    static String resolveConfigPath(String userSuppliedPath) throws IOException {
        String path;

        path = checkUserPath(userSuppliedPath);
        if (path != null) return path;

        path = checkWorkingDirectory();
        if (path != null) return path;

        path = checkSourceFolder();
        if (path != null) return path;

        path = checkClasspath();
        if (path != null) return path;

        throw new FileNotFoundException("Could not find Config.json in working directory, source folder, or classpath.");
    }

    static String checkUserPath(String userPath) throws FileNotFoundException {
        if (userPath != null && !userPath.isBlank()) {
            File userFile = new File(userPath);
            if (userFile.exists()) {
                System.out.println("Using user-specified config: " + userFile.getAbsolutePath());
                return userFile.getAbsolutePath();
            }
            throw new FileNotFoundException("Config file not found at provided path: " + userPath);
        }
        return null;
    }

    static String checkWorkingDirectory() {
        File cwd = new File(".");
        File[] matches = cwd.listFiles((dir, name) -> name.equalsIgnoreCase("Config.json"));
        if (matches != null && matches.length > 0) {
            System.out.println("Found Config.json in working directory.");
            return matches[0].getAbsolutePath();
        }
        return null;
    }

    static String checkSourceFolder() {
        File sourceConfig = new File("src/main/java/za/co/wethinkcode/robots/server/world/Config.json");
        if (sourceConfig.exists()) {
            System.out.println("Found Config.json in source folder.");
            return sourceConfig.getAbsolutePath();
        }
        return null;
    }

    static String checkClasspath() {
        var resource = Server.class.getClassLoader()
                .getResource("za/co/wethinkcode/robots/server/world/Config.json");
        if (resource != null) {
            System.out.println("Found Config.json on classpath.");
            return resource.getPath();
        }
        return null;
    }

    /**
     * Handles console input for server admin commands.
     *
     * @param gameWorld The game world instance.
     */
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
                }
            } catch (Exception e) {
                System.err.println("Console error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Converts raw console input into a JSON request for ServerCommands.
     *
     * @param input Raw console input.
     * @return JSON request node.
     */
    static JsonNode createServerCommandRequest(String input) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode request = mapper.createObjectNode();
        request.put("command", input.toLowerCase());
        request.set("arguments", mapper.createArrayNode());
        return request;
    }

    static {
        new Recorder().logRun();
    }
}
