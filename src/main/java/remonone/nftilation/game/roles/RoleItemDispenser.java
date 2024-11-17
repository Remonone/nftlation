package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.ColorUtils;
import remonone.nftilation.utils.NestedObjectFetcher;
import remonone.nftilation.utils.PlayerUtils;

import java.util.*;

@SuppressWarnings("unchecked")
public class RoleItemDispenser {
    
    public static ItemStack getItem(ItemType type, Map<String, Object> playerParams, Map<String, Object> roleMeta) {
        if(roleMeta.containsKey(type.name)) {
            return getRoleItem(type.name, playerParams, roleMeta);
        } else {
            return getDefaultItem(type, playerParams);
        }
    }
    
    private static ItemStack getDefaultItem(ItemType type,  Map<String, Object> playerParams) {
        String teamName = (String) playerParams.get(PropertyConstant.PLAYER_TEAM_NAME);
        if(StringUtils.isBlank(teamName)) return new ItemStack(Material.AIR);
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        if(team == null) return new ItemStack(Material.AIR);
        Map<String, Object> teamParams = team.getParameters();
        int level = (Integer) teamParams.getOrDefault(PropertyConstant.TEAM_UTILITY_ITEM_LEVEL, 0);
        String materialName = (String)NestedObjectFetcher.getNestedObject(MetaConstants.META_UPGRADES_UTILITY + type.name, MetaConfig.getInstance().getUpgrades(), level);
        if(StringUtils.isBlank(materialName)) return new ItemStack(Material.AIR);
        Material material = Material.getMaterial(materialName);
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.setDisplayName(type.inGameName);
        itemStack.setItemMeta(itemMeta);
        if(type.isArmor) {
            LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
            if(meta != null) {
                meta.setColor(ColorUtils.TranslateToColor(ChatColor.getByChar(team.getTeamColor())));
                itemStack.setItemMeta(meta);
            }
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

    public static List<ItemStack> getCustomAbilityItems(Player player) {
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        List<ItemStack> stacks = (List<ItemStack>) model.getParameters().get(PropertyConstant.PLAYER_CUSTOM_ABILITY_ITEMS);
        if(stacks == null) return Collections.emptyList();
        List<ItemStack> list = new ArrayList<>();
        for(ItemStack stack: stacks) {
            list.add(stack.clone());
        }
        return list;
    }

    @Getter
    public enum ItemType {
        SWORD("sword", RoleConstant.DEFAULT_SWORD_NAME, false),
        PICKAXE("pickaxe", RoleConstant.DEFAULT_PICKAXE_NAME, false),
        AXE("axe", RoleConstant.DEFAULT_AXE_NAME, false),
        SHOVEL("shovel", RoleConstant.DEFAULT_SHOVEL_NAME, false),
        HELMET("helmet", "-", true),
        CHESTPLATE("chestplate", "-",true),
        LEGGINGS("leggings", "-",true),
        BOOTS("boots", "-",true);
        
        private final String name;
        private final String inGameName;
        private final boolean isArmor;
        
        ItemType(String name, String inGameName, boolean isArmor) {
            this.name = name;
            this.inGameName = inGameName;
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
