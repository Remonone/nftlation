package remonone.nftilation.game.ingame.actions.world;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.utils.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class Checker implements IAction, Listener {
    
    private final static Material REQUIRED_TYPE = Material.YELLOW_FLOWER;
    private final static int REQUIRED_AMOUNT = 5;
    private Map<String, Boolean> teamsCompletion;
    
    @Override
    public void Init(Map<String, Object> params) {
        teamsCompletion = new HashMap<>();
        List<TeamData> teams = Store.getInstance().getDataInstance().getTeamData();
        for(TeamData data : teams) {
            teamsCompletion.put(data.getTeamName(), false);
            Location loc = GameInstance.getInstance().getTeamSpawnPoint(data.getTeamName()).getCheckerChestPosition();
            if(loc != null) {
                Block block = loc.getBlock();
                block.setType(Material.CHEST);
                Chest chest = (Chest) block.getState();
                chest.setCustomName(NameConstants.CHECKER_CHEST);
            }
        }
        getServer().getPluginManager().registerEvents(this, Nftilation.getInstance());
    }

    @Override
    public String getTitle() {
        return "Checker";
    }

    @Override
    public String getDescription() {
        return "5 ромашек на базе соберешь, аирдроп заберешь";
    }

    @Override
    public Sound getSound() {
        return Sound.ENTITY_FIREWORK_BLAST;
    }
    
    @EventHandler
    public void onChestInventoryUpdate(InventoryClickEvent e) {
        Logger.broadcast(e.getAction().toString());
        if(!e.getAction().equals(InventoryAction.PLACE_ALL)) return;
        if(e.getView().getTopInventory().getName().equals(NameConstants.CHECKER_CHEST)) {
            Inventory chestInventory = e.getView().getTopInventory();
            ListIterator<ItemStack> iterator = chestInventory.iterator();
            int amount = 0;
            while(iterator.hasNext()) {
                ItemStack item = iterator.next();
                if(item.getType().equals(REQUIRED_TYPE)) {
                    amount += item.getAmount();
                }
            }
            if(amount > REQUIRED_AMOUNT) {
                Player player = (Player) e.getWhoClicked();
                String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
                
                teamsCompletion.put(team, true);
                AwardTeam(team);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    FailTask();
                }
            }.runTaskLater(Nftilation.getInstance(), 3 * DataConstants.TICKS_IN_MINUTE);
        }
    }

    private void AwardTeam(String team) {
        TeamSpawnPoint point = GameInstance.getInstance().getTeamSpawnPoint(team);
        Block block = point.getCheckerChestPosition().getBlock();
        block.setType(Material.AIR);
        block.setType(Material.CHEST);
        Chest chest = (Chest) block.getState();
        chest.setCustomName(NameConstants.CHECKER_CHEST);
        chest.getBlockInventory().setItem(13, new ItemStack(Material.DIAMOND));
        Firework fw = (Firework) point.getCheckerChestPosition().getWorld().spawnEntity(point.getCheckerChestPosition(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(2);
        fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME).flicker(true).build());

        fw.setFireworkMeta(fwm);
        fw.detonate();
    }

    private void FailTask() {
        for(Map.Entry<String, Boolean> entry : teamsCompletion.entrySet()) {
            if(entry.getValue()) continue;
            String team = entry.getKey();
            Location loc = GameInstance.getInstance().getTeamSpawnPoint(team).getCheckerChestPosition();
            GameInstance.getInstance().getTeamPlayers(team).forEach(playerModel -> {
                Player player = playerModel.getReference();
                player.playSound(player.getLocation(), Sound.ENTITY_GHAST_HURT, 1f, .3f);
                player.sendTitle("You were cursed by Brian!", "", 10, 60, 10);
            });
            loc.getBlock().setType(Material.AIR);
            WitherSkeleton skeleton = loc.getWorld().spawn(loc, WitherSkeleton.class);
            skeleton.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100F);
            skeleton.setCustomName(NameConstants.CHECKER_NAME);
            // TODO: Set equipment
        }
    }
}
