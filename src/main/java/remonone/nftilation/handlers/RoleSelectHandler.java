package remonone.nftilation.handlers;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import remonone.nftilation.Store;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.events.OnRoleSelectEvent;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.utils.InventoryUtils;


public class RoleSelectHandler implements Listener {
    
    @EventHandler
    public void onRoleSelect(final OnRoleSelectEvent event) {
        Player player = event.getPlayer();
        Role role = event.getRole();
        boolean isUpdated = Store.getInstance().getDataInstance().updatePlayerRole(event.getRole(), player.getName());
        if(!isUpdated) {    
            player.sendMessage(MessageConstant.ROLE_ERROR);
            return;
        }
        InventoryUtils.clearInventory(event.getPlayer());
        InventoryUtils.giveRoleSelector(event.getPlayer());
        event.getPlayer().closeInventory();
        player.sendMessage(MessageConstant.ROLE_SELECT + role.getRoleName());
    }
}
