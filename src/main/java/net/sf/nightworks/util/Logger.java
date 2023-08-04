package net.sf.nightworks.util;

public class Logger {
    public static void logError(@NotNull Throwable error) {
        //noinspection CallToPrintStackTrace
        error.printStackTrace();
    }

    public static void log(@NotNull CharSequence message) {
        System.out.println(message);
    }
}
