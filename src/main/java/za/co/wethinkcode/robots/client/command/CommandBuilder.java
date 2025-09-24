package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This is the main CommandBuilder
 */
public class CommandBuilder {
    private final ObjectMapper mapper;

    public CommandBuilder() {
        this.mapper = new ObjectMapper();
    }

    /**
     * build a JSON command from user input
     * @param input user typed string
     */
    public JsonNode buildCommand(String input) {
        String[] parts = input.trim().split("\\s+");
        if (parts.length == 0) {
            return new ErrorState("Empty command").toJson();
        }

        String command = parts[0].toLowerCase();
        ObjectNode request = mapper.createObjectNode();
        ArrayNode arguments = mapper.createArrayNode();

        try {
            switch (command) {
                case "launch":
                    if (parts.length != 3) {
                        return new ErrorState("Usage: launch <make> <name>").toJson();
                    }
                    request.put("robot", parts[2]);
                    request.put("command", "launch");
                    arguments.add(parts[1]);
                    arguments.add(parts[2]);
                    break;

                case "forward":
                    ForwardCommandBuilder forwardBuilder = new ForwardCommandBuilder();
                    return forwardBuilder.build(parts);

                case "back":
                    BackCommandBuilder backBuilder = new BackCommandBuilder();
                    return backBuilder.build(parts);

                case "left":
                    LeftCommandBuilder leftBuilder = new LeftCommandBuilder();
                    return leftBuilder.build(parts);

                case "right":
                    RightCommandBuilder rightBuilder = new RightCommandBuilder();
                    return rightBuilder.build(parts);

                case "dump":
                case "look":
                case "state":
                case "quit":
                case "help":
                case "fire":
                    if (parts.length != 1) {
                        return new ErrorState("Usage: " + command).toJson();
                    }
                    request.put("command", command);
                    break;

                default:
                    return new ErrorState("Unknown command: '" + command + "'").toJson();
            }

            request.set("arguments", arguments);
            return request;
        } catch (NumberFormatException e) {
            return new ErrorState("Invalid number format").toJson();
        }
    }
}
