package remonone.nftilation.game.models;

import lombok.Data;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Collections;
import java.util.Map;

@Data
@SerializableAs("Attribute")
public class AttributeModifier implements ConfigurationSerializable {
    private String attributeName;
    private Object attributeValue;

    @Override
    public Map<String, Object> serialize() {
        return Collections.emptyMap();
    }
    
    public static AttributeModifier deserialize(Map<String, Object> map) {
        AttributeModifier modifier = new AttributeModifier();
        if(map.containsKey("name")) {
            modifier.setAttributeName((String) map.get("name"));
        }
        if(map.containsKey("value")) {
            modifier.setAttributeValue(map.get("value"));
        }
        return modifier;
    }
}
