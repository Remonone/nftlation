package remonone.nftilation.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionDestructionDisable implements Listener {
    
    @EventHandler
    public void onExplosion(EntityExplodeEvent e) {
        e.setCancelled(true);
    }
}
