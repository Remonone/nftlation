package remonone.nftilation.game.models;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import remonone.nftilation.utils.PlayerUtils;

public interface IDamageHandler {
    void OnEntityDamageHandle(EntityDamageByEntityEvent e);
    void OnDamageHandle(EntityDamageEvent e);
}
