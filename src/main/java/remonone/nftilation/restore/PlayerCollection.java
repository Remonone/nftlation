package remonone.nftilation.restore;

import lombok.Builder;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

@Builder
public class PlayerCollection implements ConfigurationSerializable, Cloneable {

    private static final String TOKENS = "tokens";
    private static final String PARAMETERS = "parameters";
    private static final String LOCATION = "location";
    private static final String HEALTH = "health";

    private Map<String, Object> parameters;
    private Location location;
    private float tokens;
    private double currentHealth;

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put(TOKENS, tokens);
        result.put(PARAMETERS, parameters);
        result.put(LOCATION, location);
        result.put(HEALTH, currentHealth);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static PlayerCollection deserialize(Map<String, Object> parameters) {
        int tokens = 0;
        Map<String, Object> params = new HashMap<>();
        Location location = null;
        double currentHealth = 0;
        if (parameters.containsKey(TOKENS)) {
            tokens = (int) parameters.get(TOKENS);
        }
        if (parameters.containsKey(PARAMETERS)) {
            params = (Map<String, Object>) parameters.get(PARAMETERS);
        }
        if (parameters.containsKey(LOCATION)) {
            location = (Location) parameters.get(LOCATION);
        }
        if (parameters.containsKey(HEALTH)) {
            currentHealth = (double) parameters.get(HEALTH);
        }
        return builder()
                .currentHealth(currentHealth)
                .location(location)
                .tokens(tokens)
                .parameters(params)
                .build();
    }

    @Override
    public PlayerCollection clone() {
        try {
            PlayerCollection clone = (PlayerCollection) super.clone();
            clone.tokens = tokens;
            clone.currentHealth = currentHealth;
            clone.location = location.clone();
            clone.parameters = new HashMap<>(parameters);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
