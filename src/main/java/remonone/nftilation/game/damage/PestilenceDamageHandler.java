package remonone.nftilation.game.damage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.utils.NestedObjectFetcher;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Optional;

public class PestilenceDamageHandler extends BaseDamageHandler {

    private final Player infector;
    private final double infectionRange;

    @Override
    public int getPriority() {
        return 3;
    }

    public PestilenceDamageHandler(Player infector) {
        this.infector = infector;
        Object pestilenceDuration = NestedObjectFetcher.getNestedObject(MetaConstants.META_EVENTS_PESTILENCE, MetaConfig.getInstance().getEvents(), 1);
        this.infectionRange = (Double) Optional.ofNullable(pestilenceDuration).orElse(5D);
    }

    @Override
    public void OnDamageHandle(EntityDamageEvent e) {
        if(!(e.getEntity() instanceof Player)) { return; }
        Player p = (Player) e.getEntity();
        PotionEffect effect = p.getPotionEffect(PotionEffectType.WITHER);
        if(effect == null) {
            PlayerUtils.getModelFromPlayer(p).getDamageHandlers().remove(this);
            return;
        }
        if(!e.getCause().equals(EntityDamageEvent.DamageCause.WITHER)) return;

        if(p.getHealth() - e.getFinalDamage() > 0) return;
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(!PlayerUtils.isActivePlayer(player)) continue;
            if(player.getLocation().distance(p.getLocation()) > this.infectionRange) continue;
            int duration = player.getPotionEffect(PotionEffectType.WITHER).getDuration();
            player.removePotionEffect(PotionEffectType.WITHER);
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration + DataConstants.TICKS_IN_MINUTE, 1, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5, 10, false, false));
        }
        infector.sendMessage(ChatColor.DARK_GREEN + MessageConstant.PESTILENCE_KILL);
    }
}
