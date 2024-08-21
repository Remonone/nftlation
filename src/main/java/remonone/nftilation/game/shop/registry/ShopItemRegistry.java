package remonone.nftilation.game.shop.registry;

import remonone.nftilation.game.shop.content.IExpandable;
import remonone.nftilation.game.shop.content.IShopElement;
import remonone.nftilation.utils.Logger;

import java.util.HashMap;

public class ShopItemRegistry {
    private final static HashMap<String, IShopElement> itemRegistry = new HashMap<>();
    private final static HashMap<String, IExpandable> expandableRegistry = new HashMap<>();

    public static void addRegistry(IShopElement item) {
        if(item == null) {
            Logger.error("Item is not belongs to shop item or null!");
            return;
        }
        if(isItemContains(item.getId())) {
            Logger.error("Trying to add an item that already exists! Id: " + item.getId());
            return;
        }
        itemRegistry.put(item.getId(), item);
        Logger.log("Successfully added category with id "+item.getId()+" to registry!");
    }

    public static void addCategoryRegistryByName(String categoryName, IExpandable ex) {
        if(categoryName == null) {
            Logger.error("Category name is null!");
            return;
        }
        if(ex == null) {
            Logger.error("Expandable ex is null!");
            return;
        }
        if(expandableRegistry.containsKey(categoryName)) {
            Logger.error("Trying to add an expandable that already exists! Id: " + categoryName);
            return;
        }
        expandableRegistry.put(categoryName, ex);
    }

    public static boolean isItemContains(String categoryId) {
        return itemRegistry.containsKey(categoryId);
    }

    public static boolean isExpandableContains(String categoryId) {
        return expandableRegistry.containsKey(categoryId);
    }

    public static IShopElement getItem(String id) {
        if(!isItemContains(id)) {
            return null;
        }
        return itemRegistry.get(id);
    }

    public static IExpandable getExpandable(String id) {
        if(!isExpandableContains(id)) {
            return null;
        }
        return expandableRegistry.get(id);
    }

    public static void clearRegister() {
        itemRegistry.clear();
    }
}
