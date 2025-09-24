package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import za.co.wethinkcode.robots.server.world.World;

public abstract class ClientCommands implements Command {
    private String argument;
    private final ObjectMapper mapper;
    private final World gameWorld;
    private final String robotName;

    public abstract JsonNode execute();

    // Constructor for commands without arguments
    public ClientCommands(String robotName, World gameWorld) {
        this.argument = "";
        this.mapper = new ObjectMapper();
        this.gameWorld = gameWorld;
        this.robotName = robotName;
    }

    // Constructor for commands with arguments
    public ClientCommands(String robotName, String argument, World gameWorld) {
        this(robotName, gameWorld);
        // Safely handle null arguments
        this.argument = (argument == null ? "" : argument.trim());
    }

    public String getArgument() {
        return this.argument;
    }

    protected ObjectMapper getMapper() {
        return this.mapper;
    }

    protected World getWorld() {
        return this.gameWorld;
    }

    // Factory method to create command objects
    public static Command create(JsonNode request, World gameWorld, String robotName) {

        // Step 1: Validate request and command field
        if (request == null || !request.has("command") || request.get("command").isNull()) {
            return new ErrorResponse(
                    "Missing or invalid 'command' field. " +
                            "If you're unsure what to do, type 'help' to see available commands.",
                    robotName,
                    gameWorld
            );
        }

        String command = request.get("command").asText().toLowerCase();
        JsonNode arguments = request.has("arguments") ? request.get("arguments") : null;

        // Step 2: Validate robot launch state
        if (robotName == null &&
                !command.equals("launch") &&
                !command.equals("quit") &&
                !command.equals("help")) {
            return new ErrorResponse(
                    "Make sure to launch a robot before running other commands.",
                    robotName,
                    gameWorld
            );
        }

        if (robotName != null && command.equals("launch")) {
            return new ErrorResponse(
                    "You’ve already got a robot running. Go ahead and run other commands!",
                    robotName,
                    gameWorld
            );
        }

        // Step 3: Command switch
        switch (command) {
            case "quit":
                return new ClientQuitCommand(robotName, gameWorld);
            case "state":
                return new StateCommand(robotName, gameWorld);
            case "look":
                return new LookCommand(robotName, gameWorld);
            case "launch":
                return new LaunchCommand(robotName, arguments, gameWorld);
            case "forward":
                return new ForwardCommand(robotName, arguments, gameWorld);
            case "back":
                return new BackCommand(robotName, arguments, gameWorld);
            case "left":
                return new LeftCommand(robotName, arguments, gameWorld);
            case "right":
                return new RightCommand(robotName, arguments, gameWorld);
            case "help":
                return new HelpCommand(robotName, gameWorld);
            case "fire":
                return new FireCommand(robotName, arguments, gameWorld);

            // Step 4: Unknown command
            default:
                return new ErrorResponse(
                        "Unsupported robot command: '" + command + "'. " +
                                "If you're unsure what to do, type 'help' to see available commands.",
                        robotName,
                        gameWorld
                );
        }
    }
}
