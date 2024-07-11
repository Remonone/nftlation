package remonone.nftilation.handlers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.components.OwnerHandleComponent;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;

import java.util.List;
import java.util.stream.Collectors;

public class ExplosionDestructionDisable implements Listener {
    
    @EventHandler
    public void onExplosion(EntityExplodeEvent e) {
        if(OwnerHandleComponent.getEntityOwner(e.getEntity()) != null) {
            e.setCancelled(true);
            return;
        }
        float radius = e.getYield() + 1;
        
        Vector explosionPoint = e.getLocation().toVector();
        List<Vector> cores = ConfigManager.getInstance().getTeamSpawnList().stream().map(TeamSpawnPoint::getCoreCenter).collect(Collectors.toList());
        for(Vector core : cores) {
            if (explosionPoint.isInSphere(core, radius)) {
                BukkitRunnable task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        Location coreCenter = new Location(e.getLocation().getWorld(), core.getX(), core.getY(), core.getZ());
                        coreCenter.getBlock().setType(Material.BEACON);
                    }
                };
                task.runTaskLater(Nftilation.getInstance(), 4);
            }
        } 
    }
}
