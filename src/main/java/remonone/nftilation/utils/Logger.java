package remonone.nftilation.utils;

import org.bukkit.Bukkit;

import static org.bukkit.Bukkit.getServer;

public class Logger {
    
    public static void log(String message) {
        StackTraceElement caller = getCallerElement();
        getServer().getLogger().info("[LOG] " + caller.getFileName() + "[" + caller.getLineNumber() + "]: " + message);
    }
    
    public static void debug(String message) {
        StackTraceElement caller = getCallerElement();
        getServer().getLogger().info("[DEBUG] " + caller.getFileName() + "[" + caller.getLineNumber() + "]: " + message);
    }
    
    public static void warn(String message) {
        StackTraceElement caller = getCallerElement();
        getServer().getLogger().info("[WARN] " + caller.getFileName() + "[" + caller.getLineNumber() + "]: " + message);
    }

    public static void error(String message) {
        StackTraceElement caller = getCallerElement();
        getServer().getLogger().info("[ERROR] " + caller.getFileName() + "[" + caller.getLineNumber() + "]: " + message);
    }
    
    public static void broadcast(String message) {
        Bukkit.broadcastMessage(message);
    }

    private static StackTraceElement getCallerElement() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return stackTrace[3];
    }
}
