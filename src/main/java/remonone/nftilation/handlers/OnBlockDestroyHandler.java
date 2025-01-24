package remonone.nftilation.handlers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.constants.*;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.events.OnCoreDamageEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerUtils;
import remonone.nftilation.utils.VectorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;

public class OnBlockDestroyHandler implements Listener {
    
    private static final Random RANDOM = new Random();
    
    @EventHandler
    public void onPlayerInteract(BlockBreakEvent e) {
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        Block block = e.getBlock();
        Player player = e.getPlayer();
        if(BlockConstants.isRespawnableBlock(e.getBlock())) {
            List<MetadataValue> list = block.getMetadata("placer");
            if(!list.isEmpty()) {
                block.removeMetadata("placer", Nftilation.getInstance());
                return;
            }
            awardPlayer(block, player);
            setResetTimerForBlock(block);
            e.setDropItems(false);
            return;
        }
        List<Location> corePositions = ConfigManager.getInstance().getTeamSpawnList().stream().map(TeamSpawnPoint::getCoreCenter).map(center -> new Location(block.getWorld(), center.getBlockX(), center.getBlockY(), center.getBlockZ())).collect(Collectors.toList());
        if(corePositions.stream().anyMatch(position -> position.getBlock().equals(block.getLocation().getBlock()))) {
            e.setCancelled(true);
            tryAttackCore(player, block);
        }
    }

    private void setResetTimerForBlock(Block block) {
        Material mat = block.getType();
        
        RuleManager rules = RuleManager.getInstance();
        boolean isResourcesRespawnable = (boolean)rules.getRuleOrDefault(RuleConstants.RULE_RESOURCE_RESPAWNABLE, false);
        long timer = BlockConstants.getMaterialCooldown(mat);
        if(timer == -1) return;
        if(!isResourcesRespawnable) {
            long spawnAt = (long) rules.getRuleOrDefault(RuleConstants.RULE_RESOURCE_SPAWN_AUTO_ENABLE_AT, System.currentTimeMillis());
            timer += (spawnAt - System.currentTimeMillis()) / DataConstants.ONE_SECOND;
        }
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                block.setType(mat);
                block.setData(block.getData());
            }
        };
        getServer().getScheduler().runTaskLater(Nftilation.getInstance(), task, timer * DataConstants.TICKS_IN_SECOND);
    }

    private void awardPlayer(Block block, Player player) {
        ItemStack stack = player.getInventory().getItemInMainHand();
        ItemStack[] drops;
        List<Material> customDrops = BlockConstants.getCustomDrops(block.getType());
        if(customDrops != null) {
            drops = new ItemStack[1];
            drops[0] = new ItemStack(customDrops.get(RANDOM.nextInt(customDrops.size())));
        } else {
            drops = block.getDrops(stack).stream()
                    .map(ItemStack::new)
                    .toArray(ItemStack[]::new);
        }
        ItemStack destroyedBy = player.getInventory().getItemInMainHand();
        List<ItemStack> itemStacks = scaleDropsOnFortune(drops, destroyedBy);
        for(ItemStack drop : itemStacks) {
            block.getWorld().dropItemNaturally(block.getLocation().add(VectorUtils.ONE.clone().multiply(.5)), drop);
        }
        int tokens = BlockConstants.getTokensFromBlock(block.getType());
        PlayerInteractComponent component = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        if(drops.length != 0) {
            component.adjustPlayerTokens(player, tokens, TransactionType.RESOURCE_GAIN);
        }
    }

    private List<ItemStack> scaleDropsOnFortune(ItemStack[] drops, ItemStack destroyedBy) {
        int fortuneLevel = destroyedBy.getEnchantments().getOrDefault(Enchantment.LOOT_BONUS_BLOCKS, 0);
        List<ItemStack> modifiedList = new ArrayList<>();
        for(ItemStack drop : drops) {
            int scale = Math.max(0, RANDOM.nextInt(fortuneLevel + 2) - 1);
            drop.setAmount(drop.getAmount() * (scale + 1));
            modifiedList.add(drop);
        }
        return modifiedList;
    }

    private void tryAttackCore(Player player, Block block) {
        ITeam team = GameInstance.getInstance().getTeamByCorePosition(block.getLocation());
        OnCoreDamageEvent event = new OnCoreDamageEvent(team.getTeamName(), PlayerUtils.getModelFromPlayer(player));
        getServer().getPluginManager().callEvent(event);
    }

    @EventHandler
    public void onPlayerPlaceBlock(final PlayerInteractEvent e) {
        if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Player player = e.getPlayer();
        ItemStack stack = player.getInventory().getItemInMainHand();
        if(stack.getType().equals(Material.TNT)) {
            Location loc = e.getClickedBlock().getRelative(e.getBlockFace()).getLocation();
            e.setCancelled(true);
            TNTPrimed tntPrimed = player.getWorld().spawn(loc, TNTPrimed.class);
            stack.setAmount(stack.getAmount() - 1);
            tntPrimed.setMetadata("invoker", new FixedMetadataValue(Nftilation.getInstance(), player));
        }
        if(BlockConstants.isRespawnableBlock(stack.getType())) {
            Location location = e.getClickedBlock().getRelative(e.getBlockFace()).getLocation();
            e.setCancelled(true);
            Block block = location.getBlock();
            block.setType(stack.getType());
            stack.setAmount(stack.getAmount() - 1);
            block.setMetadata("placer", new FixedMetadataValue(Nftilation.getInstance(), player));
        }
    }
}
