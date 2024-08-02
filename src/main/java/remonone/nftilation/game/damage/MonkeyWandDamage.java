package remonone.nftilation.game.damage;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.IDamageInvoker;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.roles.Monkey;
import remonone.nftilation.utils.PlayerUtils;

public class MonkeyWandDamage implements IDamageInvoker {
    @Override
    public void OnEntityDamageDealing(EntityDamageByEntityEvent e, PlayerUtils.AttackerInfo info) {
        if(!(e.getEntity() instanceof Player)) return;
        Player attacker = info.attacker;
        Player victim = (Player) e.getEntity();
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if(!(Store.getInstance().getDataInstance().getPlayerRole(attacker.getUniqueId()) instanceof Monkey)) return;
        if(weapon == null || weapon.getAmount() < 1 || !weapon.getType().equals(Material.AIR)) return;
        String isStick = NBT.get(weapon, nbt -> (String) nbt.getString(RoleConstant.MONKEY_NBT_CONTAINER));
        if(StringUtils.isBlank(isStick) || !isStick.equals(RoleConstant.MONKEY_NBT_WAND)) return;
        GameInstance instance = GameInstance.getInstance();
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        String team = dataInstance.getPlayerTeam(attacker.getUniqueId());
        PlayerModel model = instance.getPlayerModelFromTeam(team, attacker);
        if(model == null) return;
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        int level = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        if(level == 2) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 10* DataConstants.TICKS_IN_SECOND, 1, false, true));
        } else if(level == 3) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 10*DataConstants.TICKS_IN_SECOND, 2, false, true));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5*DataConstants.TICKS_IN_SECOND, 1, false, true));
        }
    }
}
