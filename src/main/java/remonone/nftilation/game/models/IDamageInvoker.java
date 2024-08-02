package remonone.nftilation.game.models;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import remonone.nftilation.utils.PlayerUtils;

public interface IDamageInvoker extends Comparable<IDamageInvoker> {
    int getPriority();
    void OnEntityDamageDealing(EntityDamageByEntityEvent e, PlayerUtils.AttackerInfo info);
}
