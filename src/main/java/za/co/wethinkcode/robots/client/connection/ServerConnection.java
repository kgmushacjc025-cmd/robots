package za.co.wethinkcode.robots.client.connection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import za.co.wethinkcode.robots.client.command.CommandBuilder;

/**
 * this class handles the connection between client and server
 * - opens a socket
 * - sends commands
 * - gets responses
 * - formats responses to be easier to read
 */
public class ServerConnection implements AutoCloseable {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final ObjectMapper mapper;
    private final CommandBuilder commandBuilder;

    /**
     * connect to the server using host + port
     */
    public ServerConnection(String host, int port) throws Exception {
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.mapper = new ObjectMapper();
        this.commandBuilder = new CommandBuilder();
    }

    /**
     * take user input -> build command -> send to server -> return formatted response
     */
    public String sendCommand(String input) throws Exception {
        JsonNode request = commandBuilder.buildCommand(input);
        if (request == null) {
            return "Invalid command format";
        }

        // send to server
        out.println(mapper.writeValueAsString(request));

        // wait for server reply
        String response = in.readLine();
        if (response == null) {
            throw new Exception("Server disconnected");
        }

        // turn raw json into pretty string
        return formatResponse(mapper.readTree(response));
    }

    /**
     * format the response from server
     * splits into smaller helpers so its not one long mess
     */
    private String formatResponse(JsonNode response) {
        ObjectNode resultNode = mapper.createObjectNode();

        addResultInfo(response, resultNode);
        addAvailableMakes(response, resultNode);
        addDataSection(response, resultNode);
        addStateSection(response, resultNode);

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultNode);
        } catch (Exception e) {
            return response.toString();
        }
    }

    // --- helper methods ---

    /**
     * adds the main "Result" info from server response
     * also checks for special hints like "reload" messages
     */
    private void addResultInfo(JsonNode response, ObjectNode resultNode) {
        String result = response.has("result") ? response.get("result").asText() : "Unknown";
        resultNode.put("Result", result);

        // if there's a message and it says reload, add extra note
        if (response.has("data") && response.get("data").has("message")) {
            String msg = response.get("data").get("message").asText().toLowerCase();
            if (msg.contains("reload")) {
                resultNode.put("ActionRequired", "Please reload before firing again");
            }
        }
    }

    /**
     * adds the "available_makes" list if server sent it
     * includes shots, shields, maxShots info
     */
    private void addAvailableMakes(JsonNode response, ObjectNode resultNode) {
        if (response.has("available_makes") && response.get("available_makes").isArray()) {
            ArrayNode makesArray = mapper.createArrayNode();
            for (JsonNode make : response.get("available_makes")) {
                ObjectNode makeNode = mapper.createObjectNode();
                makeNode.put("make", make.get("make").asText());
                makeNode.put("shots", make.get("shots").asInt());
                makeNode.put("shields", make.get("shields").asInt());
                makeNode.put("maxShots", make.get("maxshot").asInt());
                makesArray.add(makeNode);
            }
            resultNode.set("AvailableMakes", makesArray);
        }
    }

    /**
     * formats the "data" section from server into readable nodes
     */
    private void addDataSection(JsonNode response, ObjectNode resultNode) {
        if (!response.has("data")) return;

        ObjectNode dataNode = mapper.createObjectNode();
        JsonNode data = response.get("data");

        // break data down into smaller pieces
        addRobots(data, dataNode);
        addObstacles(data, dataNode);
        addNumbers(data, dataNode);
        addTexts(data, dataNode);
        addBooleans(data, dataNode);
        addStartEnd(data, dataNode);
        addCommands(data, dataNode);
        addPosition(data, dataNode);
        addObjects(data, dataNode);

        resultNode.set("Data", dataNode);
    }

    /**
     * formats the "state" section from server (your robot's info)
     * includes position, direction, shields, shots, dead status
     */
    private void addStateSection(JsonNode response, ObjectNode resultNode) {
        if (!response.has("state")) return;

        ObjectNode stateNode = mapper.createObjectNode();
        JsonNode state = response.get("state");

        // position
        if (state.has("position") && state.get("position").isArray()) {
            ArrayNode pos = mapper.createArrayNode();
            pos.add(state.get("position").get(0).asInt());
            pos.add(state.get("position").get(1).asInt());
            stateNode.set("Position", pos);
        }

        stateNode.put("Direction", state.has("direction") ? state.get("direction").asText() : "Unknown");
        stateNode.put("Shields", state.has("shields") ? state.get("shields").asInt() : 0);
        stateNode.put("Shots", state.has("shots") ? state.get("shots").asInt() : 0);
        stateNode.put("MaxShots", state.has("maxShots") ? state.get("maxShots").asInt() : 0);
        stateNode.put("Status", state.has("status") ? state.get("status").asText() : "Unknown");

        // if robot is dead
        if (state.has("dead") && state.get("dead").asBoolean()) {
            stateNode.put("Dead", true);
            stateNode.put("GameOverMessage", "YOU DIED! Game Over. Disconnecting...");
        } else {
            stateNode.put("Dead", false);
        }

        resultNode.set("State", stateNode);
    }

    // --- smaller helpers for Data section ---

    /** adds each robot in "robots" array from server */
    private void addRobots(JsonNode data, ObjectNode dataNode) {
        if (!data.has("robots") || !data.get("robots").isArray()) return;
        ArrayNode robotsArray = mapper.createArrayNode();

        for (JsonNode robot : data.get("robots")) {
            ObjectNode robotNode = mapper.createObjectNode();
            robotNode.put("name", robot.has("name") ? robot.get("name").asText() : "Unknown");

            if (robot.has("position") && robot.get("position").isArray()) {
                ArrayNode pos = mapper.createArrayNode();
                pos.add(robot.get("position").get(0).asInt());
                pos.add(robot.get("position").get(1).asInt());
                robotNode.set("position", pos);
            }

            robotNode.put("direction", robot.has("direction") ? robot.get("direction").asText() : "Unknown");
            robotNode.put("shields", robot.has("shields") ? robot.get("shields").asInt() : 0);
            robotNode.put("shots", robot.has("shots") ? robot.get("shots").asInt() : 0);
            robotNode.put("status", robot.has("status") ? robot.get("status").asText() : "Unknown");
            robotsArray.add(robotNode);
        }
        dataNode.set("Robots", robotsArray);
    }

    /** adds obstacles info to data section */
    private void addObstacles(JsonNode data, ObjectNode dataNode) {
        if (!data.has("obstacles") || !data.get("obstacles").isArray()) return;
        ArrayNode obsArray = mapper.createArrayNode();

        for (JsonNode obs : data.get("obstacles")) {
            ObjectNode obsNode = mapper.createObjectNode();
            obsNode.put("type", obs.has("obstacleType") ? obs.get("obstacleType").asText() : "Unknown");

            if (obs.has("corners")) {
                ObjectNode cornersNode = mapper.createObjectNode();
                JsonNode corners = obs.get("corners");
                addCorner(corners, "topLeft", cornersNode);
                addCorner(corners, "topRight", cornersNode);
                addCorner(corners, "bottomLeft", cornersNode);
                addCorner(corners, "bottomRight", cornersNode);
                obsNode.set("corners", cornersNode);
            }

            obsNode.put("canKillYou", obs.get("canKillYou").asBoolean());
            obsNode.put("canWalkThrough", obs.get("canWalkThrough").asBoolean());
            obsNode.put("canSeePast", obs.get("canSeePast").asBoolean());
            obsArray.add(obsNode);
        }
        dataNode.set("Obstacles", obsArray);
    }

    /** helper to add one corner of obstacle */
    private void addCorner(JsonNode corners, String name, ObjectNode cornersNode) {
        if (corners.has(name)) {
            ObjectNode corner = mapper.createObjectNode();
            corner.put("x", corners.get(name).get("x").asInt());
            corner.put("y", corners.get(name).get("y").asInt());
            cornersNode.set(name, corner);
        }
    }

    /** adds numeric fields like shots, steps, shields */
    private void addNumbers(JsonNode data, ObjectNode dataNode) {
        String[] intFields = {
                "maxDistance", "distanceTraveled", "startShots", "shotsUsed",
                "remainingShots", "steps", "visibility", "reload", "repair", "shields"
        };
        for (String field : intFields) {
            if (data.has(field)) dataNode.put(field, data.get(field).asInt());
        }
    }

    /** adds text fields like outcome, message, robotName */
    private void addTexts(JsonNode data, ObjectNode dataNode) {
        String[] textFields = {
                "target", "outcome", "world", "message", "robotName",
                "oldDirection", "newDirection", "turn"
        };
        for (String field : textFields) {
            if (data.has(field)) dataNode.put(field, data.get(field).asText());
        }
    }

    /** adds booleans like "fired" */
    private void addBooleans(JsonNode data, ObjectNode dataNode) {
        if (data.has("fired")) {
            dataNode.put("fired", data.get("fired").asBoolean());
        }
    }

    /** adds start and end positions */
    private void addStartEnd(JsonNode data, ObjectNode dataNode) {
        if (data.has("start") && data.get("start").isObject()) {
            ObjectNode startNode = mapper.createObjectNode();
            startNode.put("x", data.get("start").has("x") ? data.get("start").get("x").asInt() : -1);
            startNode.put("y", data.get("start").has("y") ? data.get("start").get("y").asInt() : -1);
            dataNode.set("Start", startNode);
        }
        if (data.has("end") && data.get("end").isObject()) {
            ObjectNode endNode = mapper.createObjectNode();
            endNode.put("x", data.get("end").has("x") ? data.get("end").get("x").asInt() : -1);
            endNode.put("y", data.get("end").has("y") ? data.get("end").get("y").asInt() : -1);
            dataNode.set("End", endNode);
        }
    }

    /** adds commands list */
    private void addCommands(JsonNode data, ObjectNode dataNode) {
        if (!data.has("commands") || !data.get("commands").isArray()) return;
        ArrayNode commandsArray = mapper.createArrayNode();

        for (JsonNode cmd : data.get("commands")) {
            ObjectNode cmdNode = mapper.createObjectNode();
            cmdNode.put("command", cmd.get("command").asText());
            cmdNode.put("description", cmd.get("description").asText());
            commandsArray.add(cmdNode);
        }
        dataNode.set("Commands", commandsArray);
    }

    /** adds robot position */
    private void addPosition(JsonNode data, ObjectNode dataNode) {
        if (data.has("position") && data.get("position").isArray()) {
            ArrayNode pos = mapper.createArrayNode();
            pos.add(data.get("position").get(0).asInt());
            pos.add(data.get("position").get(1).asInt());
            dataNode.set("Position", pos);
        }
    }

    /** adds objects info in view */
    private void addObjects(JsonNode data, ObjectNode dataNode) {
        if (!data.has("objects") || !data.get("objects").isArray()) return;
        ArrayNode objsArray = mapper.createArrayNode();

        for (JsonNode obj : data.get("objects")) {
            ObjectNode objNode = mapper.createObjectNode();
            objNode.put("type", obj.has("type") ? obj.get("type").asText() : "Unknown");
            objNode.put("direction", obj.has("direction") ? obj.get("direction").asText() : "Unknown");
            objNode.put("distance", obj.has("distance") ? obj.get("distance").asInt() : 0);
            objsArray.add(objNode);
        }
        dataNode.set("Objects", objsArray);
    }

    /** close everything nice */
    @Override
    public void close() {
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }
}
