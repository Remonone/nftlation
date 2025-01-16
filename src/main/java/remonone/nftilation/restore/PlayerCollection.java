package remonone.nftilation.restore;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class PlayerCollection implements ConfigurationSerializable, Cloneable {

    private static final String TOKENS = "tokens";
    private static final String PARAMETERS = "parameters";
    private static final String LOCATION = "location";
    private static final String HEALTH = "health";
    private static final String FOOD = "food";
    private static final String LOGIN = "login";
    private static final String INVENTORY = "inventory";

    private Map<String, Object> parameters;
    private Location location;
    private float tokens;
    private double currentHealth;
    private int currentFoodLevel;
    private String login;
    private ItemStack[] inventory;
    

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put(LOGIN, login);
        result.put(TOKENS, tokens);
        result.put(PARAMETERS, parameters);
        result.put(LOCATION, location);
        result.put(HEALTH, currentHealth);
        result.put(INVENTORY, inventory);
        result.put(FOOD, currentFoodLevel);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static PlayerCollection deserialize(Map<String, Object> parameters) {
        double tokens = 0;
        Map<String, Object> params = new HashMap<>();
        Location location = null;
        String login = "";
        int foodLevel = 0;
        double currentHealth = 0;
        ItemStack[] inventory = new ItemStack[0];
        if (parameters.containsKey(LOGIN)) {
            login = (String) parameters.get(LOGIN);
        }
        if (parameters.containsKey(TOKENS)) {
            tokens = (double) parameters.get(TOKENS);
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
        if(parameters.containsKey(INVENTORY)) {
            inventory = ((List<ItemStack>)parameters.get(INVENTORY)).toArray(new ItemStack[0]);
        }
        if(parameters.containsKey(FOOD)) {
            foodLevel = (int) parameters.get(FOOD);
        }
        return builder()
                .login(login)
                .currentHealth(currentHealth)
                .location(location)
                .tokens(((Double)tokens).floatValue())
                .parameters(params)
                .inventory(inventory)
                .currentFoodLevel(foodLevel)
                .build();
    }

    @Override
    public PlayerCollection clone() {
        try {
            PlayerCollection clone = (PlayerCollection) super.clone();
            clone.tokens = tokens;
            clone.inventory = inventory.clone();
            clone.login = login;
            clone.currentHealth = currentHealth;
            clone.location = location.clone();
            clone.parameters = new HashMap<>(parameters);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
