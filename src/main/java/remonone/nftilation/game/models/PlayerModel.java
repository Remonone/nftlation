package remonone.nftilation.game.models;

import lombok.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class PlayerModel {
    @Setter
    private Player reference;
    @Setter
    private int tokens;
    private final Map<String, Object> parameters;
    private final List<IDamageHandler> damageHandlers = new ArrayList<>();
    private final List<IDamageInvoker> damageInvokers = new ArrayList<>();
    
    public PlayerModel(Player reference, int tokens, Map<String, Object> params) {
        this.reference = reference;
        this.tokens = tokens;
        this.parameters = params;
    }
}