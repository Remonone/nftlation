package remonone.nftilation.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class OnCounterTickEvent extends Event {
    
    private final int timeInSeconds;
    private final int phase;
    
    public OnCounterTickEvent(final int timeInSeconds, final int phase) {
        this.timeInSeconds = timeInSeconds;
        this.phase = phase;
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
