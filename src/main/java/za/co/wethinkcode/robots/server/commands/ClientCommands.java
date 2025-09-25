package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import za.co.wethinkcode.robots.server.world.World;

/**
 * Base class for all client commands.
 * Handles common fields like robot name, arguments, and world reference.
 * Also contains a factory method to create commands from JSON requests.
 */
public abstract class ClientCommands implements Command {
    private String argument;
    private final ObjectMapper mapper;
    private final World gameWorld;
    private final String robotName;

    /** Execute the command and return a JSON response */
    public abstract JsonNode execute();

    /** Constructor for commands with no arguments */
    public ClientCommands(String robotName, World gameWorld) {
        this.argument = "";
        this.mapper = new ObjectMapper();
        this.gameWorld = gameWorld;
        this.robotName = robotName;
    }

    /** Constructor for commands with arguments */
    public ClientCommands(String robotName, String argument, World gameWorld) {
        this(robotName, gameWorld);
        // safely handle null arguments
        this.argument = (argument == null ? "" : argument.trim());
    }

    /** Get the argument string */
    public String getArgument() {
        return this.argument;
    }

    public String getRobotName() {
        return this.robotName;
    }

    /** Get the ObjectMapper for building JSON responses */
    protected ObjectMapper getMapper() {
        return this.mapper;
    }

    /** Get reference to the game world */
    protected World getWorld() {
        return this.gameWorld;
    }

    /**
     * Factory method to create a concrete command object from a JSON request.
     *
     * @param request   JSON node containing "command" and optional "arguments"
     * @param gameWorld reference to the world object
     * @param robotName the name of the robot, null if not yet launched
     * @return a concrete Command object or ErrorResponse if invalid
     */
    public static Command create(JsonNode request, World gameWorld, String robotName) {

        // Validate request and "command" field
        if (request == null || !request.has("command") || request.get("command").isNull()) {
            return new ErrorResponse(
                    "Missing or invalid 'command' field. Type 'help' to see available commands.",
                    robotName,
                    gameWorld
            );
        }

        String command = request.get("command").asText().toLowerCase();
        JsonNode arguments = request.has("arguments") ? request.get("arguments") : null;

        // Check robot launch state
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
                    "You already have a robot running. Use other commands instead!",
                    robotName,
                    gameWorld
            );
        }

        // Map commands to classes
        return switch (command) {
            case "quit" -> new ClientQuitCommand(robotName, gameWorld);
            case "state" -> new StateCommand(robotName, gameWorld);
            case "look" -> new LookCommand(robotName, gameWorld);
            case "launch" -> new LaunchCommand(robotName, arguments, gameWorld);
            case "forward" -> new ForwardCommand(robotName, arguments, gameWorld);
            case "back" -> new BackCommand(robotName, arguments, gameWorld);
            case "turn" -> {
                if (arguments == null || !arguments.isArray() || arguments.size() < 1 || !arguments.get(0).isTextual()) {
                    yield new ErrorResponse("Usage: turn <left|right>", robotName, gameWorld);
                }
                String direction = arguments.get(0).asText().toLowerCase();
                if (direction.equals("left")) {
                    yield new LeftCommand(robotName, null, gameWorld);
                } else if (direction.equals("right")) {
                    yield new RightCommand(robotName, null, gameWorld);
                } else {
                    yield new ErrorResponse("Invalid direction: '" + direction + "'", robotName, gameWorld);
                }
            }
            case "orientation" -> new OrientationCommand(robotName, gameWorld);
            case "help" -> new HelpCommand(robotName, gameWorld);
            case "fire" -> new FireCommand(robotName, arguments, gameWorld);
            case "repair" -> new RepairCommand(robotName, gameWorld);
            case "reload" -> new ReloadCommand(robotName, gameWorld);
            default -> new ErrorResponse(
                    "Unsupported robot command: '" + command + "'. Type 'help' to see available commands.",
                    robotName,
                    gameWorld
            );
        };
    }
}