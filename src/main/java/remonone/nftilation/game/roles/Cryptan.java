package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
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
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.utils.InventoryUtils;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class Cryptan extends Role {
    @Override
    public Material getMaterial() {
        return Material.GOLD_INGOT;
    }

    @Override
    public String getRoleName() {
        return "Cryptan";
    }

    @Override
    public List<String> getRoleDescription() {
        return Arrays.asList(RoleConstant.CRYPTAN_DESCRIPTION_1, RoleConstant.CRYPTAN_DESCRIPTION_2, RoleConstant.CRYPTAN_DESCRIPTION_3);
    }

    @Override
    public String getRoleID() {
        return "CY";
    }

    @Override
    public int getRoleIndex() {
        return 20;
    }
    

    @Override
    public void setPlayer(Player player, int upgradeLevel) {
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(RoleConstant.CRYPTAN_ATTACK_SPEED);
    }

    @Override
    protected ItemStack getSword(int upgradeLevel) {
        ItemStack itemStack = new ItemStack(Material.WOOD_SWORD);
        switch(upgradeLevel) {
            case 1:
                itemStack = new ItemStack(Material.IRON_SWORD);
                itemStack.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                break;
            case 2:
                itemStack = new ItemStack(Material.IRON_SWORD);
                itemStack.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                break;
            case 3:
                itemStack = new ItemStack(Material.DIAMOND_SWORD);
                itemStack.addEnchantment(Enchantment.DAMAGE_ALL, 4);
                break;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(RoleConstant.CRYPTAN_SWORD);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    
    @Override
    protected ItemStack getChestplate(Player player, int upgradeLevel) {
        ItemStack itemStack = new ItemStack(Material.WOOD_SWORD);
        switch(upgradeLevel) {
            case 1:
                itemStack = new ItemStack(Material.IRON_CHESTPLATE);
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                break;
            case 2:
                itemStack = new ItemStack(Material.IRON_CHESTPLATE);
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                break;
            case 3:
                itemStack = new ItemStack(Material.DIAMOND_CHESTPLATE);
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                break;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(RoleConstant.CRYPTAN_CHESTPLATE_NAME);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    
    @Override
    protected List<ItemStack> getAbilityItems(int upgradeLevel) {
        if(upgradeLevel < 2) return Collections.emptyList();
        ItemStack itemStack = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(RoleConstant.CRYPTAN_ABILITY);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        NBT.modify(itemStack, nbt -> {nbt.setString("cryptan", "hook");});
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
        GameInstance.PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, player);
        int setCooldown = model.getUpgradeLevel() == 3 ? RoleConstant.CRYPTAN_COOLDOWN_MAX_RANK : RoleConstant.CRYPTAN_COOLDOWN_LOW_RANK;
        InventoryUtils.setCooldownForItem(stack, setCooldown);
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if(player.getHealth() - event.getFinalDamage() > 6D) return;
        if(!(Store.getInstance().getDataInstance().getPlayerRole(player.getUniqueId()) instanceof Cryptan)) return;
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        GameInstance.PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, player);
        World world = player.getWorld();
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, .5f, 1f);
        int power = Math.min(model.getUpgradeLevel(), 2);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, power, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 10 * 20, power, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, power, false, false));
    }
}
