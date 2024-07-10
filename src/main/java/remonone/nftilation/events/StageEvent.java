package remonone.nftilation.events;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import remonone.nftilation.enums.Stage;

@Getter
public class StageEvent extends Event implements Cancellable {
    private final Stage oldStage;
    private final Stage newStage;
    
    private final World world;
    
    private static final HandlerList handlers = new HandlerList();
    
    public StageEvent(Stage oldStage, Stage newStage, World world) {
        this.oldStage = oldStage;
        this.newStage = newStage;
        this.world = world;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancel) {

    }
}
