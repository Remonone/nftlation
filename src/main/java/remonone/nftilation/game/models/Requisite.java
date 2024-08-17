package remonone.nftilation.game.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Collections;
import java.util.Map;

@Getter
@AllArgsConstructor
@Data
@SerializableAs("Requisite")
public class Requisite implements ConfigurationSerializable {
    private String name;
    private Object value;

    public boolean isRequisiteFulfilled(Object value) {
        if(value == null) return false;
        if(value.equals("*")) {
            return true;
        }
        return this.value.equals(value);
    }

    @Override
    public Map<String, Object> serialize() {
        return Collections.emptyMap();
    }
    
    public static Requisite deserialize(Map<String, Object> map) {
        String name = "";
        Object value = null;
        if(map.containsKey("name")) {
            name = (String) map.get("name");
        }
        if(map.containsKey("value")) {
            value = map.get("value");
        }
        return new Requisite(name, value);
    }
}
