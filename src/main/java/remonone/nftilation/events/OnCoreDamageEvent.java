package remonone.nftilation.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import remonone.nftilation.game.models.PlayerModel;

@Getter
public class OnCoreDamageEvent extends Event implements Cancellable {
    
    public final String teamName;
    public final PlayerModel attacker;
    
    @Setter
    private boolean cancelled;
    
    public OnCoreDamageEvent(String attackedTeam, PlayerModel attacker) {
        this.teamName = attackedTeam;
        this.attacker = attacker;
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
