package remonone.nftilation.utils;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class NotificationUtils {
    
    private static final HashMap<NotificationType, Map.Entry<ChatColor, Sound>> setup = new HashMap<NotificationType, Map.Entry<ChatColor, Sound>>() {{
        put(NotificationType.SUCCESS, new AbstractMap.SimpleEntry<>(ChatColor.GREEN, Sound.ENTITY_CAT_PURREOW));
        put(NotificationType.FAIL, new AbstractMap.SimpleEntry<>(ChatColor.RED, Sound.ENTITY_CAT_HISS));
        put(NotificationType.WARNING, new AbstractMap.SimpleEntry<>(ChatColor.RED, Sound.ENTITY_CAT_HURT));
    }};
    
    public static void sendNotification(Player receiver, String message, NotificationType type, boolean isSilent) {
        Map.Entry<ChatColor, Sound> pair = setup.get(type);
        receiver.sendMessage(pair.getKey() + "[FAIL] " +  message);
        if (!isSilent) {
            receiver.playSound(receiver.getLocation(), pair.getValue(), 1f, 1f);
        }
    }
    
    public enum NotificationType {
        SUCCESS,
        FAIL,
        WARNING
    }
}
