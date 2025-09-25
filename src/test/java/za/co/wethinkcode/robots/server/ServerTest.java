package za.co.wethinkcode.robots.server;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.world.WorldConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    private File tempConfigFile;

    @BeforeEach
    void setup() throws IOException {
        // Create a temporary dummy config file for testing
        tempConfigFile = File.createTempFile("Config", ".json");
        tempConfigFile.deleteOnExit();
    }

    @Test
    void testCheckUserPathValidFile() throws Exception {
        String path = Server.checkUserPath(tempConfigFile.getAbsolutePath());
        assertEquals(tempConfigFile.getAbsolutePath(), path);
    }

    @Test
    void testCheckUserPathInvalidFile() {
        assertThrows(FileNotFoundException.class, () -> {
            Server.checkUserPath("nonexistent.json");
        });
    }

    @Test
    void testCheckUserPathNullOrBlank() throws Exception {
        assertNull(Server.checkUserPath(null));
        assertNull(Server.checkUserPath(""));
        assertNull(Server.checkUserPath("   "));
    }

    @Test
    void testCreateServerCommandRequest() {
        String rawInput = "robots";
        JsonNode request = Server.createServerCommandRequest(rawInput);

        assertEquals("robots", request.get("command").asText());
        assertTrue(request.get("arguments").isArray());
        assertEquals(0, request.get("arguments").size());
    }

    @Test
    void testResolveConfigPathUserSupplied() throws Exception {
        String resolved = Server.resolveConfigPath(tempConfigFile.getAbsolutePath());
        assertEquals(tempConfigFile.getAbsolutePath(), resolved);
    }

    @Test
    void testResolveConfigPathFileNotFoundThrows() {
        assertThrows(FileNotFoundException.class, () -> {
            Server.resolveConfigPath("nonexistent.json");
        });
    }

    @Test
    void testCheckWorkingDirectoryReturnsNullIfNoFile() {
        File cwd = new File(".");
        File[] files = cwd.listFiles((dir, name) -> name.equalsIgnoreCase("Config.json"));
        if (files != null && files.length > 0) {
            for (File f : files) f.renameTo(new File(f.getAbsolutePath() + ".bak"));
        }

        String result = Server.checkWorkingDirectory();
        assertNull(result);
    }

    @Test
    void testCheckClasspathReturnsNullIfNoResource() {
        String result = Server.checkClasspath();
        assertNull(result);
    }

    @Test
    void testCreateServerCommandRequestLowercasesCommand() {
        JsonNode req = Server.createServerCommandRequest("QUIT");
        assertEquals("quit", req.get("command").asText());
    }

    @Test
    void testStaticRecorderInitialization() {
        // Just ensure static block does not throw
        assertDoesNotThrow(() -> {
            new Server();
        });
    }
}
