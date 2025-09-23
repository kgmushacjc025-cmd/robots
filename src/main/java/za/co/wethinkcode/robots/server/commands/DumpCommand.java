package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import za.co.wethinkcode.robots.server.world.World;

public class DumpCommand extends  ServerCommands{
    private final ObjectMapper mapper;
    private final  World gameWorld;

    public DumpCommand(World gameWorld){
        super(gameWorld);
        this.gameWorld = gameWorld;
        this.mapper = new ObjectMapper();

    }

    @Override
    public JsonNode execute(){
        ObjectNode response = mapper.createObjectNode();
        response.put("result", "OK");
        ObjectNode data = mapper.createObjectNode();
        data.set("worldState", gameWorld.getWorldState());
        response.set("data", data);
        return response;
    }
}
