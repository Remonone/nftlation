package remonone.nftilation.game.inventory;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import remonone.nftilation.Store;
import remonone.nftilation.components.ItemStatModifierComponent;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.shop.content.*;
import remonone.nftilation.game.shop.registry.ShopItemRegistry;
import remonone.nftilation.utils.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class InventoryBuilder {

    public static Inventory getRoleSelectionInventory(Player player, String teamName) {
        Inventory inventory = Bukkit.createInventory(player, 54, NameConstants.ROLE_SELECTION_TAB);

        DataInstance dataInstance = Store.getInstance().getDataInstance();
        List<DataInstance.PlayerInfo> infos = dataInstance.getTeamPlayers(teamName);

        List<Role> roles = infos.stream()
                .map(DataInstance.PlayerInfo::getRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Role> registeredRoles = Role.getRoles();
        for(Role role : registeredRoles) {
            ItemStack itemStack;
            Material mat = roles.contains(role) ?
                    Material.RED_GLAZED_TERRACOTTA
                    : role.getMaterial();
            itemStack = new ItemStack(mat);
            FillMeta(itemStack, role);
            NBT.modify(itemStack, nbt -> {
                nbt.setString(PropertyConstant.NBT_ROLE, role.getRoleID());
                nbt.setBoolean(PropertyConstant.NBT_ROLE_RESERVED, roles.contains(role));
            });
            inventory.setItem(role.getRoleIndex(), itemStack);
        }
        return inventory;
    }

    private static void FillMeta(ItemStack itemStack, Role role) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(role.getRoleName());
        itemMeta.setLore(role.getRoleDescription());
        itemStack.setItemMeta(itemMeta);
    }
    
    public static Inventory buildShopKeeperInventory(Player player, CategoryElement el) {
        Inventory inventory = Bukkit.createInventory(player, 27, NameConstants.SHOP_TAB);
        for(Map.Entry<Integer, String> element : el.getExpandableElements().entrySet()) {
            IShopElement shopElement = ShopItemRegistry.getItem(element.getValue());
            if(shopElement == null) {
                Logger.error("Category " + el.getId() + "contains item which not exists! Id: " + element.getValue() + ". Skipping...");
                continue;
            }
            ItemStack stack = shopElement.getDisplay();
            NBT.modify(stack, nbt -> {
                nbt.setString("id", shopElement.getId());
            });
            if(shopElement instanceof ItemElement) {
                ItemElement itemElement = (ItemElement) shopElement;
                ItemMeta itemMeta = stack.getItemMeta();
                itemMeta.setLore(Collections.singletonList("Price: " + itemElement.getPrice()));
                stack.setItemMeta(itemMeta);
            }
            if(shopElement instanceof ServiceElement) {
                ServiceElement serviceElement = (ServiceElement) shopElement;
                ItemMeta itemMeta = stack.getItemMeta();
                itemMeta.setLore(Collections.singletonList("Price: " + serviceElement.getPrice()));
                stack.setItemMeta(itemMeta);
                inventory.setItem(element.getKey(), stack);
            }
            ItemStatModifierComponent.markItemAsUndroppable(stack);
            inventory.setItem(element.getKey(), stack);
        }
        return inventory;
    }
}
