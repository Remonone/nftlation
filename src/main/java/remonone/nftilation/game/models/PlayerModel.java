package remonone.nftilation.game.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.entity.Player;

import java.util.Map;

@Getter
@AllArgsConstructor
@ToString
public class PlayerModel {
    @Setter
    private Player reference;
    @Setter
    private int tokens;
    @Getter
    private Map<String, Object> parameters;
}