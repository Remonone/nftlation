package remonone.nftilation.game.services;

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
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.IInventoryHelder;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.game.runes.Rune;
import remonone.nftilation.game.shop.content.*;
import remonone.nftilation.game.shop.registry.ShopItemRegistry;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerUtils;

import java.util.*;
import java.util.stream.Collectors;

public class InventoryService {
    
    private static final int ROW_SHIFT = 9;

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
        String name = Optional.ofNullable(helder.getName()).orElse("none");
        List<String> descriptions = Optional.ofNullable(helder.getDescription()).orElse(Collections.emptyList());
        itemMeta.setDisplayName(name);
        itemMeta.setLore(descriptions);
        itemStack.setItemMeta(itemMeta);
    }
    
    public static Inventory buildShopKeeperInventory(Player player, CategoryElement el) {
        Inventory inventory = Bukkit.createInventory(player, 27, NameConstants.SHOP_TAB + el.getExpandableName());
        Map<Integer, String> availableItems = getAvailableItems(player, el.getExpandableElements());
        Float discount = (Float) RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_PRICE_SCALE, 1F);
        for(Map.Entry<Integer, String> element : availableItems.entrySet()) {
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
                List<String> lore = new ArrayList<>();
                if(discount != 1F) {
                    lore.add("Discount: " + (float)Math.round((1f - discount) * 1000) / 10 + "%");
                }
                lore.add("Price: " + itemElement.getPrice() * discount);
                itemMeta.setLore(lore);
                stack.setItemMeta(itemMeta);
            }
            if(shopElement instanceof ServiceElement) {
                ServiceElement serviceElement = (ServiceElement) shopElement;
                ItemMeta itemMeta = stack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if(discount != 1F) {
                    lore.add("Discount: " + (float)Math.round((1f - discount) * 1000) / 10 + "%");
                }
                lore.add("Price: " + serviceElement.getPrice() * discount);
                itemMeta.setLore(lore);
                stack.setItemMeta(itemMeta);
            }
            ItemStatModifierComponent.markItemAsUndroppable(stack);
            inventory.setItem(element.getKey(), stack);
        }
        return inventory;
    }
    
    private static Map<Integer, String> getAvailableItems(Player player, List<String> ids) {
        List<String> filteredIds = filterIds(player, ids);
        Map<Integer, String> availableItems = new HashMap<>();
        int count = filteredIds.size();
        int slots = 9;

        int spacing = 1;

        for(int i = 2; i <= 4; i++) {
            int reservedSpace = count + (count - 1) * (i - 1);
            int remaining = slots - reservedSpace;
            if(remaining < 0) break;
            spacing = i;
            if(remaining % 2 == 0) break;
        }

        int finalReservation = count + (count - 1) * (spacing - 1);
        int offset = (slots - finalReservation) / 2;

        for(int i = 0; i < count; i++) {
            availableItems.put(offset + i * spacing + ROW_SHIFT, filteredIds.get(i));
        }

        return availableItems;
    }

    private static List<String> filterIds(Player player, List<String> ids) {
        List<String> result = new ArrayList<>();
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        Map<String, Object> requisites = new HashMap<>(model.getParameters());
        String teamName = (String)model.getParameters().get(PropertyConstant.PLAYER_TEAM_NAME);
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        if(team != null) {
            requisites.putAll(team.getParameters());            
        }
        for(String id : ids) {
            IShopElement element = ShopItemRegistry.getItem(id);
            if(element == null) continue;
            if(element.getRequisites().checkForRequisites(requisites)) {
                result.add(id);
            }
        }
        return result;
    }
}
