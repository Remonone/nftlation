package remonone.nftilation.game.models;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import remonone.nftilation.utils.PlayerUtils;

public interface IDamageHandler extends Comparable<IDamageHandler> {
    int getPriority();
    void OnEntityDamageHandle(EntityDamageByEntityEvent e);
    void OnDamageHandle(EntityDamageEvent e);
}
