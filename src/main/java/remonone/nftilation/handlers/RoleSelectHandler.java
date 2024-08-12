package remonone.nftilation.handlers;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import remonone.nftilation.Store;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.events.OnRoleSelectEvent;
import remonone.nftilation.events.OnRuneSelectEvent;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.runes.Rune;
import remonone.nftilation.utils.InventoryUtils;


public class RoleSelectHandler implements Listener {
    
    @EventHandler
    public void onRoleSelect(final OnRoleSelectEvent event) {
        Player player = event.getPlayer();
        Role role = event.getRole();
        boolean isUpdated = Store.getInstance().getDataInstance().updatePlayerRole(event.getRole(), player.getUniqueId());
        if(!isUpdated) {    
            player.sendMessage(MessageConstant.ROLE_ERROR);
            return;
        }
        InventoryUtils.clearInventory(event.getPlayer());
        InventoryUtils.fillPlayerLobbyInventory(event.getPlayer());
        event.getPlayer().closeInventory();
        player.sendMessage(MessageConstant.SUCCESSFUL_SELECTION + role.getName());
    }
    
    @EventHandler
    public void onRuneSelect(final OnRuneSelectEvent event) {
        Player player = event.getPlayer();
        Rune rune = event.getRune();
        boolean isUpdated = Store.getInstance().getDataInstance().updatePlayerRune(rune, player.getUniqueId());
        if(!isUpdated) {
            player.sendMessage(MessageConstant.RUNE_ERROR);
            return;
        }
        InventoryUtils.clearInventory(player);
        InventoryUtils.fillPlayerLobbyInventory(player);
        event.getPlayer().closeInventory();
        player.sendMessage(MessageConstant.SUCCESSFUL_SELECTION + rune.getName());
    }
}
