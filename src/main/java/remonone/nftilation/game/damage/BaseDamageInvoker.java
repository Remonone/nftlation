package remonone.nftilation.game.damage;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import remonone.nftilation.game.models.IDamageInvoker;
import remonone.nftilation.utils.PlayerUtils;

public abstract class BaseDamageInvoker implements IDamageInvoker {

    @Override
    public void OnEntityDamageDealing(EntityDamageByEntityEvent e, PlayerUtils.AttackerInfo info) {}

    @Override
    public int compareTo(IDamageInvoker o) {
        return o.getPriority() - this.getPriority();
    }
}
