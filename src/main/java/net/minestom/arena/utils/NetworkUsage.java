package net.minestom.arena.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public final class NetworkUsage {

    public static final String UTIL_BYTES_IN = "./util/bytes-in";
    private static final String UTIL_BYTES_OUT = "./util/bytes-out";
    public static final String UTIL_BYTES_RESET = "./util/bytes-reset";

    public static long getBytesSent() {
        return Long.parseLong(execute(UTIL_BYTES_OUT));
    }

    public static long getBytesReceived() {
        return Long.parseLong(execute(UTIL_BYTES_IN));
    }

    public static void resetCounters() {
        execute(UTIL_BYTES_RESET);
    }

    public static boolean executablesPresent() {
        return new File(UTIL_BYTES_OUT).canExecute() && new File(UTIL_BYTES_IN).canExecute() && new File(UTIL_BYTES_RESET).canExecute();
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
