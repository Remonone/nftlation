package remonone.nftilation.game.damage;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import remonone.nftilation.Store;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.roles.Cryptomarine;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.utils.AttackPresets;
import remonone.nftilation.utils.PlayerUtils;

public class CryptomarineDeathHandler extends BaseDamageHandler {
    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void OnDamageHandle(EntityDamageEvent e) {
        Player player = (Player) e.getEntity();
        Role role = Store.getInstance().getDataInstance().getPlayerRole(player.getUniqueId());
        if(!(role instanceof Cryptomarine)) return;
        if(player.getHealth() - e.getFinalDamage() > 0) return;
        if(EntityDamageEvent.DamageCause.VOID.equals(e.getCause())) return;
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        if(!(Boolean)role.getMetaByName(model, MetaConstants.META_CRYPTOMARINE_EXPLOSION_AVAILABILITY)) return;
        Location location = player.getLocation();
        double range = (Double)role.getMetaByName(model, MetaConstants.META_CRYPTOMARINE_EXPLOSION_RANGE);
        double damage = (Double)role.getMetaByName(model, MetaConstants.META_CRYPTOMARINE_EXPLOSION_DAMAGE);
        AttackPresets.summonExplosion(location, player, range, damage, 2, 15, 100, 2, true, EntityDamageEvent.DamageCause.VOID);
    }
}
