package remonone.nftilation.game.shop.content;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SerializableAs("ShopItemPosition")
public class ShopItemPosition implements ConfigurationSerializable {

    private static final String POSITION = "position";
    private static final String ITEM_ID = "itemId";

    private int position;
    private String itemId;

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        map.put(POSITION, position);
        map.put(ITEM_ID, itemId);
        return map;
    }

    public static ShopItemPosition deserialize(Map<String, Object> map) {
        ShopItemPosition position = new ShopItemPosition();
        position.position = Integer.parseInt(map.get(POSITION).toString());
        position.itemId = map.get(ITEM_ID).toString();
        return position;
    }
}
