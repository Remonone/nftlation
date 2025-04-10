package remonone.nftilation.utils;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import remonone.nftilation.Store;
import remonone.nftilation.components.ItemStatModifierComponent;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.events.OnCooldownApplyEvent;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.runes.Rune;

import static org.bukkit.Bukkit.getServer;

public class InventoryUtils {
    
    public static void clearInventory(Player player) {
        player.getInventory().clear();
    }
    
    public static void fillPlayerLobbyInventory(Player player) {
        giveRoleSelector(player);
        giveRuneSelector(player);
    }
    
    public static void giveRoleSelector(Player player) {
        DataInstance instance = Store.getInstance().getDataInstance();
        Role role = instance.getPlayerRole(player.getUniqueId());
        Material material = role != null ? role.getMaterial() : Material.CYAN_GLAZED_TERRACOTTA;
        
        ItemStack roleSelector = new ItemStack(material);
        ItemMeta itemMeta = roleSelector.getItemMeta();

        itemMeta.setDisplayName(NameConstants.ROLE_SELECTOR);
        if(role != null) {
            itemMeta.addEnchant(Enchantment.MENDING, 1, false);
        }
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        roleSelector.setItemMeta(itemMeta);
        ItemStatModifierComponent.markItemAsUndroppable(roleSelector);
        ItemStatModifierComponent.markItemAsUnstorable(roleSelector);
        ItemStatModifierComponent.markItemAsUncraftable(roleSelector);
        player.getInventory().addItem(roleSelector);
    }

    public static void giveRuneSelector(Player player) {
        DataInstance instance = Store.getInstance().getDataInstance();
        Rune rune = instance.getPlayerRune(player.getUniqueId());
        Material material = rune != null ? rune.getMaterial() : Material.BLACK_GLAZED_TERRACOTTA;

        ItemStack roleSelector = new ItemStack(material);
        ItemMeta itemMeta = roleSelector.getItemMeta();

        itemMeta.setDisplayName(NameConstants.RUNE_SELECTOR);
        if(rune != null) {
            itemMeta.addEnchant(Enchantment.MENDING, 1, false);
        }
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
        roleSelector.setItemMeta(itemMeta);
        ItemStatModifierComponent.markItemAsUndroppable(roleSelector);
        ItemStatModifierComponent.markItemAsUnstorable(roleSelector);
        ItemStatModifierComponent.markItemAsUncraftable(roleSelector);
        player.getInventory().addItem(roleSelector);
    }
    
    public static void setCooldownForItem(PlayerModel model, ItemStack item, float cooldown) {
        OnCooldownApplyEvent event = new OnCooldownApplyEvent(model, cooldown);
        getServer().getPluginManager().callEvent(event);
        long finalCooldown = (long) event.getCooldown() * DataConstants.ONE_SECOND;
        NBT.modify(item, nbt -> {nbt.setLong("cooldown", System.currentTimeMillis() + finalCooldown);});
    }
    
    public static boolean isCooldownRemain(ItemStack item) {
        long cooldown = NBT.get(item, nbt -> (Long) nbt.getLong("cooldown"));
        return cooldown > System.currentTimeMillis();
    }
    
    public static void notifyAboutCooldown(Player player, ItemStack item) {
        player.playSound(player.getLocation(), Sound.ENTITY_CAT_HURT, 1f, 1f);
        long cooldown = NBT.get(item, nbt -> (Long) nbt.getLong("cooldown"));
        player.sendMessage(ChatColor.RED + MessageConstant.ITEM_COOLDOWN + (int)((cooldown - System.currentTimeMillis()) / 1000));
    }
    
}
