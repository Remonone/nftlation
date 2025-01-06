package remonone.nftilation.utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;


public class ColorUtils {
    
    public static Color TranslateToColor(ChatColor color) {
        return Colors.getColorByChatColor(color);
    }
    
    
    enum Colors {
        BLACK(Color.fromRGB(1908001), ChatColor.BLACK),
        DARK_BLUE(Color.fromRGB(3847130), ChatColor.DARK_BLUE),
        DARK_GREEN(Color.fromRGB(8439583), ChatColor.DARK_GREEN), // NOT CORRECT
        DARK_AQUA(Color.fromRGB(1481884), ChatColor.DARK_AQUA),
        DARK_RED(Color.fromRGB(162, 0, 0), ChatColor.DARK_RED),
        DARK_PURPLE(Color.fromRGB(8991416), ChatColor.DARK_PURPLE),
        GOLD(Color.fromRGB(16351261), ChatColor.GOLD),
        GRAY(Color.fromRGB(10329495), ChatColor.GRAY),
        DARK_GRAY(Color.fromRGB(4673362), ChatColor.DARK_GRAY),
        BLUE(Color.fromRGB(3949738), ChatColor.BLUE),
        GREEN(Color.fromRGB(6192150), ChatColor.GREEN),
        AQUA(Color.fromRGB(65535), ChatColor.AQUA),
        RED(Color.fromRGB(11546150), ChatColor.RED),
        LIGHT_PURPLE(Color.fromRGB(15961002), ChatColor.LIGHT_PURPLE),
        YELLOW(Color.fromRGB(16701501), ChatColor.YELLOW),
        WHITE(Color.fromRGB(16383998), ChatColor.WHITE);
        
        private final Color color;
        private final ChatColor reference;
        
        public static Color getColorByChatColor(ChatColor color) {
            for(Colors c : Colors.values()) {
                if(c.reference == color) return c.color;
            }
            return null;
        }
        
        Colors(Color color, ChatColor ref) {
            this.color = color;
            this.reference = ref;
        }
    }
}
