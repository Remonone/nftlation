package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.*;
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
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.damage.CryptanLowDamage;
import remonone.nftilation.game.models.EffectPotion;
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
    protected List<ItemStack> getAbilityItems(Map<String, Object> params) {
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        int hookAvailability = (int) Optional.of(getMetaInfo(MetaConstants.META_HOOK_AVAILABILITY, upgradeLevel)).orElse(1);
        if(upgradeLevel < hookAvailability) return Collections.emptyList();
        String name = (String) Optional.of(getMetaInfo(MetaConstants.META_CRYPTAN_ABILITY_NAME, upgradeLevel)).orElse("UNDEFINED");
        ItemStack itemStack = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        NBT.modify(itemStack, nbt -> {nbt.setString(RoleConstant.CRYPTAN_NBT_CONTAINER, RoleConstant.CRYPTAN_NBT_HOOK);});
        List<ItemStack> items = new ArrayList<>();
        items.add(itemStack);
        return items;
    }
    
    @SuppressWarnings({"deprecation", "unchecked"})
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
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(model == null) return;
        int level = (Integer)model.getParameters().getOrDefault(PropertyConstant.PLAYER_LEVEL_PARAM, 1);
        if(state.equals(PlayerFishEvent.State.FISHING)) {
            double range = (Double) Optional.of(getMetaInfo(MetaConstants.META_CRYPTAN_ABILITY_RANGE, level)).orElse(2.5D);
            Vector newHookVelocity = event.getHook().getVelocity().multiply(range);
            event.getHook().setVelocity(newHookVelocity);
        }
        if(!(state == PlayerFishEvent.State.IN_GROUND || state == PlayerFishEvent.State.CAUGHT_ENTITY)) return;
        Location flyTo = event.getHook().getLocation();
        event.setCancelled(true);
        double speed = (Double) Optional.of(getMetaInfo(MetaConstants.META_CRYPTAN_ABILITY_SPEED, level)).orElse(.125D);
        double airborne = (Double) Optional.of(getMetaInfo(MetaConstants.META_CRYPTAN_ABILITY_AIRBORNE, level)).orElse(1.1D);
        double verticalShift = (Double) Optional.of(getMetaInfo(MetaConstants.META_CRYPTAN_ABILITY_VERTICAL_SHIFT, level)).orElse(3D);
        // ==set Velocity==
        Vector direction = flyTo.toVector().subtract(player.getLocation().toVector()).add(new Vector(0, verticalShift, 0)).multiply(
                new Vector(airborne, 1, airborne))
                .normalize();
        Vector destinationVelocity = direction.multiply(flyTo.distance(player.getLocation()) * speed);
        player.setVelocity(destinationVelocity);
        // =====
        
        event.getHook().remove();
        if(event.getCaught() != null && event.getCaught() instanceof Player) {
            Player caught = (Player) event.getCaught();
            if(!GameInstance.getInstance().checkIfPlayersInSameTeam(player, caught)) {
                List<EffectPotion> negatives = (List<EffectPotion>) getMetaInfo(MetaConstants.META_CRYPTAN_ABILITY_EFFECT_TARGET, level);
                for(EffectPotion effect : negatives) {
                    caught.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect.getEffect()), effect.getDuration(), effect.getStrength(), false, false));
                }
            }
            List<EffectPotion> positives = (List<EffectPotion>) getMetaInfo(MetaConstants.META_CRYPTAN_ABILITY_EFFECT_BOOST, level);
            for(EffectPotion effect : positives) {
                caught.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect.getEffect()), effect.getDuration(), effect.getStrength(), false, false));
            }
            World world = caught.getWorld();
            world.playSound(caught.getLocation(), Sound.ENTITY_SLIME_ATTACK, 3f, .3f);
            world.playSound(caught.getLocation(), Sound.ENTITY_WITHER_SKELETON_HURT, .1f, .5f);
            double damage = (Double) Optional.of(getMetaInfo(MetaConstants.META_CRYPTAN_ABILITY_DAMAGE, level)).orElse(2D);
            EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(player, caught, EntityDamageEvent.DamageCause.CRAMMING, damage);
            getServer().getPluginManager().callEvent(e);
            if(!e.isCancelled()) {
                caught.setHealth(caught.getHealth() - e.getFinalDamage());
                caught.setLastDamageCause(e);
            }
        }

        Integer cooldown = (Integer)Optional.of(getMetaInfo(MetaConstants.META_CRYPTAN_ABILITY_COOLDOWN, level)).orElse(6);
        InventoryUtils.setCooldownForItem(model, stack, cooldown);
    }

    @Override
    public List<IDamageHandler> getDamageHandlers() {
        return Collections.singletonList(new CryptanLowDamage());
    }

}
