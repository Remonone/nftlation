package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
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
    public String getRoleID() {
        return "IN";
    }

    public Indian() {
        super("IN");
    }
    
    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> params) {
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
        NBT.modify(itemStack, nbt -> {nbt.setString(RoleConstant.INDIAN_NBT_CONTAINER, RoleConstant.INDIAN_NBT_RECALL);});
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
        InventoryUtils.setCooldownForItem(PlayerUtils.getModelFromPlayer(player), item, RoleConstant.INDIAN_BLOCK_COOLDOWN);
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
        PlayerModel model = PlayerUtils.getModelFromPlayer(accessor);
        Map<String, Object> params = model.getParameters();
        if(!params.containsKey(RoleConstant.INDIAN_PARAM_RECALL)) return;
        int taskId = (int)params.get(RoleConstant.INDIAN_PARAM_RECALL);
        params.remove(RoleConstant.INDIAN_PARAM_RECALL);
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
        String isRecall = NBT.get(item, nbt -> (String) nbt.getString(RoleConstant.INDIAN_NBT_CONTAINER));
        if(StringUtils.isEmpty(isRecall) || !isRecall.equals(RoleConstant.INDIAN_NBT_RECALL)) return;
        event.setCancelled(true);
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        Map<String, Object> params = model.getParameters();
        String team = (String)params.get(PropertyConstant.PLAYER_TEAM_NAME);
        if(params.containsKey(RoleConstant.INDIAN_PARAM_RECALL)) {
            player.sendMessage(ChatColor.GOLD + RoleConstant.INDIAN_RECALL_ACTIVE);
            return;
        }
        player.sendMessage(ChatColor.GOLD + MessageConstant.START_RECALL);
        
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        int upgradeLevel = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                params.remove(RoleConstant.INDIAN_PARAM_RECALL);
                GameInstance.getInstance().teleportPlayerToBase(team, player);
            }
        };
        int delay = upgradeLevel > 2 ? RoleConstant.INDIAN_RECALL_MAX_LEVEL : RoleConstant.INDIAN_RECALL_MIN_LEVEL;
        runnable.runTaskLater(Nftilation.getInstance(), delay * 20);
        int taskId = runnable.getTaskId();
        params.put(RoleConstant.INDIAN_PARAM_RECALL, taskId);
    }
}
