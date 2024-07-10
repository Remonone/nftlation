package remonone.nftilation.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import remonone.nftilation.application.models.PlayerData;

@Getter
public class PlayerLoginEvent extends Event {
    
    private final PlayerData playerData;
    private final Player player;
    
    public PlayerLoginEvent(PlayerData data, Player player) {
        this.playerData = data;
        this.player = player;
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
