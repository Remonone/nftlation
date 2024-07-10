package remonone.nftilation.game.inventory;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.roles.Role;

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
    
    public static Inventory getShopKeeperInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, NameConstants.SHOP_TAB);
        ItemStack dynamite = new ItemStack(Material.TNT);
        addPurchasableItemToInventory(inventory, dynamite, 200, 9);
        ItemStack food = buildItem(Material.BREAD, 1, "Food");
        addCategoryItemToInventory(inventory, food, DataConstants.NBT_CATEGORY_FOOD, 11);
        ItemStack baseRepairment = buildItem(Material.ANVIL, 1, "Repair base");
        addServiceItemToInventory(inventory, baseRepairment, "base-repair", 200, 13);
        ItemStack potions = buildItem(Material.POTION, 1, "Potions");
        addCategoryItemToInventory(inventory, potions, DataConstants.NBT_CATEGORY_POTIONS, 15);
        ItemStack upgrades = buildItem(Material.NETHER_STAR, 1, "Upgrades");
        addCategoryItemToInventory(inventory, upgrades, DataConstants.NBT_CATEGORY_UPGRADES, 17);
        return inventory;
    }
    
    public static Inventory getShopKeeperPotions(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, NameConstants.SHOP_TAB);
        ItemStack healthRegeneration = buildItem(Material.POTION, 1, "Health regeneration");
        buildPotionData(healthRegeneration, Color.FUCHSIA, PotionType.REGEN);
        addPurchasableItemToInventory(inventory, healthRegeneration, 40, 11);
        ItemStack fireResistance = buildItem(Material.POTION, 1, "Fire Resistance");
        buildPotionData(fireResistance, Color.ORANGE, PotionType.FIRE_RESISTANCE);
        addPurchasableItemToInventory(inventory, fireResistance, 40, 13);
        ItemStack invisibility = buildItem(Material.POTION, 1, "Invisibility");
        buildPotionData(invisibility, Color.GRAY, PotionType.INVISIBILITY);
        addPurchasableItemToInventory(inventory, invisibility, 40, 15);
        return inventory;
    }
    
    public static Inventory getShopKeeperGoods(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, NameConstants.SHOP_TAB);
        ItemStack apple = buildItem(Material.GOLDEN_APPLE, 1);
        addPurchasableItemToInventory(inventory, apple, 150, 12);
        ItemStack steak = buildItem(Material.COOKED_BEEF, 12);
        addPurchasableItemToInventory(inventory, steak, 50, 14);
        return inventory;
    }

    public static Inventory getShopKeeperUpgrades(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, NameConstants.SHOP_TAB);
        ItemStack upgradeToSecond = buildItem(Material.NETHER_STAR, 1, "Upgrades to second tier");
        addServiceItemToInventory(inventory, upgradeToSecond, "second-tier", 800, 12);
        ItemStack upgradeToThird = buildItem(Material.NETHER_STAR, 1, "Upgrades to third tier");
        addServiceItemToInventory(inventory, upgradeToThird, "third-tier", 3000, 14);
        return inventory;
    }

    private static void buildPotionData(ItemStack itemStack, Color color, PotionType type) {
        PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
        meta.setColor(color);
        meta.setBasePotionData(new PotionData(type));
        itemStack.setItemMeta(meta);
    }

    private static void addPurchasableItemToInventory(Inventory inventory, ItemStack stack, int price, int index) {
        NBT.modify(stack, nbt -> {
            nbt.setString(PropertyConstant.NBT_PRODUCT_TYPE, DataConstants.NBT_TYPE_ITEM);
            nbt.setInteger(PropertyConstant.NBT_PRICE, price);
        });
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(Collections.singletonList("Price: " + price));
        stack.setItemMeta(meta);
        inventory.setItem(index, stack);
    }
    
    private static void addCategoryItemToInventory(Inventory inventory, ItemStack stack, String categoryName, int index) {
        NBT.modify(stack, nbt -> {
            nbt.setString(PropertyConstant.NBT_PRODUCT_TYPE, DataConstants.NBT_TYPE_CATEGORY);
            nbt.setString(PropertyConstant.NBT_CATEGORY_NAME, categoryName);
        });
        inventory.setItem(index, stack);
    }

    private static void addServiceItemToInventory(Inventory inventory, ItemStack stack, String serviceName, int price, int index) {
        NBT.modify(stack, nbt -> {
            nbt.setString(PropertyConstant.NBT_PRODUCT_TYPE, DataConstants.NBT_TYPE_SERVICE);
            nbt.setString(PropertyConstant.NBT_SERVICE_NAME, serviceName);
            nbt.setInteger(PropertyConstant.NBT_PRICE, price);
        });
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(Collections.singletonList("Price: " + price));
        stack.setItemMeta(meta);
        inventory.setItem(index, stack);
    }

    private static ItemStack buildItem(Material mat, int amount) {
        ItemStack itemStack = new ItemStack(mat);
        itemStack.setAmount(amount);
        return itemStack;
    }
    
    private static ItemStack buildItem(Material mat, int amount, String name) {
        ItemStack itemStack = new ItemStack(mat);
        itemStack.setAmount(amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
