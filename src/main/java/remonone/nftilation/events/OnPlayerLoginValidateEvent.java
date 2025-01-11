package remonone.nftilation.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import remonone.nftilation.application.models.PlayerData;

@Getter
public class OnPlayerLoginValidateEvent extends Event {

    private final PlayerData playerData;
    private final Player player;

    public OnPlayerLoginValidateEvent(PlayerData playerData, Player player) {
        this.playerData = playerData;
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
