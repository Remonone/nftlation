package remonone.nftilation.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import remonone.nftilation.events.OnCounterTickEvent;
import remonone.nftilation.game.ingame.actions.ActionContainer;
import remonone.nftilation.game.ingame.actions.ActionType;

import java.util.Map;

@AllArgsConstructor
@Getter
public class GlobalEventHandler implements Listener {
    private final int activationSecond;
    private final int activationPhase;
    private final Map<String, Object> params;
    private final ActionType actionType;
    
    private boolean isActivated;
    
    @EventHandler
    public void checkOnTiming(final OnCounterTickEvent event) {
        if(isActivated) return;
        if(event.getPhase() + 1 != activationPhase) return;
        if(event.getTimeInSeconds() >= activationSecond) {
            isActivated = true;
            ActionContainer.InitAction(actionType, params);
            OnCounterTickEvent.getHandlerList().unregister(this);
        }
    }
}
