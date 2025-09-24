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

public class ServerConnection implements AutoCloseable {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final ObjectMapper mapper;
    private final CommandBuilder commandBuilder;

    public ServerConnection(String host, int port) throws Exception {
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.mapper = new ObjectMapper();
        this.commandBuilder = new CommandBuilder();
    }

    /**
     * Send a command string to the server and return a formatted response.
     */
    public String sendCommand(String input) throws Exception {
        JsonNode request = commandBuilder.buildCommand(input);
        if (request == null) {
            return "Invalid command format";
        }

        out.println(mapper.writeValueAsString(request));
        String response = in.readLine();
        if (response == null) {
            throw new Exception("Server disconnected");
        }

        return formatResponse(mapper.readTree(response));
    }

    /**
     * Convert server JSON into a structured and readable format.
     */
    private String formatResponse(JsonNode response) {
        ObjectNode resultNode = mapper.createObjectNode();

        // Result
        String result = response.has("result") ? response.get("result").asText() : "Unknown";
        resultNode.put("Result", result);

        // Messages
        if (response.has("data") && response.get("data").has("message")) {

            // If fire requires reload, show extra hint
            if (response.get("data").get("message").asText().toLowerCase().contains("reload")) {
                resultNode.put("ActionRequired", "Please reload before firing again.");
            }
        }

        // Available Makes
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

        // --- Data Section ---
        if (response.has("data")) {
            ObjectNode dataNode = mapper.createObjectNode();
            JsonNode data = response.get("data");

            // Robots
            if (data.has("robots") && data.get("robots").isArray()) {
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

            // Obstacles
            if (data.has("obstacles") && data.get("obstacles").isArray()) {
                ArrayNode obsArray = mapper.createArrayNode();
                for (JsonNode obs : data.get("obstacles")) {
                    ObjectNode obsNode = mapper.createObjectNode();
                    obsNode.put("type", obs.has("obstacleType") ? obs.get("obstacleType").asText() : "Unknown");

                    if (obs.has("corners")) {
                        ObjectNode cornersNode = mapper.createObjectNode();
                        JsonNode corners = obs.get("corners");
                        if (corners.has("topLeft")) {
                            ObjectNode corner = mapper.createObjectNode();
                            corner.put("x", corners.get("topLeft").get("x").asInt());
                            corner.put("y", corners.get("topLeft").get("y").asInt());
                            cornersNode.set("topLeft", corner);
                        }
                        if (corners.has("topRight")) {
                            ObjectNode corner = mapper.createObjectNode();
                            corner.put("x", corners.get("topRight").get("x").asInt());
                            corner.put("y", corners.get("topRight").get("y").asInt());
                            cornersNode.set("topRight", corner);
                        }
                        if (corners.has("bottomLeft")) {
                            ObjectNode corner = mapper.createObjectNode();
                            corner.put("x", corners.get("bottomLeft").get("x").asInt());
                            corner.put("y", corners.get("bottomLeft").get("y").asInt());
                            cornersNode.set("bottomLeft", corner);
                        }
                        if (corners.has("bottomRight")) {
                            ObjectNode corner = mapper.createObjectNode();
                            corner.put("x", corners.get("bottomRight").get("x").asInt());
                            corner.put("y", corners.get("bottomRight").get("y").asInt());
                            cornersNode.set("bottomRight", corner);
                        }
                        obsNode.set("corners", cornersNode);
                    }

                    obsNode.put("canKillYou", obs.get("canKillYou").asBoolean());
                    obsNode.put("canWalkThrough", obs.get("canWalkThrough").asBoolean());
                    obsNode.put("canSeePast", obs.get("canSeePast").asBoolean());
                    obsArray.add(obsNode);
                }
                dataNode.set("Obstacles", obsArray);
            }

            // Numbers
            String[] intFields = {
                    "maxDistance", "distanceTraveled", "startShots", "shotsUsed",
                    "remainingShots", "steps", "visibility", "reload", "repair", "shields"
            };
            for (String field : intFields) {
                if (data.has(field)) dataNode.put(field, data.get(field).asInt());
            }

            // Texts
            String[] textFields = {
                    "target", "outcome", "world", "message", "robotName",
                    "oldDirection", "newDirection", "turn"
            };
            for (String field : textFields) {
                if (data.has(field)) dataNode.put(field, data.get(field).asText());
            }

            // Fired (boolean)
            if (data.has("fired")) {
                dataNode.put("fired", data.get("fired").asBoolean());
            }

            // Start / End
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

            // Commands
            if (data.has("commands") && data.get("commands").isArray()) {
                ArrayNode commandsArray = mapper.createArrayNode();
                for (JsonNode cmd : data.get("commands")) {
                    ObjectNode cmdNode = mapper.createObjectNode();
                    cmdNode.put("command", cmd.get("command").asText());
                    cmdNode.put("description", cmd.get("description").asText());
                    commandsArray.add(cmdNode);
                }
                dataNode.set("Commands", commandsArray);
            }

            // Position
            if (data.has("position") && data.get("position").isArray()) {
                ArrayNode pos = mapper.createArrayNode();
                pos.add(data.get("position").get(0).asInt());
                pos.add(data.get("position").get(1).asInt());
                dataNode.set("Position", pos);
            }

            // Objects
            if (data.has("objects") && data.get("objects").isArray()) {
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

            resultNode.set("Data", dataNode);
        }

        // State
        if (response.has("state")) {
            ObjectNode stateNode = mapper.createObjectNode();
            JsonNode state = response.get("state");
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

            if (state.has("dead") && state.get("dead").asBoolean()) {
                stateNode.put("Dead", true);
                stateNode.put("GameOverMessage", "YOU DIED! Game Over. Disconnecting...");
            } else {
                stateNode.put("Dead", false);
            }

            resultNode.set("State", stateNode);
        }

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultNode);
        } catch (Exception e) {
            return response.toString();
        }
    }

    @Override
    public void close() {
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }
}
