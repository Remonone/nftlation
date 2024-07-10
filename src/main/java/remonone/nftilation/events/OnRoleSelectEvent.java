package remonone.nftilation.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import remonone.nftilation.game.roles.Role;

@Getter
public class OnRoleSelectEvent extends Event {

    private final Role role;
    private final Player player;

    public OnRoleSelectEvent(Role data, Player player) {
        this.role = data;
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
