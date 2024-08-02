package remonone.nftilation.game.damage;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.roles.Cryptomarine;
import remonone.nftilation.utils.PlayerUtils;

public class CryptomarineDeathHandler extends BaseDamageHandler {
    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void OnEntityDamageHandle(EntityDamageByEntityEvent e) {
        Player player = (Player) e.getEntity();
        if(!(Store.getInstance().getDataInstance().getPlayerRole(player.getUniqueId()) instanceof Cryptomarine)) return;
        if(player.getHealth() - e.getFinalDamage() > 0) return;
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, player);
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        int upgradeLevel = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        if(upgradeLevel < 3) return;
        Location location = player.getLocation();
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 1000, false, false));
        TNTPrimed entity = player.getWorld().spawn(location, TNTPrimed.class);
        entity.setMetadata("cryptomarine", new FixedMetadataValue(Nftilation.getInstance(), 10));
        entity.setFuseTicks(0);
        EntityHandleComponent.setEntityOwner(entity, player);
        entity.setIsIncendiary(true);
    }
}
