package za.co.wethinkcode.robots.client.connection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public String sendCommand(String input) throws Exception {
        JsonNode request = commandBuilder.buildCommand(input);
        if (request == null) {
            return "Invalid command format";
        }
        if (request.has("result") && request.get("result").asText().equals("ERROR")) {
            return "Error: " + request.get("data").get("message").asText();
        }

        out.println(mapper.writeValueAsString(request));
        String response = in.readLine();
        if (response == null) {
            throw new Exception("Server disconnected");
        }
        return formatResponse(mapper.readTree(response));
    }

    private String formatResponse(JsonNode response) {
        StringBuilder sb = new StringBuilder();
        String result = response.has("result") ? response.get("result").asText() : "Unknown";
        sb.append("Result: ").append(result).append("\n");

        if (response.has("available_makes") && response.get("available_makes").isArray()) {
            if (response.has("message")) {
                sb.append(response.get("message").asText()).append("\n\n");
            }
            sb.append("Available Makes:\n");
            for (JsonNode make : response.get("available_makes")) {
                sb.append("  - ").append(make.get("make").asText())
                        .append(" | Shots: ").append(make.get("shots").asInt())
                        .append(" | Shields: ").append(make.get("shields").asInt())
                        .append(" | Max Shots per Fire: ").append(make.get("maxshot").asInt())
                        .append("\n");
            }
            return sb.toString();
        }

        if (response.has("data")) {
            JsonNode data = response.get("data");

            if (data.has("robots") && data.get("robots").isArray()) {
                sb.append("Robots:\n");
                for (JsonNode robot : data.get("robots")) {
                    sb.append("  - ").append(robot.has("name") ? robot.get("name").asText() : "Unknown").append("\n");
                    if (robot.has("position") && robot.get("position").isArray()) {
                        JsonNode pos = robot.get("position");
                        sb.append("      Position: [").append(pos.get(0).asInt()).append(", ").append(pos.get(1).asInt()).append("]\n");
                    }
                    sb.append("      Direction: ").append(robot.has("direction") ? robot.get("direction").asText() : "Unknown").append("\n");
                    sb.append("      Shields: ").append(robot.has("shields") ? robot.get("shields").asInt() : 0).append("\n");
                    sb.append("      Shots: ").append(robot.has("shots") ? robot.get("shots").asInt() : 0).append("\n");
                    sb.append("      Status: ").append(robot.has("status") ? robot.get("status").asText() : "Unknown").append("\n");
                }
                sb.append("\n");
            }

            if (data.has("obstacles") && data.get("obstacles").isArray()) {
                sb.append("Obstacles:\n");
                for (JsonNode obs : data.get("obstacles")) {
                    sb.append("  - Type: ").append(obs.has("obstacleType") ? obs.get("obstacleType").asText() : "Unknown").append("\n");

                    if (obs.has("corners")) {
                        JsonNode corners = obs.get("corners");
                        if (corners.has("topLeft")) {
                            JsonNode topLeft = corners.get("topLeft");
                            sb.append("    topLeftCorner(").append(topLeft.get("x").asInt()).append(", ").append(topLeft.get("y").asInt()).append(")\n");
                        }
                        if (corners.has("topRight")) {
                            JsonNode topRight = corners.get("topRight");
                            sb.append("    topRightCorner(").append(topRight.get("x").asInt()).append(", ").append(topRight.get("y").asInt()).append(")\n");
                        }
                        if (corners.has("bottomLeft")) {
                            JsonNode bottomLeft = corners.get("bottomLeft");
                            sb.append("    bottomLeftCorner(").append(bottomLeft.get("x").asInt()).append(", ").append(bottomLeft.get("y").asInt()).append(")\n");
                        }
                        if (corners.has("bottomRight")) {
                            JsonNode bottomRight = corners.get("bottomRight");
                            sb.append("    bottomRightCorner(").append(bottomRight.get("x").asInt()).append(", ").append(bottomRight.get("y").asInt()).append(")\n");
                        }
                    }

                    sb.append("    CanKillYou: ").append(obs.get("canKillYou").asBoolean()).append("\n");
                    sb.append("    CanWalkThrough: ").append(obs.get("canWalkThrough").asBoolean()).append("\n");
                    sb.append("    CanSeePast: ").append(obs.get("canSeePast").asBoolean()).append("\n\n");
                }
            }

            if (data.has("maxDistance") || data.has("startShots")) {
                if (data.has("maxDistance")) sb.append("Max Distance Allowed: ").append(data.get("maxDistance").asInt()).append("\n");
                if (data.has("distanceTraveled")) sb.append("Distance Traveled: ").append(data.get("distanceTraveled").asInt()).append("\n");
                if (data.has("startShots")) sb.append("Shots at Start: ").append(data.get("startShots").asInt()).append("\n");
                if (data.has("shotsUsed")) sb.append("Shots Used: ").append(data.get("shotsUsed").asInt()).append("\n");
                if (data.has("remainingShots")) sb.append("Shots Left: ").append(data.get("remainingShots").asInt()).append("\n");
                if (data.has("hitX") && data.has("hitY")) {
                    sb.append("Impact Coordinates: [").append(data.get("hitX").asInt()).append(", ").append(data.get("hitY").asInt()).append("]\n");
                }
                if (data.has("target")) {
                    String target = data.get("target").asText();
                    sb.append(target.equalsIgnoreCase("none") ? "No Target Hit\n" : "Target: " + target + "\n");
                }
                if (data.has("outcome")) sb.append("Outcome: ").append(data.get("outcome").asText()).append("\n");
            }

            if (data.has("steps")) sb.append("Steps taken: ").append(data.get("steps").asInt()).append("\n");

            if (data.has("start") && data.get("start").isObject()) {
                JsonNode start = data.get("start");
                sb.append("Start Position: [")
                        .append(start.has("x") ? start.get("x").asInt() : "Unknown")
                        .append(", ")
                        .append(start.has("y") ? start.get("y").asInt() : "Unknown")
                        .append("]\n");
            }
            if (data.has("end") && data.get("end").isObject()) {
                JsonNode end = data.get("end");
                sb.append("End Position: [")
                        .append(end.has("x") ? end.get("x").asInt() : "Unknown")
                        .append(", ")
                        .append(end.has("y") ? end.get("y").asInt() : "Unknown")
                        .append("]\n");
            }
            if (data.has("message")) {
                sb.append("Message: ").append(data.get("message").asText()).append("\n");
                if (data.get("message").asText().toLowerCase().contains("reload")) {
                    sb.append("Action Required: Please reload before firing again.\n");
                }
            }
            if (data.has("commands")) {
                sb.append("Available Commands:\n");
                for (JsonNode cmd : data.get("commands")) {
                    sb.append("  ").append(cmd.get("command").asText())
                            .append(" - ").append(cmd.get("description").asText())
                            .append("\n");
                }
            }
            if (data.has("position") && data.get("position").isArray() && data.get("position").size() >= 2) {
                JsonNode position = data.get("position");
                sb.append("Position: [")
                        .append(position.get(0).asInt())
                        .append(", ")
                        .append(position.get(1).asInt())
                        .append("]\n");
            }
            if (data.has("objects") && data.get("objects").isArray()) {
                sb.append("Objects:\n");
                for (JsonNode obj : data.get("objects")) {
                    sb.append("  - ").append(obj.has("type") ? obj.get("type").asText() : "Unknown")
                            .append(" at ").append(obj.has("direction") ? obj.get("direction").asText() : "Unknown")
                            .append(", distance ").append(obj.has("distance") ? obj.get("distance").asInt() : 0)
                            .append("\n");
                }
            }
            if (data.has("visibility")) sb.append("Visibility: ").append(data.get("visibility").asInt()).append("\n");
            if (data.has("reload")) sb.append("Reload Time: ").append(data.get("reload").asInt()).append("s\n");
            if (data.has("repair")) sb.append("Repair Time: ").append(data.get("repair").asInt()).append("s\n");
            if (data.has("shields")) sb.append("Shields: ").append(data.get("shields").asInt()).append("\n");
            if (data.has("world")) sb.append("World:\n").append(data.get("world").asText()).append("\n");
        }

        if (response.has("state")) {
            JsonNode state = response.get("state");
            sb.append("State:\n");
            if (state.has("position") && state.get("position").isArray() && state.get("position").size() >= 2) {
                JsonNode position = state.get("position");
                sb.append("  Position: [").append(position.get(0).asInt()).append(", ").append(position.get(1).asInt()).append("]\n");
            } else {
                sb.append("  Position: [Unknown]\n");
            }
            sb.append("  Direction: ").append(state.has("direction") ? state.get("direction").asText() : "Unknown").append("\n");
            sb.append("  Shields: ").append(state.has("shields") ? state.get("shields").asInt() : 0).append("\n");
            sb.append("  Shots: ").append(state.has("shots") ? state.get("shots").asInt() : 0).append("\n");
            sb.append("  MaxShots: ").append(state.has("maxShots") ? state.get("maxShots").asInt() : 0).append("\n");
            sb.append("  Status: ").append(state.has("status") ? state.get("status").asText() : "Unknown").append("\n");

            if (state.has("dead") && state.get("dead").asBoolean()) {
                sb.append("\nYOU DIED!\n");
                sb.append("Game Over. Disconnecting...\n");
            }
        }

        return sb.toString();
    }

    @Override
    public void close() {
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }
}
