package remonone.nftilation.handlers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.constants.BlockConstants;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.Logger;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class OnBlockDestroyHandler implements Listener {
    
    private static final Random RANDOM = new Random();
    
    private static final List<Material> GRAVEL_DROPS = Arrays.asList(Material.FEATHER, Material.STRING, Material.FLINT);
    
    @EventHandler
    public void onPlayerInteract(BlockBreakEvent e) {
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        Block block = e.getBlock();
        Player player = e.getPlayer();
        if(BlockConstants.isRespawnableBlock(e.getBlock())) {
            
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
            boolean isResourcesRespawnable = (boolean)rules.getRuleOrDefault(PropertyConstant.RULE_RESOURCE_RESPAWNABLE, false);
            long timer = BlockConstants.getMaterialCooldown(mat);
            int tokens = BlockConstants.getTokensFromBlock(mat);
            PlayerData data = Store.getInstance().getDataInstance().FindPlayerByName(player.getName());
            if(drops.length != 0) {
                GameInstance.getInstance().awardPlayer(data.getTeam().getTeamName(), player, tokens);
            }
            if(timer == -1) return;
            if(!isResourcesRespawnable) {
                long spawnAt = (long) rules.getRuleOrDefault(PropertyConstant.RULE_RESOURCE_SPAWN_AUTO_ENABLE_AT, System.currentTimeMillis());
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
        
        if(block.getType() == Material.BEACON) {
            TeamData teamData = GameInstance.getInstance().getTeamByCorePosition(block.getLocation().toVector());
            Logger.debug(teamData.toString());
            TeamData playerTeam = Store.getInstance().getDataInstance().FindPlayerByName(player.getName()).getTeam();
            if(teamData.equals(playerTeam)) {
                e.setCancelled(true);
                return;
            }
            boolean isInvulnerable = (boolean)RuleManager.getInstance().getRuleOrDefault(PropertyConstant.RULE_CORE_INVULNERABLE, false);
            if(isInvulnerable) {
                player.sendMessage(ChatColor.RED + MessageConstant.CORE_INVULNERABLE);
                return;
            }
            String teamName = playerTeam.getTeamName();
            if(GameInstance.getInstance().damageCore(teamName)) {
                List<GameInstance.PlayerModel> players = GameInstance.getInstance().getTeamPlayers(teamName);
                for(GameInstance.PlayerModel model : players) {
                    GameInstance.getInstance().awardPlayer(teamName, model.getReference(), DataConstants.TOKEN_PER_DESTRUCTION);
                }
            }
        }
    }
    
    
}
