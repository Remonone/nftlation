package remonone.nftilation.game.damage;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.events.OnCooldownApplyEvent;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.EffectPotion;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.roles.Monkey;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.utils.PlayerUtils;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class MonkeyWandDamage extends BaseDamageInvoker {
    @Override
    public int getPriority() {
        return 1;
    }

    @SuppressWarnings("unchecked")
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
        Role role = Role.getRoleByID((String)model.getParameters().get(PropertyConstant.PLAYER_ROLE_ID));
        if(role == null) return;
        List<EffectPotion> effects = (List<EffectPotion>) role.getMetaInfo(MetaConstants.META_MONKEY_WAND_EFFECTS, level);
        for(EffectPotion potion : effects) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.getByName(potion.getEffect()), potion.getDuration(), potion.getStrength(), true, true));
        }
        String weaponName = weapon.getItemMeta().getDisplayName();
        if(!weaponName.equals(role.getMetaByName(model, MetaConstants.META_MONKEY_WAND_NAME_CHARGED))) return;
        if(level > (int)role.getMetaInfo(MetaConstants.META_MONKEY_WAND_DISORIENTATION_LEVEL, level)) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            weaponMeta.setDisplayName((String)role.getMetaByName(model, MetaConstants.META_MONKEY_WAND_NAME_DEFAULT));
            float duration = ((Double)role.getMetaInfo(MetaConstants.META_MONKEY_WAND_DISORIENTATION_DURATION, level)).floatValue();
            PlayerModel victimModel = PlayerUtils.getModelFromPlayer(victim);
            victimModel.getParameters().put(PropertyConstant.PLAYER_DISORIENTATION_DURATION, System.currentTimeMillis() + duration);
            attacker.getWorld().playSound(attacker.getLocation(), Sound.BLOCK_ANVIL_HIT, 1f, .8f);
            OnCooldownApplyEvent event = new OnCooldownApplyEvent(model, ((Double)role.getMetaByName(model, MetaConstants.META_MONKEY_WAND_DISORIENTATION_COOLDOWN)).floatValue());
            getServer().getPluginManager().callEvent(event);
            float cooldown = (float)event.getCooldown();
            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemMeta meta = weapon.getItemMeta();
                    meta.setDisplayName((String)role.getMetaByName(model, MetaConstants.META_MONKEY_WAND_NAME_DEFAULT));
                    weapon.setItemMeta(meta);
                    attacker.getWorld().playSound(attacker.getLocation(), Sound.ITEM_TOTEM_USE, 1f, .8f);
                }
            }.runTaskLater(Nftilation.getInstance(), (long)cooldown * DataConstants.TICKS_IN_SECOND);
        }
    }
}
