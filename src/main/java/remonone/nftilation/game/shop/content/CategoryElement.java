package remonone.nftilation.game.shop.content;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import remonone.nftilation.game.shop.registry.ShopItemRegistry;

import java.util.*;

public class CategoryElement implements IShopElement, IExpandable, ConfigurationSerializable {

    @Getter
    private final String id;
    private final Map<Integer, IShopElement> elements;
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
    public Map<Integer, IShopElement> getExpandableElements() {
        return elements;
    }

    @Override
    public ItemStack getDisplay() {
        return displayItem;
    }

    private Map<Integer, IShopElement> buildElements(List<ShopItemPosition> elements) {
        Map<Integer, IShopElement> elementsMap = new HashMap<>();
        for(ShopItemPosition element : elements) {
            elementsMap.put(element.getPosition(), ShopItemRegistry.getItem(element.itemId));
        }
        return Collections.unmodifiableMap(elementsMap);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        List<ShopItemPosition> elements = new ArrayList<>();
        for(Map.Entry<Integer, IShopElement> pair: this.elements.entrySet()) {
            elements.add(new ShopItemPosition(pair.getKey(), pair.getValue().getId()));
        }
        map.put("elements", elements);
        map.put("display_material", displayItem.getType());
        map.put("display_name", displayItem.getItemMeta().getDisplayName());
        return map;
    }

    public static CategoryElement deserialize(Map<String, Object> map) {
        String id = (String) map.get("id");
        Material mat = (Material) map.get("display_material");
        String displayName = (String) map.get("display_name");
        List<ShopItemPosition> elements = new ArrayList<>();
        if(map.containsKey("elements")) {
            elements = (List<ShopItemPosition>) map.get("elements");
        }
        return new CategoryElement(id, mat, displayName, elements);
    }

    @SerializableAs("categoryItem")
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShopItemPosition implements ConfigurationSerializable {

        private static final String POSITION = "position";
        private static final String ITEM_ID = "itemId";
        @Getter
        private int position;
        @Getter
        private String itemId;

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<String, Object>();

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
}
