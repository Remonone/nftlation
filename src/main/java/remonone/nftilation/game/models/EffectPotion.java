package remonone.nftilation.game.models;

import lombok.Data;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import remonone.nftilation.constants.DataConstants;

import java.util.Collections;
import java.util.Map;

@Data
@SerializableAs("PotionEffect")
public class EffectPotion implements ConfigurationSerializable {
    
    private final String effect;
    private final int strength;
    private final int duration;
    
    @Override
    public Map<String, Object> serialize() {
        return Collections.emptyMap();
    }
    
    public static EffectPotion deserialize(Map<String, Object> args) {
        String effect = "";
        int strength = 0;
        int duration = Integer.MAX_VALUE;
        if(args.containsKey("effect")) {
            effect = args.get("effect").toString();
        }
        if (args.containsKey("strength")) {
            strength = Integer.parseInt(args.get("strength").toString());
        }
        if(args.containsKey("duration")) {
            duration = Integer.parseInt(args.get("duration").toString()) * DataConstants.TICKS_IN_SECOND;
        }
        return new EffectPotion(effect, strength, duration);
    }
}
