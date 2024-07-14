package remonone.nftilation.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import remonone.nftilation.components.EntityHandleComponent;

import java.util.Arrays;

public class OnChunkUnloadHandler implements Listener {
    
    @EventHandler
    public void onChunkUnload(final ChunkUnloadEvent e) {
        boolean isChunkLocked = Arrays.stream(e.getChunk().getEntities()).anyMatch(EntityHandleComponent::isEntityLockedForUnload);
        if(isChunkLocked) {
            e.setCancelled(true);
        }
    }
}
