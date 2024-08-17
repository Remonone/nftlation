package remonone.nftilation.game.shop.content;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import remonone.nftilation.game.models.RequisiteContainer;

import java.util.*;

@Setter
@Getter
@SerializableAs("ItemElement")
public class ItemElement implements IShopElement, IPurchasableItem, ConfigurationSerializable {

    private static final String ITEM_ID = "id";
    private static final String ITEM_STACK = "itemStack";
    private static final String ITEM_PRICE = "itemPrice";
    private static final String ITEM_REQUISITES = "requisites";
    
    private String id;
    private final ItemStack element;
    private final boolean shouldCopyMeta;
    private final int price;
    @Getter
    private RequisiteContainer requisites;
    
    public ItemElement(String id, ItemStack item, int price, RequisiteContainer container) {
        this.id = id;
        this.element = item;
        this.price = price;
        this.shouldCopyMeta = true;
        this.requisites = container;
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
    public List<String> getDescription() {
        return Collections.emptyList();
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
        RequisiteContainer container;
        if (map.containsKey(ITEM_ID)) {
            id = (String) map.get(ITEM_ID);
        }
        if (map.containsKey(ITEM_STACK)) {
            itemStack = (ItemStack) map.get(ITEM_STACK);
        }
        if (map.containsKey(ITEM_PRICE)) {
            price = (int) map.get(ITEM_PRICE);
        }
        if(map.containsKey(ITEM_REQUISITES)) {
            container = (RequisiteContainer) map.get(ITEM_REQUISITES);
        } else {
            container = new RequisiteContainer(new ArrayList<>());
        }

        return new ItemElement(id, itemStack, price, container);
    }
}
