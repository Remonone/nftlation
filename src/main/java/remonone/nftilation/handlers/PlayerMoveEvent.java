package remonone.nftilation.handlers;

import org.apache.commons.lang.ObjectUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import remonone.nftilation.Store;
import remonone.nftilation.game.DataInstance;

public class PlayerMoveEvent implements Listener {
    @EventHandler
    public void onPlayerMove(final org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(isNotAuthenticated(player)) {
            event.setCancelled(true);
        }
    }
    
    
    private boolean isNotAuthenticated(final Player player) {
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        return ObjectUtils.equals(dataInstance.FindPlayerByName(player.getName()), null);
    }
}
