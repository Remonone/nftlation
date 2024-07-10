package remonone.nftilation.utils;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import remonone.nftilation.constants.DataConstants;

public class ResetUtils {
    
    public static void globalResetPlayerStats(Player player) {
        player.setHealthScaled(false);
        player.setHealthScale(DataConstants.PLAYER_HEALTH);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setHealth(DataConstants.PLAYER_HEALTH);
        player.setExp(0);
        player.setLevel(0);
        player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0);
        player.setWalkSpeed(DataConstants.PLAYER_SPEED);
        for(PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setGameMode(GameMode.SURVIVAL);
    }
}
