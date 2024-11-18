package remonone.nftilation.game.ingame.actions.world;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Map;

public class ColdWinter implements IAction {
    @Override
    public void Init(Map<String, Object> params) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if(PlayerUtils.getModelFromPlayer(player) != null) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 2 * DataConstants.TICKS_IN_MINUTE, 1, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2 * DataConstants.TICKS_IN_MINUTE, 1, false, false));
            }
        });
    }

    @Override
    public String getTitle() {
        return "Морозная зима";
    }

    @Override
    public String getDescription() {
        return "Может кто-нибудь включить обогреватель?";
    }

    @Override
    public Sound getSound() {
        return Sound.ENTITY_CAT_PURREOW;
    }
}
