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

    public ClientCommands(String robotName, World gameWorld) {
        this.argument = "";
        this.mapper = new ObjectMapper();
        this.gameWorld = gameWorld;
        this.robotName= robotName;
    }

    public ClientCommands(String robotName, String argument, World gameWorld) {
        this(robotName,gameWorld);
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



    public static Command create(JsonNode request, World gameWorld, String robotName) {

        try {
            String command = request.get("command").asText().toLowerCase();

            JsonNode arguments = request.get("arguments");

            if (robotName == null && !command.equalsIgnoreCase("launch") && !command.equalsIgnoreCase("quit") && !command.equalsIgnoreCase("help")){
                return  new ErrorResponse("Make sure to launch a robot before running other commands.", robotName, gameWorld);
            }
            if (robotName != null && command.equalsIgnoreCase("launch")){
                return  new ErrorResponse("You’ve already got a robot running. Go ahead and run other commands!", robotName, gameWorld);
            }

            switch (command) {
                case "quit":
                    return new ClientQuitCommand(robotName,gameWorld);
                case "state":
                    return new StateCommand(robotName, gameWorld);
                case "look":
                    return new LookCommand(robotName, gameWorld);
                case "launch":
                    return new LaunchCommand(robotName, arguments, gameWorld);
                case "forward":
                    return new ForwardCommand(robotName,arguments, gameWorld);
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
                default:
                    return new ErrorResponse("Unsupported robot command: "+command , robotName , gameWorld);
            }

        } catch (Exception e) {
            return new ErrorResponse("Could not parse arguments", robotName,gameWorld);
        }
    }


}
