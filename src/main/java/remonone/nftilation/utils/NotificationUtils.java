package remonone.nftilation.utils;

import javafx.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class NotificationUtils {
    
    private static final HashMap<NotificationType, Pair<ChatColor, Sound>> setup = new HashMap<NotificationType, Pair<ChatColor, Sound>>() {{
        put(NotificationType.SUCCESS, new Pair<>(ChatColor.GREEN, Sound.ENTITY_CAT_PURREOW));
        put(NotificationType.FAIL, new Pair<>(ChatColor.RED, Sound.ENTITY_CAT_HISS));
        put(NotificationType.WARNING, new Pair<>(ChatColor.RED, Sound.ENTITY_CAT_HURT));
    }};
    
    public static void sendNotification(Player receiver, String message, NotificationType type, boolean isSilent) {
        Pair<ChatColor, Sound> pair = setup.get(type);
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
