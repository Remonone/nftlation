package remonone.nftilation.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import remonone.nftilation.game.models.PlayerModel;

@Getter
public class OnCooldownApplyEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final PlayerModel model;
    @Setter
    private float cooldown;
    
    public OnCooldownApplyEvent(PlayerModel model, float cooldown) {
        this.model = model;
        this.cooldown = cooldown;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}
