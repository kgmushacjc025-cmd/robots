package za.co.wethinkcode.robots.client.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorStateTest {
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void testErrorSimpleMessage() {
        ErrorState error = new ErrorState("Something went wrong");
        JsonNode result = error.toJson();

        ObjectNode expected = mapper.createObjectNode();
        expected.put("result", "ERROR");
        ObjectNode data = mapper.createObjectNode();
        data.put("message", "Something went wrong");
        expected.set("data", data);

        assertEquals(expected, result);
    }

    @Test
    void testErrorEmptyMessage() {
        ErrorState error = new ErrorState("");
        JsonNode result = error.toJson();

        ObjectNode expected = mapper.createObjectNode();
        expected.put("result", "ERROR");
        ObjectNode data = mapper.createObjectNode();
        data.put("message", "");
        expected.set("data", data);

        assertEquals(expected, result);
    }
}