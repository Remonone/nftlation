package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.components.ItemStatModifierComponent;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.utils.ColorUtils;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.ResetUtils;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public abstract class Role implements Cloneable, Listener {
    
    @Getter
    private final static List<Role> roles = new ArrayList<>();
    
    public abstract Material getMaterial();
    
    public abstract String getRoleName();
    
    public abstract List<String> getRoleDescription();
    
    public abstract String getRoleID();
    
    public abstract int getRoleIndex();
    
    protected abstract void setPlayer(Player player, int upgradeLevel);
    protected void killPlayer(Player player) {}
    
    public static void UpdatePlayerAbilities(Player player, Role role, int upgradeLevel) {
        ResetUtils.globalResetPlayerStats(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                role.setPlayer(player, upgradeLevel);
            }
        }.runTaskLater(Nftilation.getInstance(), 1);
    }

    protected ItemStack getSword(int level) {
        Material mat = level > 1 ? Material.IRON_SWORD : Material.STONE_SWORD;
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(RoleConstant.DEFAULT_SWORD_NAME);
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return stack;
    }
    protected ItemStack getPickaxe(int level) {
        Material mat = level > 1 ? Material.IRON_PICKAXE : Material.STONE_PICKAXE;
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(RoleConstant.DEFAULT_PICKAXE_NAME);
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return stack;
    }
    protected ItemStack getAxe(int level) {
        Material mat = level > 1 ? Material.IRON_AXE : Material.STONE_AXE;
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(RoleConstant.DEFAULT_AXE_NAME);
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return stack;
    }
    protected ItemStack getShovel(int level) {
        Material mat = level > 1 ? Material.IRON_SPADE : Material.STONE_SPADE;
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(RoleConstant.DEFAULT_SHOVEL_NAME);
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return stack;
    }

    protected List<ItemStack> getAbilityItems(int level) {
        return Collections.emptyList();
    }
    
    protected ItemStack getHelmet(Player player, int level) {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        setMetaToDefaultArmor(player, helmet);
        return helmet;
    }
    protected ItemStack getChestplate(Player player, int level) {
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        setMetaToDefaultArmor(player, chestplate);
        return chestplate;
    }
    protected ItemStack getLeggings(Player player, int level) {
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        setMetaToDefaultArmor(player, leggings);
        return leggings;
    }
    protected ItemStack getBoots(Player player, int level) {
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        setMetaToDefaultArmor(player, boots);
        return boots;
    }
    
    private void setMetaToDefaultArmor(Player player, ItemStack stack) {
        ItemStatModifierComponent.markItemAsDefault(stack);
        LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
        PlayerData data = Store.getInstance().getDataInstance().FindPlayerByName(player.getUniqueId()).getData();
        meta.setColor(ColorUtils.TranslateToColor(ChatColor.getByChar(data.getTeam().getTeamColor())));
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
    }
    
    public static void registerRole(Class<? extends Role> role) {
        try {
            Role roleToAdd = role.getDeclaredConstructor().newInstance();
            if(roles.contains(roleToAdd)) return;
            getServer().getPluginManager().registerEvents(roleToAdd, Nftilation.getInstance());
            roles.add(roleToAdd);
        } catch(Exception e) {
            Logger.error("Error during registering the role: " + e.getMessage());
        }
    }
    
    public static void refillInventoryWithItems(Player player, Role role, int upgradeLevel) {
        player.getInventory().clear();
        SetInventoryItems(player, role, upgradeLevel);
    }

    public static void SetInventoryItems(Player player, Role role, int upgradeLevel) {
        Inventory inventory = player.getInventory();
        clearPlayerItems(player);
        ItemStack[] itemStacks = Arrays.asList(
                role.getSword(upgradeLevel), 
                role.getPickaxe(upgradeLevel),
                role.getAxe(upgradeLevel),
                role.getShovel(upgradeLevel)).toArray(new ItemStack[0]);
        SetOwner(player, itemStacks);
        for(ItemStack stack : itemStacks) {
            ItemStatModifierComponent.markItemAsUndroppable(stack);
            ItemStatModifierComponent.markItemAsUnstorable(stack);
        }
        inventory.addItem(itemStacks);
        FillEquipment(player, role, upgradeLevel);
        role.giveAbilityItems(player, upgradeLevel);
    }
    
    protected void giveAbilityItems(Player player, int upgradeLevel) {
        for(ItemStack stack : getAbilityItems(upgradeLevel)) {
            SetOwner(player, stack);
            ItemStatModifierComponent.markItemAsUndroppable(stack);
            ItemStatModifierComponent.markItemAsUnstorable(stack);
            ItemStatModifierComponent.markItemAsUncraftable(stack);
            player.getInventory().addItem(stack);
        }
    }
    
    public static void OnDie(Player player, Role role) {
        ResetUtils.globalResetPlayerStats(player);
        role.killPlayer(player);
    }

    private static void SetOwner(Player owner, ItemStack... itemStacks) {
        for (ItemStack stack : itemStacks) {
            if(stack == null || stack.getAmount() < 1 || stack.getType() == Material.AIR) continue;
            NBT.modify(stack, nbt -> {
                nbt.setString("owner", owner.getUniqueId().toString());
            });
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static void clearPlayerItems(Player player) {
        Spliterator<ItemStack> itemStackSpliterator = player.getInventory().spliterator();
        while(itemStackSpliterator.tryAdvance(itemStack -> {
            if(itemStack == null) return;
            String owner = NBT.get(itemStack, nbt -> (String) nbt.getString("owner"));
            if(StringUtils.isEmpty(owner)) return;
            if(!owner.equals(player.getUniqueId().toString())) return;
            player.getInventory().remove(itemStack);
        }));
    }

    private static void FillEquipment(Player player, Role role, int upgradeLevel) {
        EntityEquipment equipment = player.getEquipment();
        ItemStack helmet = role.getHelmet(player, upgradeLevel);
        ItemStack chestplate = role.getChestplate(player, upgradeLevel);
        ItemStack leggins = role.getLeggings(player, upgradeLevel);
        ItemStack boots = role.getBoots(player, upgradeLevel);
        SetOwner(player, helmet, chestplate, leggins, boots);
        ItemStatModifierComponent.markItemAsUnstorable(helmet);
        ItemStatModifierComponent.markItemAsUnstorable(chestplate);
        ItemStatModifierComponent.markItemAsUnstorable(leggins);
        ItemStatModifierComponent.markItemAsUnstorable(boots);
        ItemStatModifierComponent.markItemAsUndroppable(helmet);
        ItemStatModifierComponent.markItemAsUndroppable(chestplate);
        ItemStatModifierComponent.markItemAsUndroppable(leggins);
        ItemStatModifierComponent.markItemAsUndroppable(boots);
        equipment.setHelmet(helmet);
        equipment.setChestplate(chestplate);
        equipment.setLeggings(leggins);
        equipment.setBoots(boots);
    }
    
    public static Role getRoleByID(String roleID) {
        return roles.stream().filter(role -> role.getRoleID().equals(roleID)).findFirst().orElse(null);
    }

    @Override
    public Role clone() {
        try {
            return (Role) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
