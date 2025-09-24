package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import za.co.wethinkcode.robots.server.world.World;

public abstract class ServerCommands implements Command {
    private String argument;
    private final ObjectMapper mapper;
    private final World gameWorld;

    public abstract JsonNode execute();

    public ServerCommands( World gameWorld) {
        this.argument = "";
        this.mapper = new ObjectMapper();
        this.gameWorld = gameWorld;
    }

    public ServerCommands(String name, String argument, World gameWorld) {
        this(gameWorld);
        this.argument = argument.trim();
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


    public static Command create(JsonNode request, World gameWorld) {
        try {
            String command = request.get("command").asText().toLowerCase();

            switch (command) {
                case "quit":
                    return new ServerQuitCommand(gameWorld);
                case "robots":
                    return new RobotsCommand(gameWorld);
                case "dump":
                    return new DumpCommand(gameWorld);
                default:
                    return new ErrorResponse("Unsupported server command: "+command + "'. " +
                            "Valid server commands are: robots, quit, dump.", gameWorld);
            }

        } catch (Exception e) {
            return new ErrorResponse("Could not parse arguments", gameWorld);
        }
    }


}
