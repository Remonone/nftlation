package remonone.nftilation.game.damage;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import remonone.nftilation.game.models.IDamageHandler;

public abstract class BaseDamageHandler implements IDamageHandler {
    @Override
    public int compareTo(IDamageHandler o) {
        return o.getPriority() - this.getPriority();
    }

    @Override
    public void OnEntityDamageHandle(EntityDamageByEntityEvent e) {}

    @Override
    public void OnDamageHandle(EntityDamageEvent e) {}
}
