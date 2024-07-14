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
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.roles.Role;

public class InventoryUtils {
    
    public static void clearInventory(Player player) {
        player.getInventory().clear();
    }
    
    public static void giveRoleSelector(Player player) {
        DataInstance instance = Store.getInstance().getDataInstance();
        Role role = instance.getPlayerRole(player.getUniqueId());
        Material material = role != null ? role.getMaterial() : Material.RED_GLAZED_TERRACOTTA;
        
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
    
    public static void setCooldownForItem(ItemStack item,  long cooldown) {
        NBT.modify(item, nbt -> {nbt.setLong("cooldown", System.currentTimeMillis() + cooldown * DataConstants.ONE_SECOND);});
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
