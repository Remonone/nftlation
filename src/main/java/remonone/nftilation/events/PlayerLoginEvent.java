package remonone.nftilation.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import remonone.nftilation.application.models.PlayerData;

@Getter
public class PlayerLoginEvent extends Event implements Cancellable {
    
    private final PlayerData playerData;
    private final Player player;
    private boolean isCancelled;
    
    public PlayerLoginEvent(PlayerData data, Player player) {
        this.playerData = data;
        this.player = player;
        this.isCancelled = false;
    }
    
    public static final HandlerList handlers = new HandlerList();
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }
}
