package remonone.nftilation.game.models;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public interface IDamageHandler extends Comparable<IDamageHandler> {
    int getPriority();
    void OnEntityDamageHandle(EntityDamageByEntityEvent e);
    void OnDamageHandle(EntityDamageEvent e);
}
