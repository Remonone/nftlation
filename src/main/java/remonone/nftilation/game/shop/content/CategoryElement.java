package remonone.nftilation.game.shop.content;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@Setter
@Getter
@SerializableAs("CategoryElement")
public class CategoryElement implements IShopElement, IExpandable, ConfigurationSerializable {

    @Getter
    private final String id;
    private final Map<Integer, String> elements;
    private final ItemStack displayItem;

    public CategoryElement(String id, Material mat, String name, List<ShopItemPosition> elements) {
        this.elements = buildElements(elements);
        this.displayItem = new ItemStack(mat);
        ItemMeta meta = displayItem.getItemMeta();
        meta.setDisplayName(name);
        displayItem.setItemMeta(meta);
        this.id = id;
    }

    @Override
    public Map<Integer, String> getExpandableElements() {
        return elements;
    }

    @Override
    public ItemStack getDisplay() {
        return displayItem;
    }

    private Map<Integer, String> buildElements(List<ShopItemPosition> elements) {
        Map<Integer, String> elementsMap = new HashMap<>();
        for(ShopItemPosition element : elements) {
            elementsMap.put(element.getPosition(), element.getItemId());
        }
        return Collections.unmodifiableMap(elementsMap);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        List<ShopItemPosition> elements = new ArrayList<>();
        for(Map.Entry<Integer, String> pair: this.elements.entrySet()) {
            elements.add(new ShopItemPosition(pair.getKey(), pair.getValue()));
        }
        map.put("elements", elements);
        map.put("display_material", displayItem.getType());
        map.put("display_name", displayItem.getItemMeta().getDisplayName());
        return map;
    }
    
    @SuppressWarnings("unchecked")
    public static CategoryElement deserialize(Map<String, Object> map) {
        String id = (String) map.get("id");
        Material mat = Material.getMaterial((String)map.get("display_material"));
        String displayName = (String) map.get("display_name");
        List<ShopItemPosition> elements = new ArrayList<>();
        if(map.containsKey("elements")) {
            elements = (List<ShopItemPosition>) map.get("elements");
        }
        return new CategoryElement(id, mat, displayName, elements);
    }
    
}
