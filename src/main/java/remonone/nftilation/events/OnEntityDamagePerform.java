package remonone.nftilation.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import remonone.nftilation.game.damage.DamageReport;

@Getter
public class OnEntityDamagePerform extends Event implements Cancellable {
    
    private final DamageReport incomingReport;
    @Setter
    private boolean cancelled;
    
    public static final HandlerList handlers = new HandlerList();

    public OnEntityDamagePerform(DamageReport incomingReport) {
        this.incomingReport = incomingReport;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
