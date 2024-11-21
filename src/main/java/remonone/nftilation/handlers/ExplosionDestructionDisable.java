package remonone.nftilation.handlers;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.util.Vector;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.constants.DataConstants;

import java.util.List;
import java.util.stream.Collectors;

public class ExplosionDestructionDisable implements Listener {
    
    @EventHandler
    public void onExplosion(EntityExplodeEvent e) {
        if(EntityHandleComponent.getEntityOwner(e.getEntity()) != null) {
            e.setCancelled(true);
            return;
        }
        
        Vector explosionPoint = e.getLocation().toVector();
        List<Location> cores = ConfigManager.getInstance().getTeamSpawnList().stream().map(TeamSpawnPoint::getCoreCenter).collect(Collectors.toList());
        Vector center = ConfigManager.getInstance().getCenterLocation().toVector();
        if(explosionPoint.isInSphere(center, 20)) {
            e.setCancelled(true);
            return;
        }
        for(Location core : cores) {
            if (explosionPoint.isInSphere(core.toVector(), DataConstants.INVULNERABILITY_RANGE)) {
                e.setCancelled(true);
                return;
            }
        }
        if(explosionPoint.isInSphere(ConfigManager.getInstance().getCenterLocation().toVector(), DataConstants.INVULNERABILITY_RANGE)) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPreExplosion(ExplosionPrimeEvent e) {
        if(!e.getEntity().getMetadata("meteor").isEmpty()) {
            e.setRadius((float)e.getEntity().getMetadata("meteor").get(0).value());
            e.setFire(false);
        }
    }
}
