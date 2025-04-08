package remonone.nftilation.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class OnCoreHealEvent extends Event implements Cancellable {
    private final String healedTeam;
    @Setter
    private boolean cancelled;

    public OnCoreHealEvent(final String healedTeam) {
        this.healedTeam = healedTeam;
    }
    
    public static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
