package remonone.nftilation.game.shop.registry;

import remonone.nftilation.game.shop.content.IExpandable;
import remonone.nftilation.game.shop.content.IShopElement;
import remonone.nftilation.utils.Logger;

import java.util.HashMap;

public class ShopItemRegistry {
    private final static HashMap<String, IShopElement> itemRegistry = new HashMap<>();

    public static void addRegistry(IShopElement item) {
        if(item == null) {
            Logger.error("Item is not belongs to shop item or null!");
            return;
        }
        itemRegistry.put(item.getId(), item);
        Logger.log("Successfully added category with id "+item.getId()+" to registry!");
    }

    public static boolean isItemContains(String categoryId) {
        return itemRegistry.containsKey(categoryId);
    }

    public static IShopElement getItem(String id) {
        if(!isItemContains(id)) {
            return null;
        }
        return itemRegistry.get(id);
    }
}
