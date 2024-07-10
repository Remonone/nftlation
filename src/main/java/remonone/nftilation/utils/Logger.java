package remonone.nftilation.utils;

import org.bukkit.Bukkit;

import static org.bukkit.Bukkit.getServer;

public class Logger {
    
    public static void log(String message) {
        getServer().getLogger().info("[LOG] " + message);
    }
    
    public static void debug(String message) {
        getServer().getLogger().info("[DEBUG] " + message);
    }
    
    public static void warn(String message) {
        getServer().getLogger().info("[WARN] " + message);
    }

    public static void error(String message) {
        getServer().getLogger().info("[ERROR] " + message);
    }
    
    public static void broadcast(String message) {
        Bukkit.broadcastMessage(message);
    }
}
