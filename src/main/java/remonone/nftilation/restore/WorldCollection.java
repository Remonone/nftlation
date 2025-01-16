package remonone.nftilation.restore;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

@Builder
@Getter
public class WorldCollection implements ConfigurationSerializable, Cloneable {

    private static String SECONDS = "seconds";
    private static String PHASE = "phase";

    private int seconds;
    private int phase;
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(SECONDS, seconds);
        map.put(PHASE, phase);
        return map;
    }
    
    public static WorldCollection deserialize(Map<String, Object> parameters) {
        int seconds = 0;
        int phase = 0;
        if (parameters.containsKey(SECONDS)) {
            seconds = Integer.parseInt(parameters.get(SECONDS).toString());
        }
        if (parameters.containsKey(PHASE)) {
            phase = Integer.parseInt(parameters.get(PHASE).toString());
        }
        return new WorldCollection(seconds, phase);
    }

    @Override
    public WorldCollection clone() {
        try {
            return (WorldCollection) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
