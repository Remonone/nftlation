package remonone.nftilation.game.models;

import lombok.*;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.PriorityQueue;

@Data
public class PlayerModel {
    @Setter
    private Player reference;
    @Setter
    private int tokens;
    private final Map<String, Object> parameters;
    private final PriorityQueue<IDamageHandler> damageHandlers = new PriorityQueue<>();
    private final PriorityQueue<IDamageInvoker> damageInvokers = new PriorityQueue<>();
    
    public PlayerModel(Player reference, int tokens, Map<String, Object> params) {
        this.reference = reference;
        this.tokens = tokens;
        this.parameters = params;

    }
}