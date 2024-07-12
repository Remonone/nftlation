package remonone.nftilation.game.shop.content;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("ServiceElement")
public class ServiceElement implements IShopElement, IPurchasableAction, ConfigurationSerializable {

    private final static String SERVICE_ID = "id";
    private final static String SERVICE_NAME = "name";
    private final static String SERVICE_DISPLAY = "serviceDisplay";
    private final static String SERVICE_PRICE = "servicePrice";
    
    @Getter
    private final String id;
    private final String serviceName;
    private final int price;

    private final ItemStack displayItem;

    public ServiceElement(String id, Material mat, String name, String serviceName, int price) {
        this.serviceName = serviceName;
        this.price = price;
        this.id = id;
        this.displayItem = new ItemStack(mat);
        ItemMeta meta = displayItem.getItemMeta();
        meta.setDisplayName(name);
        displayItem.setItemMeta(meta);
    }
    public ServiceElement(String id, ItemStack displayItem, String serviceName, int price) {
        this.serviceName = serviceName;
        this.price = price;
        this.id = id;
        this.displayItem = displayItem;
    }

    @Override
    public String getServiceName() {
        return this.serviceName;
    }

    @Override
    public int getPrice() {
        return this.price;
    }

    @Override
    public ItemStack getDisplay() {
        return displayItem;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(SERVICE_ID, this.id);
        map.put(SERVICE_NAME, this.serviceName);
        map.put(SERVICE_DISPLAY, this.displayItem.serialize());
        return map;
    }

    public static ServiceElement deserialize(Map<String, Object> map) {
        String id = "";
        String serviceName = "";
        ItemStack itemStack = new ItemStack(Material.AIR);
        int price = 0;

        if (map.containsKey(SERVICE_ID)) {
            id = (String) map.get(SERVICE_ID);
        }
        if (map.containsKey(SERVICE_NAME)) {
            serviceName = (String) map.get(SERVICE_NAME);
        }
        if (map.containsKey(SERVICE_DISPLAY)) {
            itemStack = (ItemStack) map.get(SERVICE_DISPLAY);
        }
        if (map.containsKey(SERVICE_PRICE)) {
            price = (int) map.get(SERVICE_PRICE);
        }

        return new ServiceElement(id, itemStack, serviceName, price);
    }
}
