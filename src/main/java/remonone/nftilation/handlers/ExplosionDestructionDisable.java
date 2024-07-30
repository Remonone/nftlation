package remonone.nftilation.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.util.Vector;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.RoleConstant;

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
        List<Vector> cores = ConfigManager.getInstance().getTeamSpawnList().stream().map(TeamSpawnPoint::getCoreCenter).collect(Collectors.toList());
        for(Vector core : cores) {
            if (explosionPoint.isInSphere(core, DataConstants.INVULNERABILITY_RANGE)) {
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
        if(!e.getEntity().getMetadata("cryptomarine").isEmpty()) {
            e.setRadius(RoleConstant.CRYPTOMARINE_EXPLOSION_STRENGTH);
            e.setFire(false);
        }
        if(!e.getEntity().getMetadata("sybil_attacker").isEmpty()) {
            e.setRadius(RoleConstant.SYBIL_EXPLOSION_ARROW_STRENGTH);
            e.setFire(false);
        }
    }
}
