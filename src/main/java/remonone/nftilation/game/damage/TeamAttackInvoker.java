package remonone.nftilation.game.damage;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import remonone.nftilation.Store;
import remonone.nftilation.utils.PlayerUtils;

public class TeamAttackInvoker extends BaseDamageInvoker {

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public void OnEntityDamageDealing(EntityDamageByEntityEvent e, PlayerUtils.AttackerInfo info) {
        Player attacker = info.attacker;
        if(!(e.getEntity() instanceof Player)) return;
        Player target = (Player)e.getEntity();
        String attackerTeam = Store.getInstance().getDataInstance().getPlayerTeam(attacker.getUniqueId());
        String targetTeam = Store.getInstance().getDataInstance().getPlayerTeam(target.getUniqueId());
        if(!attackerTeam.equals(targetTeam)) return;
        e.setCancelled(true);
    }
}
