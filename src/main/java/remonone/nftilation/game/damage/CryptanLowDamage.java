package remonone.nftilation.game.damage;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.Store;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.roles.Cryptan;
import remonone.nftilation.utils.PlayerUtils;

public class CryptanLowDamage extends BaseDamageHandler {
    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void OnDamageHandle(EntityDamageEvent e) {
        if(!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        if(player.getHealth() - e.getFinalDamage() > 6D) return;
        if(!(Store.getInstance().getDataInstance().getPlayerRole(player.getUniqueId()) instanceof Cryptan)) return;
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, player);
        World world = player.getWorld();
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, .5f, 1f);
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        int upgradeLevel = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        int power = Math.min(upgradeLevel, 2);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, power, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 10 * 20, power, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, power, false, false));
    }
}
