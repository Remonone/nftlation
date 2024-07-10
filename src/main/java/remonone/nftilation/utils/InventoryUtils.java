package remonone.nftilation.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import remonone.nftilation.Store;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.roles.Role;

public class InventoryUtils {
    
    public static void clearInventory(Player player) {
        player.getInventory().clear();
    }
    
    public static void giveRoleSelector(Player player) {
        DataInstance instance = Store.getInstance().getDataInstance();
        Role role = instance.getPlayerRole(player.getName());
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

        player.getInventory().addItem(roleSelector);
    }
}
