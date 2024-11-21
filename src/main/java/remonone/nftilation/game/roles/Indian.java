package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.constants.*;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.ColorUtils;
import remonone.nftilation.utils.PlayerUtils;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class Indian extends Role {
    
    @Override
    public String getRoleID() {
        return "IN";
    }

    public Indian() {
        super("IN");
        super.registerHandlers(new HashMap<String, IAbilityHandler>(){{
            put(RoleConstant.INDIAN_NBT_BLOCKS, new IAbilityHandler() {
                @Override
                public boolean executeHandle(PlayerModel model) {
                    return onBlockActivate(model);
                }

                @Override
                public float getCooldown(PlayerModel model) {
                    return ((Double)getMetaByName(model, MetaConstants.META_INDIAN_BLOCKS_COOLDOWN)).floatValue();
                }
            });
            put(RoleConstant.INDIAN_NBT_RECALL, new IAbilityHandler() {
                @Override
                public boolean executeHandle(PlayerModel model) {
                    return onRecall(model);
                }

                @Override
                public float getCooldown(PlayerModel model) {
                    return 0;
                }
            });
        }}, RoleConstant.INDIAN_NBT_CONTAINER);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> params) {
        List<ItemStack> stacks = new ArrayList<>();
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        Material material = Material.getMaterial((String)getMetaInfo(MetaConstants.META_INDIAN_BLOCKS_TYPE, upgradeLevel));
        int amount = (int)getMetaInfo(MetaConstants.META_INDIAN_BLOCKS_AMOUNT, upgradeLevel);
        ItemStack blocks = new ItemStack(material);
        if(material.equals(Material.WOOL)) {
            ITeam team = GameInstance.getInstance().getTeam((String) params.get(PropertyConstant.PLAYER_TEAM_NAME));
            char teamColor = team.getTeamColor();
            blocks = new ItemStack(material, 1, getColorData(teamColor));
        }
        NBT.modify(blocks, nbt -> {
            nbt.setString("block-type", material.toString());
            nbt.setInteger("amount", amount);
            nbt.setString(RoleConstant.INDIAN_NBT_CONTAINER, RoleConstant.INDIAN_NBT_BLOCKS);
            nbt.setString(RoleConstant.ROLE, getRoleID());
        });
        String blocksName = (String)getMetaInfo(MetaConstants.META_INDIAN_BLOCKS_NAME, upgradeLevel);
        List<String> blocksDescription = (List<String>)getMetaInfo(MetaConstants.META_INDIAN_BLOCKS_DESCRIPTION, upgradeLevel);
        ItemMeta blockMeta = blocks.getItemMeta();
        blockMeta.setDisplayName(blocksName);
        blockMeta.setLore(blocksDescription);
        blocks.setItemMeta(blockMeta);
        stacks.add(blocks);
        if((Boolean)getMetaInfo(MetaConstants.META_INDIAN_RECALL_AVAILABILITY, upgradeLevel)) {
            ItemStack itemStack = new ItemStack(Material.TORCH);
            String recallName = (String) getMetaInfo(MetaConstants.META_INDIAN_RECALL_NAME, upgradeLevel);
            List<String> recallDescription = (List<String>) getMetaInfo(MetaConstants.META_INDIAN_RECALL_DESCRIPTION, upgradeLevel);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setUnbreakable(true);
            meta.setDisplayName(recallName);
            meta.setLore(recallDescription);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            itemStack.setItemMeta(meta);
            NBT.modify(itemStack, nbt -> {
                nbt.setString(RoleConstant.INDIAN_NBT_CONTAINER, RoleConstant.INDIAN_NBT_RECALL);
                nbt.setString(RoleConstant.ROLE, getRoleID());
            });
            stacks.add(itemStack);
        }
        return stacks;
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
    
    
    private boolean onBlockActivate(PlayerModel model) {
        Player player = model.getReference();
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType() == Material.AIR || item.getAmount() < 0) {
            item = player.getInventory().getItemInOffHand();
        }
        String block = NBT.get(item, nbt -> (String)nbt.getString("block-type"));
        int amount = NBT.get(item, nbt -> (Integer)nbt.getInteger("amount"));
        Material mat = Material.getMaterial(block);

        ItemStack stack = new ItemStack(mat);
        ITeam team = PlayerUtils.getTeamFromPlayer(player);
        if(team == null) {
            return false;
        }
        if(mat.equals(Material.WOOL)) {
            stack = new ItemStack(mat, amount, DyeColor.getByColor(ColorUtils.TranslateToColor(ChatColor.getByChar(team.getTeamColor()))).getWoolData());
        }
        stack.setAmount(amount);
        player.getInventory().addItem(stack);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, .5f, 1f);
        return true;
    }
    
    private boolean onRecall(PlayerModel model) {
        Player player = model.getReference();
        Map<String, Object> params = model.getParameters();
        if(params.containsKey(RoleConstant.INDIAN_PARAM_RECALL)) {
            player.sendMessage(ChatColor.GOLD + RoleConstant.INDIAN_RECALL_ACTIVE);
            return false;
        }
        player.sendMessage(ChatColor.GOLD + MessageConstant.START_RECALL);
        double delay = (Double)getMetaByName(model, MetaConstants.META_INDIAN_CALLBACK_AWAIT);
        String teamName = (String) params.get(PropertyConstant.PLAYER_TEAM_NAME);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                params.remove(RoleConstant.INDIAN_PARAM_RECALL);
                GameInstance.getInstance().teleportPlayerToBase(teamName, player);
            }
        };
        runnable.runTaskLater(Nftilation.getInstance(),(long)(delay * DataConstants.TICKS_IN_SECOND));
        params.put(RoleConstant.INDIAN_PARAM_RECALL, runnable.getTaskId());
        return true;
    }
    
    @SuppressWarnings("deprecation")
    private byte getColorData(char color) {
        Color rawColor = ColorUtils.TranslateToColor(ChatColor.getByChar(color));
        return DyeColor.getByColor(rawColor).getWoolData();
    }
}
