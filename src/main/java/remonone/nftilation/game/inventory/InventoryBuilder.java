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
import remonone.nftilation.game.models.IInventoryHelder;
import remonone.nftilation.game.roles.Guts;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.runes.Rune;
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
            if(role instanceof Guts) {
                if(!player.getDisplayName().contains("Jinrui_Saikyo")) {
                    continue;
                }
            }
            ItemStack itemStack;
            Material mat = roles.contains(role) ?
                    Material.RED_GLAZED_TERRACOTTA
                    : role.getMaterial();
            if(mat == Material.AIR) continue;
            itemStack = new ItemStack(mat);
            FillMeta(itemStack, role);
            NBT.modify(itemStack, nbt -> {
                nbt.setString(PropertyConstant.NBT_ROLE, role.getRoleID());
                nbt.setBoolean(PropertyConstant.NBT_ROLE_RESERVED, roles.contains(role));
            });
            inventory.setItem(role.getIndex(), itemStack);
        }
        return inventory;
    }

    public static Inventory getRuneSelectionInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 54, NameConstants.RUNE_SELECTION_TAB);
        for(Rune rune : Rune.getRunes()) {
            ItemStack itemStack;
            Material mat = rune.getMaterial();
            Logger.debug(mat.toString());
            if(mat == Material.AIR) continue;
            itemStack = new ItemStack(mat);
            FillMeta(itemStack, rune);
            NBT.modify(itemStack, nbt -> {
                nbt.setString(PropertyConstant.NBT_RUNE, rune.getRuneID());
            });
            inventory.setItem(rune.getIndex(), itemStack);
        }
        return inventory;
    }
    
    private static void FillMeta(ItemStack itemStack, IInventoryHelder helder) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        String roleName = Optional.ofNullable(helder.getName()).orElse("none");
        List<String> descriptions = Optional.ofNullable(helder.getDescription()).orElse(Collections.emptyList());
        itemMeta.setDisplayName(roleName);
        itemMeta.setLore(descriptions);
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
