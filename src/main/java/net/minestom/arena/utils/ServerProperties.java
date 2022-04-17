package net.minestom.arena.utils;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ServerProperties {
    private static final Logger LOG = LoggerFactory.getLogger(ServerProperties.class);
    private static final Map<String, String> properties = new HashMap<>();

    static {
        final File file = new File("server.properties");

        if (file.exists()) {
            LOG.info("Found server.properties file, loading values...");
            try {
                new BufferedReader(new FileReader(file)).lines().forEach(line -> {
                    final String[] split = line.split("=", 2);
                    if (split.length < 2) return;
                    properties.put(split[0], split[1]);
                });
                LOG.info("Loaded {} properties", properties.size());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static int getServerPort(int fallback) {
        final String port = properties.get("server-port");
        return port == null ? fallback : Integer.parseInt(port);
    }

    public static String getServerAddress(String fallback) {
        return properties.getOrDefault("server-address", fallback);
    }

    public static @Nullable String getForwardingSecret() {
        return properties.get("forwarding-secret");
    }
}
