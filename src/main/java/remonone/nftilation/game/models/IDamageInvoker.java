package remonone.nftilation.game.models;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import remonone.nftilation.utils.PlayerUtils;

public interface IDamageInvoker {
    void OnEntityDamageDealing(EntityDamageByEntityEvent e, PlayerUtils.AttackerInfo info);
}
