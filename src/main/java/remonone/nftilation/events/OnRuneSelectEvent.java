package remonone.nftilation.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import remonone.nftilation.game.runes.Rune;

@Getter
public class OnRuneSelectEvent extends Event {

    private final Rune rune;
    private final Player player;
    
    private static final HandlerList handlers = new HandlerList();
    
    public OnRuneSelectEvent(Rune rune, Player player) {
        this.rune = rune;
        this.player = player;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
