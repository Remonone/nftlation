package remonone.nftilation.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.enums.Stage;

public class ConfigUtils {
    
    public static boolean trySendMessageOnProhibited(Player player, PlayerData data) {
        if(data == null || data.getRole() == PlayerRole.PLAYER) {
            player.sendMessage(ChatColor.RED + MessageConstant.PERMISSION_LOCKED);
            return true;
        }
        if(Store.getInstance().getGameStage().getStage() != Stage.IDLE) {
            player.sendMessage(ChatColor.RED + MessageConstant.STATE_NOT_IDLE);
            return true;
        }
        return false;
    }
}
