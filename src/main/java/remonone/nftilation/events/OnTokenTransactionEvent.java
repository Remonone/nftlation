package remonone.nftilation.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import remonone.nftilation.game.models.PlayerModel;

@Getter
public class OnTokenTransactionEvent extends Event implements Cancellable {

    private final TransactionType transactionType;
    @Setter
    private float tokensAmount;
    private final PlayerModel player;
    
    private boolean cancelled;
    
    private static final HandlerList handlers = new HandlerList();

    public OnTokenTransactionEvent(TransactionType transactionType, int tokensAmount, PlayerModel player) {
        this.transactionType = transactionType;
        this.tokensAmount = tokensAmount;
        this.player = player;
        this.cancelled = false;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public enum TransactionType {
        SPEND,
        GAIN,
        TRANSFER
    }
}
