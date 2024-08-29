package remonone.nftilation.game.models;

import lombok.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

@Data
public class PlayerModel {
    @Setter
    private Player reference;
    @Setter
    private float tokens;
    private final Map<String, Object> parameters;
    private final PriorityQueue<IDamageHandler> damageHandlers = new PriorityQueue<>();
    private final PriorityQueue<IDamageInvoker> damageInvokers = new PriorityQueue<>();
    
    public PlayerModel(Player reference, float tokens, Map<String, Object> params) {
        this.reference = reference;
        this.tokens = tokens;
        this.parameters = params;

    }
}