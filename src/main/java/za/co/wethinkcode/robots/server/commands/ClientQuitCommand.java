package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.World;

public class ClientQuitCommand extends ClientCommands{
    private final  World gameWorld;
    private final String robotName;
    private final ObjectMapper mapper;

    public ClientQuitCommand (String robotName, World gameWorld){
        super(robotName, gameWorld);
        this.gameWorld = gameWorld;
        this.robotName = robotName;
        this.mapper = new ObjectMapper();
    }

    @Override
    public JsonNode execute(){
        gameWorld.removeOneRobot(robotName);
        ObjectNode response = mapper.createObjectNode();
        response.put("result", "OK");
        response.putObject("data").put("message", "Robot shutting down and exiting the world.");
        response.put("exitWorld", "TRUE");
        return response;
    }
}
