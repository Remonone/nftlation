package remonone.nftilation.game.ingame.actions.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.TeamData;
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
        if(!e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) return;
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
        }
    }

    private void AwardTeam(String team) {
        Logger.broadcast(team + " awarded.");
    }
}
