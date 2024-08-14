package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.damage.CryptanLowDamage;
import remonone.nftilation.game.models.IDamageHandler;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.InventoryUtils;
import remonone.nftilation.utils.PlayerUtils;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class Cryptan extends Role {
    
    public Cryptan() {
        super("CY");
    }

    @Override
    public String getRoleID() {
        return "CY";
    }
    

    @Override
    public void setPlayer(Player player, Map<String, Object> params) {
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(RoleConstant.CRYPTAN_ATTACK_SPEED);
    }
    
    @Override
    protected List<ItemStack> getAbilityItems(Map<String, Object> params) {
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        int hookAvailability = (int) getMetaInfo(MetaConstants.META_HOOK_AVAILABILITY, upgradeLevel);
        if(upgradeLevel < hookAvailability) return Collections.emptyList();
        ItemStack itemStack = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(RoleConstant.CRYPTAN_ABILITY);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        NBT.modify(itemStack, nbt -> {nbt.setString(RoleConstant.CRYPTAN_NBT_CONTAINER, RoleConstant.CRYPTAN_NBT_HOOK);});
        List<ItemStack> items = new ArrayList<>();
        items.add(itemStack);
        return items;
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteract(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = player.getInventory().getItemInMainHand();
        if(!(Store.getInstance().getDataInstance().getPlayerRole(player.getUniqueId()) instanceof Cryptan)) return;
        if(InventoryUtils.isCooldownRemain(stack)) {
            InventoryUtils.notifyAboutCooldown(player, stack);
            event.setCancelled(true);
            return;
        }
        PlayerFishEvent.State state = event.getState();
        if(state.equals(PlayerFishEvent.State.FISHING)) {
            Vector newHookVelocity = event.getHook().getVelocity().multiply(RoleConstant.CRYPTAN_HOOK_STRENGTH);
            event.getHook().setVelocity(newHookVelocity);
        }
        if(!(state == PlayerFishEvent.State.IN_GROUND || state == PlayerFishEvent.State.CAUGHT_ENTITY)) return;

        Location flyTo = event.getHook().getLocation();
        event.setCancelled(true);
        
        // ==set Velocity==
        Vector direction = flyTo.toVector().subtract(player.getLocation().toVector()).add(new Vector(0, 3F, 0)).multiply(
                new Vector(RoleConstant.CRYPTAN_HOOK_AIRBORNE_MODIFIER, 1, RoleConstant.CRYPTAN_HOOK_AIRBORNE_MODIFIER))
                .normalize();
        Vector destinationVelocity = direction.multiply(flyTo.distance(player.getLocation()) * RoleConstant.CRYPTAN_HOOK_SPEED);
        player.setVelocity(destinationVelocity);
        // =====
        
        event.getHook().remove();
        if(event.getCaught() != null && event.getCaught() instanceof Player) {
            Player caught = (Player) event.getCaught();
            if(!GameInstance.getInstance().checkIfPlayersInSameTeam(player, caught)) {
                caught.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, RoleConstant.CRYPTAN_HOOK_NEGATIVE_SPELLS_DURATION * DataConstants.TICKS_IN_SECOND, 2, false, false));
                caught.addPotionEffect(new PotionEffect(PotionEffectType.POISON, RoleConstant.CRYPTAN_HOOK_NEGATIVE_SPELLS_DURATION * DataConstants.TICKS_IN_SECOND, 1, false, false));
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, RoleConstant.CRYPTAN_HOOK_BUFF_DURATION * DataConstants.TICKS_IN_SECOND, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, RoleConstant.CRYPTAN_HOOK_BUFF_DURATION * DataConstants.TICKS_IN_SECOND, 0, false, false));
            World world = caught.getWorld();
            world.playSound(caught.getLocation(), Sound.ENTITY_SLIME_ATTACK, 3f, .3f);
            world.playSound(caught.getLocation(), Sound.ENTITY_WITHER_SKELETON_HURT, .1f, .5f);
            EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(player, caught, EntityDamageEvent.DamageCause.CRAMMING, RoleConstant.CRYPTAN_HOOK_DAMAGE);
            getServer().getPluginManager().callEvent(e);
            if(!e.isCancelled()) {
                caught.setHealth(caught.getHealth() - e.getFinalDamage());
                caught.setLastDamageCause(e);
            }
        }
        
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, player);
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        int upgradeLevel = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        int setCooldown = upgradeLevel == 3 ? RoleConstant.CRYPTAN_COOLDOWN_MAX_RANK : RoleConstant.CRYPTAN_COOLDOWN_LOW_RANK;
        InventoryUtils.setCooldownForItem(model, stack, setCooldown);
    }

    @Override
    public List<IDamageHandler> getDamageHandlers() {
        return Collections.singletonList(new CryptanLowDamage());
    }

}
