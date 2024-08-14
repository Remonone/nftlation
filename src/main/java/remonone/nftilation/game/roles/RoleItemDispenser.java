package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.utils.ColorUtils;
import remonone.nftilation.utils.NestedObjectFetcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class RoleItemDispenser {
    
    //TODO: Replace for Team Upgrades
    private static final Map<ItemType, Material> defaultItems = new HashMap<ItemType, Material>() {{
        put(ItemType.SWORD, Material.STONE_SWORD);
        put(ItemType.PICKAXE, Material.STONE_PICKAXE);
        put(ItemType.AXE, Material.STONE_AXE);
        put(ItemType.SHOVEL, Material.STONE_SPADE);
        put(ItemType.HELMET, Material.LEATHER_HELMET);
        put(ItemType.CHESTPLATE, Material.LEATHER_CHESTPLATE);
        put(ItemType.LEGGINGS, Material.LEATHER_LEGGINGS);
        put(ItemType.BOOTS, Material.LEATHER_BOOTS);
    }};
    
    public static ItemStack getItem(ItemType type, Map<String, Object> playerParams, Map<String, Object> roleMeta) {
        if(roleMeta.containsKey(type.name)) {
            return getRoleItem(type.name, playerParams, roleMeta);
        } else {
            return getDefaultItem(type, playerParams);
        }
    }
    
    private static ItemStack getDefaultItem(ItemType type,  Map<String, Object> playerParams) {
        // TODO: Team upgrades
        ItemStack itemStack = new ItemStack(defaultItems.get(type));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(itemMeta);
        if(type.isArmor) {
            String teamName = (String) playerParams.get(PropertyConstant.PLAYER_TEAM_NAME);
            LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
            ITeam team = GameInstance.getInstance().getTeam(teamName);
            meta.setColor(ColorUtils.TranslateToColor(ChatColor.getByChar(team.getTeamColor())));
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }
    
    private static ItemStack getRoleItem(String type, Map<String, Object> playerParams, Map<String, Object> roleMeta) {
        int upgradeLevel = (int)playerParams.getOrDefault(PropertyConstant.PLAYER_LEVEL_PARAM, 1);
        String itemMaterial = (String)getMetaInfo(type + MetaConstants.META_ITEM_TYPE, roleMeta, upgradeLevel);
        ItemStack itemStack = new ItemStack(Material.getMaterial(itemMaterial));
        if(itemStack.getType().equals(Material.AIR)) return itemStack;
        List<EnchantInfo> enchantList = getEnchantments(type, upgradeLevel, roleMeta);
        if(enchantList != null) {
            for (EnchantInfo enchantInfo : enchantList) {
                itemStack.addUnsafeEnchantment(Enchantment.getByName(enchantInfo.enchantment), enchantInfo.level);
            }
        }
        String itemName = (String)getMetaInfo(type + MetaConstants.META_ITEM_NAME, roleMeta, upgradeLevel);
        ItemMeta meta =  itemStack.getItemMeta();
        meta.setDisplayName(itemName);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        Object rawNBT = getMetaInfo(type + MetaConstants.META_ITEM_NBT, roleMeta, upgradeLevel);
        if(rawNBT != null) {
            Map<String, String> nbtData = (Map<String, String>) rawNBT;
            NBT.modify(itemStack, nbt -> {
                for(Map.Entry<String, String> entry: nbtData.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    nbt.setString(key, value);
                }
            });
        }
        return itemStack;
    }

    private static List<EnchantInfo> getEnchantments(String type, int upgradeLevel, Map<String, Object> roleMeta) {
        return (List<EnchantInfo>) getMetaInfo(type + MetaConstants.META_ITEM_ENCHANTS, roleMeta, upgradeLevel);
    }


    private static Object getMetaInfo(String path, Map<String, Object> meta, int level) {
        return NestedObjectFetcher.getNestedObject(path, meta, level);
    }
    
    @Getter
    public enum ItemType {
        SWORD("sword", false),
        PICKAXE("pickaxe", false),
        AXE("axe", false),
        SHOVEL("shovel", false),
        HELMET("helmet", true),
        CHESTPLATE("chestplate", true),
        LEGGINGS("leggings", true),
        BOOTS("boots", true);
        
        private final String name;
        private final boolean isArmor;
        
        ItemType(String name, boolean isArmor) {
            this.name = name;
            this.isArmor = isArmor;
        }
    }
    
    @Data
    @SerializableAs("EnchantInfo")
    @AllArgsConstructor
    public static class EnchantInfo implements Cloneable, ConfigurationSerializable {
        private String enchantment;
        private int level;

        @Override
        public Map<String, Object> serialize() {
            return Collections.emptyMap();
        }
        
        public static EnchantInfo deserialize(Map<String, Object> map) {
            String enchant = "";
            int power = 0;
            if(map.containsKey("enchant")) {
                enchant = (String) map.get("enchant");
            }
            if(map.containsKey("power")) {
                power = (int) map.get("power");
            }
            return new EnchantInfo(enchant, power);
        }

        @Override
        public EnchantInfo clone() {
            try {
                return (EnchantInfo) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }
}
