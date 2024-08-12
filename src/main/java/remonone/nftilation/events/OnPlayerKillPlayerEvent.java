package remonone.nftilation.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import remonone.nftilation.game.models.PlayerModel;

@Getter
public class OnPlayerKillPlayerEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final PlayerModel killer;
    private final PlayerModel victim;
    
    public OnPlayerKillPlayerEvent(PlayerModel killer, PlayerModel victim) {
        this.killer = killer;
        this.victim = victim;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
