package net.minestom.arena.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public final class NetworkUsage {
    private static final File baseDir = new File("./util/net");
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUsage.class);
    public static final String UTIL_BYTES_IN = new File(baseDir, "bytes-in").toString();
    private static final String UTIL_BYTES_OUT = new File(baseDir, "bytes-out").toString();
    public static final String UTIL_BYTES_RESET = new File(baseDir, "bytes-reset").toString();

    public static long getBytesSent() {
        return Long.parseLong(execute(UTIL_BYTES_OUT));
    }

    public static long getBytesReceived() {
        return Long.parseLong(execute(UTIL_BYTES_IN));
    }

    public static void resetCounters() {
        execute(UTIL_BYTES_RESET);
    }

    public static boolean checkEnabledOrExtract() {
        if (baseDir.isDirectory()) {
            if (new File(baseDir, "enabled").exists()) {
                return true;
            } else {
                LOGGER.warn("Network utils aren't enabled, metrics for network I/O will not be reported!");
                return false;
            }
        } else {
            LOGGER.warn("Extracting network utils, refer to the README.md in {} to enable network metrics.", baseDir);
            try {
                ResourceUtils.extractResource("util/net");
            } catch (URISyntaxException | IOException e) {
                LOGGER.error("Failed to extract utils", e);
            }
            return false;
        }
    }

    private static String execute(String command) {
        try {
            final String line = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream())).readLine();
            if (line.length() == 1) return "0"; else return line;
        } catch (IOException e) {
            return "0";
        }
    }
}
