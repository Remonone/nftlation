package remonone.nftilation.handlers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.constants.*;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.game.rules.RuleManager;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;

public class OnBlockDestroyHandler implements Listener {
    
    private static final Random RANDOM = new Random();
    
    private static final List<Material> GRAVEL_DROPS = Arrays.asList(Material.FEATHER, Material.STRING, Material.FLINT);
    
    @EventHandler
    public void onPlayerInteract(BlockBreakEvent e) {
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        Block block = e.getBlock();
        Player player = e.getPlayer();
        PlayerInteractComponent component = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        if(BlockConstants.isRespawnableBlock(e.getBlock())) {
            List<MetadataValue> list = e.getBlock().getMetadata("placer");
            if(!list.isEmpty()) {
                e.getBlock().removeMetadata("placer", Nftilation.getInstance());
                return;
            }
            ItemStack stack = player.getInventory().getItemInMainHand();
            ItemStack[] drops;
            if(block.getType().equals(Material.GRAVEL)) {
                drops = new ItemStack[1];
                drops[0] = new ItemStack(GRAVEL_DROPS.get(RANDOM.nextInt(3)));
            } else {
                drops = block.getDrops(stack).stream()
                        .map(ItemStack::new)
                        .toArray(ItemStack[]::new);
            }
            e.setCancelled(true);
            Material mat = block.getType();
            block.setType(Material.AIR);
            player.getInventory().addItem(drops);
            RuleManager rules = RuleManager.getInstance();
            boolean isResourcesRespawnable = (boolean)rules.getRuleOrDefault(RuleConstants.RULE_RESOURCE_RESPAWNABLE, false);
            long timer = BlockConstants.getMaterialCooldown(mat);
            int tokens = BlockConstants.getTokensFromBlock(mat);
            if(drops.length != 0) {
                component.adjustPlayerTokens(player, tokens, TransactionType.RESOURCE_GAIN);
            }
            if(timer == -1) return;
            if(!isResourcesRespawnable) {
                long spawnAt = (long) rules.getRuleOrDefault(RuleConstants.RULE_RESOURCE_SPAWN_AUTO_ENABLE_AT, System.currentTimeMillis());
                timer += (spawnAt - System.currentTimeMillis()) / DataConstants.ONE_SECOND;
            }
            timer *= 20;
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    block.setType(mat);
                }
            };
            getServer().getScheduler().runTaskLater(Nftilation.getInstance(), task, timer);
        }
        List<Vector> corePositions = ConfigManager.getInstance().getTeamSpawnList().stream().map(TeamSpawnPoint::getCoreCenter).collect(Collectors.toList());
        if(corePositions.stream().anyMatch(position -> position.distance(block.getLocation().toVector()) < DataConstants.ZERO_THRESHOLD)) {
            ITeam team = GameInstance.getInstance().getTeamByCorePosition(block.getLocation().toVector());
            String playerTeam = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
            e.setCancelled(true);
            if(team.getTeamName().equals(playerTeam)) {
                return;
            }
            boolean isInvulnerable = (boolean)RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_CORE_INVULNERABLE, false);
            if(isInvulnerable) {
                player.sendMessage(ChatColor.RED + MessageConstant.CORE_INVULNERABLE);
                return;
            }
            if(GameInstance.getInstance().damageCore(team.getTeamName(), true)) {
                List<PlayerModel> players = GameInstance.getInstance().getTeam(playerTeam).getPlayers();
                for(PlayerModel model : players) {
                    component.adjustPlayerTokens(model, DataConstants.TOKEN_PER_DESTRUCTION, TransactionType.DESTROY_GAIN);
                }
                block.setType(Material.AIR);
            }
        }
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
