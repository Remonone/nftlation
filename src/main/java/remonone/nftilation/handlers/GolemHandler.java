package remonone.nftilation.handlers;

import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import remonone.nftilation.components.EntityHandleComponent;

public class GolemHandler implements Listener {
    
    @EventHandler
    public void onPlayerInteract(final EntityTargetEvent event) {
        if (event.getEntity() instanceof IronGolem) {
            if(!(event.getTarget() instanceof Player)) return;
            if(EntityHandleComponent.isEntityHostile(event.getEntity())) {
                Player player = (Player) event.getTarget();
                ((IronGolem) event.getEntity()).setTarget(player);
            }
        }
    }
}
