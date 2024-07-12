package remonone.nftilation.game.shop.content;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import remonone.nftilation.utils.Logger;

import java.util.HashMap;
import java.util.Map;
@Setter
@Getter
@SerializableAs("ItemElement")
public class ItemElement implements IShopElement, IPurchasableItem, ConfigurationSerializable {

    private static final String ITEM_ID = "id";
    private static final String ITEM_STACK = "itemStack";
    private static final String ITEM_PRICE = "itemPrice";
    
    private String id;
    private final ItemStack element;
    private final boolean shouldCopyMeta;
    private final int price;

    public ItemElement(Material material, String displayName, int amount, int price, boolean copyMeta, String id) {
        this.element = new ItemStack(material, amount);
        ItemMeta meta = this.element.getItemMeta();
        meta.setDisplayName(displayName);
        this.element.setItemMeta(meta);
        this.shouldCopyMeta = copyMeta;
        this.price = price;
        this.id = id;
    }
    
    public ItemElement(String id, ItemStack item, int price) {
        this.id = id;
        this.element = item;
        this.price = price;
        this.shouldCopyMeta = true;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public ItemStack getPurchasedItem() {
        ItemStack copyElement = new ItemStack(element.getType());
        copyElement.setAmount(element.getAmount());
        if(shouldCopyMeta) {
            copyElement.setItemMeta(element.getItemMeta());
        }
        return copyElement;
    }

    @Override
    public ItemStack getDisplay() {
        return element.clone();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(ITEM_ID, this.id);
        map.put(ITEM_STACK, this.element);
        map.put(ITEM_PRICE, this.price);
        return map;
    }
    
    public static ItemElement deserialize(Map<String, Object> map) {
        String id = "";
        ItemStack itemStack = new ItemStack(Material.AIR);
        int price = 0;
        if (map.containsKey(ITEM_ID)) {
            id = (String) map.get(ITEM_ID);
        }
        if (map.containsKey(ITEM_STACK)) {
            itemStack = (ItemStack) map.get(ITEM_STACK);
        }
        if (map.containsKey(ITEM_PRICE)) {
            price = (int) map.get(ITEM_PRICE);
        }

        return new ItemElement(id, itemStack, price);
    }
}
