package remonone.nftilation.utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;


public class ColorUtils {
    
    public static Color TranslateToColor(ChatColor color) {
        return Colors.getColorByChatColor(color);
    }
    
    
    enum Colors {
        BLACK(Color.fromRGB(0,0,0), ChatColor.BLACK),
        DARK_BLUE(Color.fromRGB(0,0, 163), ChatColor.DARK_BLUE),
        DARK_GREEN(Color.fromRGB(0, 163,0), ChatColor.DARK_GREEN),
        DARK_AQUA(Color.fromRGB(0, 166, 166), ChatColor.DARK_AQUA),
        DARK_RED(Color.fromRGB(162, 0, 0), ChatColor.DARK_RED),
        DARK_PURPLE(Color.fromRGB(169, 0, 169), ChatColor.DARK_PURPLE),
        GOLD(Color.fromRGB(253, 169, 0), ChatColor.GOLD),
        GRAY(Color.fromRGB(169, 169, 169), ChatColor.GRAY),
        DARK_GRAY(Color.fromRGB(84, 84, 84), ChatColor.DARK_GRAY),
        BLUE(Color.fromRGB(83, 83, 251), ChatColor.BLUE),
        GREEN(Color.fromRGB(84, 253, 84), ChatColor.GREEN),
        AQUA(Color.fromRGB(84, 253, 253), ChatColor.AQUA),
        RED(Color.fromRGB(253, 84, 84), ChatColor.RED),
        LIGHT_PURPLE(Color.fromRGB(253, 84, 253), ChatColor.LIGHT_PURPLE),
        YELLOW(Color.fromRGB(253, 253, 84), ChatColor.YELLOW),
        WHITE(Color.fromRGB(229, 229, 229), ChatColor.WHITE);
        
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
