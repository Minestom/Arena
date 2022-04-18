package net.minestom.arena.utils;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class ServerProperties {
    private static final Path PROPERTIES_PATH = Path.of("server.properties");
    private static final Logger LOG = LoggerFactory.getLogger(ServerProperties.class);
    private static final Map<String, String> PROPERTIES = new HashMap<>();

    static {
        if (Files.exists(PROPERTIES_PATH)) {
            LOG.info("Found server.properties file, loading values...");
            try (Stream<String> lines = Files.lines(PROPERTIES_PATH)) {
                lines.forEach(line -> {
                    final String[] split = line.split("=", 2);
                    if (split.length < 2) return;
                    PROPERTIES.put(split[0], split[1]);
                });
                LOG.info("Loaded {} properties", PROPERTIES.size());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static int getServerPort(int fallback) {
        return getInt("server-port", fallback);
    }

    public static String getServerAddress(String fallback) {
        return getString("server-address", fallback);
    }

    public static @Nullable String getForwardingSecret() {
        return getString("forwarding-secret", null);
    }

    private static int getInt(String key, int fallback) {
        final String value = PROPERTIES.get(key);
        return value == null ? fallback : Integer.parseInt(value);
    }

    private static String getString(String key, String fallback) {
        final String value = PROPERTIES.get(key);
        return value == null ? fallback : value;
    }
}
