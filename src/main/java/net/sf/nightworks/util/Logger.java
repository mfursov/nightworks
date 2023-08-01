package net.sf.nightworks.util;

public class Logger {
    public static void logError(@NotNull Throwable error) {
        //noinspection CallToPrintStackTrace
        error.printStackTrace();
    }
}
