package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.InventoryUtils;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class Indian extends Role {
    @Override
    public Material getMaterial() {
        return Material.BOOK;
    }

    @Override
    public String getRoleName() {
        return "Indian";
    }

    @Override
    public List<String> getRoleDescription() {
        return Arrays.asList(RoleConstant.INDIAN_DESCRIPTION_1, 
                RoleConstant.INDIAN_DESCRIPTION_2, 
                RoleConstant.INDIAN_DESCRIPTION_3,
                RoleConstant.INDIAN_DESCRIPTION_4,
                RoleConstant.INDIAN_DESCRIPTION_5);
    }

    @Override
    public String getRoleID() {
        return "IN";
    }

    @Override
    public int getRoleIndex() {
        return 31;
    }

    @Override
    public void setPlayer(Player player, Map<String, Object> params) {
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        float speedAddition = .05F * (upgradeLevel + 1);
        player.setWalkSpeed(DataConstants.PLAYER_SPEED + speedAddition);
    }
    
    @Override
    public ItemStack getPickaxe(Map<String, Object> params) {
        ItemStack stack = new ItemStack(Material.WOOD_PICKAXE);
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        switch (upgradeLevel) {
            case 1:
                stack = new ItemStack(Material.IRON_PICKAXE);
                stack.addEnchantment(Enchantment.DIG_SPEED, 1);
                stack.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
                break;
            case 2:
                stack = new ItemStack(Material.IRON_PICKAXE);
                stack.addEnchantment(Enchantment.DIG_SPEED, 1);
                stack.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 2);
                stack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
                break;
            case 3:
                stack = new ItemStack(Material.DIAMOND_PICKAXE);
                stack.addEnchantment(Enchantment.DIG_SPEED, 4);
                stack.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                stack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
                stack.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
                break;
        }
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(RoleConstant.INDIAN_PICKAXE_NAME);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        stack.setItemMeta(meta);
        return stack;
    }
    
    @Override
    protected List<ItemStack> getAbilityItems(Map<String, Object> params) {
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        ItemStack blocks = new ItemStack(Material.STONE);
        switch (upgradeLevel) {
            case 1:
                blocks = new ItemStack(Material.WOOL);
                NBT.modify(blocks, nbt -> {
                    nbt.setString("block-type", Material.WOOL.toString());
                    nbt.setInteger("amount", 16);
                });
                break;
            case 2:
                blocks = new ItemStack(Material.SANDSTONE);
                NBT.modify(blocks, nbt -> {
                    nbt.setString("block-type", Material.SANDSTONE.toString());
                    nbt.setInteger("amount", 32);
                });
                break;
            case 3:
                blocks = new ItemStack(Material.ENDER_STONE);
                NBT.modify(blocks, nbt -> {
                    nbt.setString("block-type", Material.ENDER_STONE.toString());
                    nbt.setInteger("amount", 32);
                });
                break;            
        }
        ItemMeta blockMeta = blocks.getItemMeta();
        blockMeta.setDisplayName(RoleConstant.INDIAN_BLOCK_ABILITY);
        blocks.setItemMeta(blockMeta);
        ItemStack itemStack = new ItemStack(Material.TORCH);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName(RoleConstant.INDIAN_RECALL_ABILITY);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        NBT.modify(itemStack, nbt -> {nbt.setString("indian", "recall");});
        return Arrays.asList(blocks, itemStack);
    }
    
    @EventHandler
    public void onItemInteract(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if(item == null || item.getAmount() < 0 || item.getType() == Material.AIR) {
            return;
        }
        if(!(Store.getInstance().getDataInstance().getPlayerRole(player.getUniqueId()) instanceof Indian)) return;
        String block = NBT.get(item, nbt -> (String)nbt.getString("block-type"));
        if(StringUtils.isEmpty(block)) return;
        event.setCancelled(true);
        if(InventoryUtils.isCooldownRemain(item)) {
            InventoryUtils.notifyAboutCooldown(player, item);
            return;
        }
        int amount = NBT.get(item, nbt -> (Integer)nbt.getInteger("amount"));
        Material mat = Material.getMaterial(block);
        ItemStack stack = new ItemStack(mat);
        stack.setAmount(amount);
        player.getInventory().addItem(stack);
        World world = player.getWorld();
        world.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, .5f, 1f);
        InventoryUtils.setCooldownForItem(item, RoleConstant.INDIAN_BLOCK_COOLDOWN);
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        Player accessor = e.getPlayer();
        DataInstance data = Store.getInstance().getDataInstance();
        Role role = data.getPlayerRole(accessor.getUniqueId());
        if(!(role instanceof Indian)) {
            return;
        }
        if(e.getFrom().toVector().distance(e.getTo().toVector()) < .1f) return;
        Map<String, Object> params = data.getPlayerParams(accessor.getUniqueId());
        if(!params.containsKey("recall")) return;
        int taskId = (int)params.get("recall");
        params.remove("recall");
        getServer().getScheduler().cancelTask(taskId);
        accessor.sendMessage(ChatColor.GOLD + RoleConstant.INDIAN_RECALL_CANCEL);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if(item == null || item.getAmount() < 0 || item.getType() == Material.AIR) {
            return;
        }
        if(!(Store.getInstance().getDataInstance().getPlayerRole(player.getUniqueId()) instanceof Indian)) return;
        String isRecall = NBT.get(item, nbt -> (String) nbt.getString("indian"));
        if(StringUtils.isEmpty(isRecall) || !isRecall.equals("recall")) return;
        event.setCancelled(true);
        GameInstance instance = GameInstance.getInstance();
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        PlayerModel model = instance.getPlayerModelFromTeam(team, player);
        Map<String, Object> params = model.getParameters();
        if(params.containsKey("recall")) {
            player.sendMessage(ChatColor.GOLD + RoleConstant.INDIAN_RECALL_ACTIVE);
            return;
        }
        player.sendMessage(ChatColor.GOLD + MessageConstant.START_RECALL);
        
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        int upgradeLevel = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                params.remove("recall");
                GameInstance.getInstance().teleportPlayerToBase(team, player);
            }
        };
        int delay = upgradeLevel > 2 ? RoleConstant.INDIAN_RECALL_MAX_LEVEL : RoleConstant.INDIAN_RECALL_MIN_LEVEL;
        runnable.runTaskLater(Nftilation.getInstance(), delay * 20);
        int taskId = runnable.getTaskId();
        params.put("recall", taskId);
    }
    
}
