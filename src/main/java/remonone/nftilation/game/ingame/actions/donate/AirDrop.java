package remonone.nftilation.game.ingame.actions.donate;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.actions.IAction;

import java.util.Map;

public class AirDrop implements IAction {
    @Override
    public void Init(Map<String, Object> params) {
        if(!params.containsKey(PropertyConstant.ACTION_TEAM)) {
            throw new NullPointerException("Couldn't initiate AirDrop action. Team is missing!");
        }
        String teamName = (String) params.get(PropertyConstant.ACTION_TEAM);
        if(!GameInstance.getInstance().isTeamActive(teamName)) {
            return;
        }
        TeamSpawnPoint point = GameInstance.getInstance().getTeamSpawnPoint(teamName);
        Location location = point.getAirDropPosition();
        location.getBlock().setType(Material.CHEST);
        Chest chest = (Chest) location.getBlock().getState();
        chest.setCustomName(ChatColor.GOLD + "Air Drop");
        chest.update();
        Inventory chestInventory = chest.getBlockInventory();
        chestInventory.addItem(new ItemStack(Material.DIAMOND, 5));
        chestInventory.addItem(new ItemStack(Material.IRON_INGOT, 32));
        chestInventory.addItem(new ItemStack(Material.COAL, 32));
        chestInventory.addItem(new ItemStack(Material.COOKED_BEEF, 32));
    }

    @Override
    public String getTitle() {
        return "Air Drop";
    }

    @Override
    public String getDescription() {
        return "You are eligible";
    }

    @Override
    public Sound getSound() {
        return Sound.ENTITY_VILLAGER_YES;
    }
}
