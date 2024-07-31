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
import remonone.nftilation.components.ItemStatModifierComponent;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.ColorUtils;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerUtils;
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
    
    protected abstract void setPlayer(Player player, Map<String, Object> params);
    protected void killPlayer(Player player) {}
    
    public static void UpdatePlayerAbilities(PlayerModel model) {
        ResetUtils.globalResetPlayerStats(model.getReference());
        Map<String, Object> params = model.getParameters();
        if(!PlayerUtils.validateParams(params)) {
            Logger.error("Cannot update player abilities for: " + model.getReference().getDisplayName());
            return;
        }
        Role role = getRoleByID(params.get(PropertyConstant.PLAYER_ROLE_ID).toString());
        new BukkitRunnable() {
            @Override
            public void run() {
                role.setPlayer(model.getReference(), params);
            }
        }.runTaskLater(Nftilation.getInstance(), 1);
    }

    public static void UpdatePlayerAbilities(Player player) {
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, player);
        UpdatePlayerAbilities(model);
    }

    protected ItemStack getSword(Map<String, Object> params) {
        Material mat = Material.STONE_SWORD;
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(RoleConstant.DEFAULT_SWORD_NAME);
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return stack;
    }
    protected ItemStack getPickaxe(Map<String, Object> params) {
        Material mat = Material.STONE_PICKAXE;
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(RoleConstant.DEFAULT_PICKAXE_NAME);
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return stack;
    }
    protected ItemStack getAxe(Map<String, Object> params) {
        Material mat = Material.STONE_AXE;
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(RoleConstant.DEFAULT_AXE_NAME);
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return stack;
    }
    protected ItemStack getShovel(Map<String, Object> params) {
        Material mat = Material.STONE_SPADE;
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(RoleConstant.DEFAULT_SHOVEL_NAME);
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return stack;
    }

    protected List<ItemStack> getAbilityItems(Map<String, Object> params) {
        return Collections.emptyList();
    }
    
    protected ItemStack getHelmet(Map<String, Object> params) {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        String teamName = params.get(PropertyConstant.PLAYER_TEAM_NAME).toString();
        setMetaToDefaultArmor(teamName, helmet);
        return helmet;
    }
    protected ItemStack getChestplate(Map<String, Object> params) {
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        String teamName = params.get(PropertyConstant.PLAYER_TEAM_NAME).toString();
        setMetaToDefaultArmor(teamName, chestplate);
        return chestplate;
    }
    protected ItemStack getLeggings(Map<String, Object> params) {
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        String teamName = params.get(PropertyConstant.PLAYER_TEAM_NAME).toString();
        setMetaToDefaultArmor(teamName, leggings);
        return leggings;
    }
    protected ItemStack getBoots(Map<String, Object> params) {
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        String teamName = params.get(PropertyConstant.PLAYER_TEAM_NAME).toString();
        setMetaToDefaultArmor(teamName, boots);
        return boots;
    }
    
    private void setMetaToDefaultArmor(String teamName, ItemStack stack) {
        LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        meta.setColor(ColorUtils.TranslateToColor(ChatColor.getByChar(team.getTeamColor())));
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
    
    public static void refillInventoryWithItems(PlayerModel player) {
        player.getReference().getInventory().clear();
        SetInventoryItems(player);
    }

    public static void SetInventoryItems(PlayerModel model) {
        Player player = model.getReference();
        Inventory inventory = player.getInventory();
        clearPlayerItems(player);
        Map<String, Object> params = model.getParameters();
        if(!PlayerUtils.validateParams(params)) {
            Logger.error("Cannot set inventory items for player: " + model.getReference().getDisplayName());
        }
        Role role = getRoleByID(params.getOrDefault(PropertyConstant.PLAYER_ROLE_ID, "_").toString());
        
        ItemStack[] itemStacks = Arrays.asList(
                role.getSword(params), 
                role.getPickaxe(params),
                role.getAxe(params),
                role.getShovel(params)).toArray(new ItemStack[0]);
        SetOwner(player, itemStacks);
        for(ItemStack stack : itemStacks) {
            ItemStatModifierComponent.markItemAsUndroppable(stack);
            ItemStatModifierComponent.markItemAsUnstorable(stack);
        }
        inventory.addItem(itemStacks);
        FillEquipment(player, params);
        role.giveAbilityItems(player, params);
    }
    
    private void giveAbilityItems(Player player, Map<String, Object> params) {
        ItemStack[] abilityItems = getAbilityItems(params).toArray(new ItemStack[0]);
        SetOwner(player, abilityItems);
        for(int i = 0; i < abilityItems.length; i++) {
            ItemStack stack = abilityItems[i];
            ItemStack existingItem = player.getInventory().getItem(8 - i);
            ItemStatModifierComponent.markItemAsUndroppable(stack);
            ItemStatModifierComponent.markItemAsUnstorable(stack);
            ItemStatModifierComponent.markItemAsUncraftable(stack);
            player.getInventory().setItem(8 - i, stack);
            if(existingItem != null) {
                player.getInventory().addItem(existingItem);
            }
        }
    }
    
    public static void onDie(Player player, Role role) {
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
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if(offHand == null || offHand.getAmount() < 1 || offHand.getType().equals(Material.AIR)) return;
        String owner = NBT.get(offHand, nbt -> (String) nbt.getString("owner"));
        if(StringUtils.isEmpty(owner)) return;
        if(!owner.equals(player.getUniqueId().toString())) return;
        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
    }

    private static void FillEquipment(Player player, Map<String, Object> params) {
        EntityEquipment equipment = player.getEquipment();
        if(!PlayerUtils.validateParams(params)) {
            Logger.error("Cannot fill equipment for player: " + player.getDisplayName());
            return;
        }
        Role role = getRoleByID(params.get(PropertyConstant.PLAYER_ROLE_ID).toString());
        ItemStack helmet = role.getHelmet(params);
        ItemStack chestplate = role.getChestplate(params);
        ItemStack leggings = role.getLeggings(params);
        ItemStack boots = role.getBoots(params);
        SetOwner(player, helmet, chestplate, leggings, boots);
        ItemStatModifierComponent.markItemAsDefault(helmet);
        ItemStatModifierComponent.markItemAsDefault(chestplate);
        ItemStatModifierComponent.markItemAsDefault(leggings);
        ItemStatModifierComponent.markItemAsDefault(boots);
        ItemStatModifierComponent.markItemAsUnstorable(helmet);
        ItemStatModifierComponent.markItemAsUnstorable(chestplate);
        ItemStatModifierComponent.markItemAsUnstorable(leggings);
        ItemStatModifierComponent.markItemAsUnstorable(boots);
        ItemStatModifierComponent.markItemAsUndroppable(helmet);
        ItemStatModifierComponent.markItemAsUndroppable(chestplate);
        ItemStatModifierComponent.markItemAsUndroppable(leggings);
        ItemStatModifierComponent.markItemAsUndroppable(boots);
        ItemStack equippedHelmet = equipment.getHelmet();
        if(equippedHelmet != null && !ItemStatModifierComponent.checkItemIfDefault(equippedHelmet)) player.getInventory().addItem(equippedHelmet.clone());
        equipment.setHelmet(helmet);
        ItemStack equippedChestplate = equipment.getChestplate();
        if(equippedChestplate != null && !ItemStatModifierComponent.checkItemIfDefault(equippedChestplate)) player.getInventory().addItem(equippedChestplate.clone());
        equipment.setChestplate(chestplate);
        ItemStack equippedLeggings = equipment.getLeggings();
        if(equippedLeggings != null && !ItemStatModifierComponent.checkItemIfDefault(equippedLeggings)) player.getInventory().addItem(equippedLeggings.clone());
        equipment.setLeggings(leggings);
        ItemStack equippedBoots = equipment.getBoots();
        if(equippedBoots != null && !ItemStatModifierComponent.checkItemIfDefault(equippedBoots)) player.getInventory().addItem(equippedBoots.clone());
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
