package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.databind.JsonNode;

public interface Command {
    JsonNode execute();
}