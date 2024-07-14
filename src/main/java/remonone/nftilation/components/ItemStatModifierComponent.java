package remonone.nftilation.components;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStatModifierComponent {

    private static final String UNDROPPABLE_ITEM = "undroppable";
    private static final String UNSTORABLE_ITEM = "unstorable";
    private static final String UNCRAFTABLE_ITEM = "uncraftable";
    
    
    public static void markItemAsUndroppable(ItemStack stack) {
        if(isItemNotSatisfy(stack)) return;
        NBT.modify(stack, nbt -> {nbt.setBoolean(UNDROPPABLE_ITEM, true);});
    }

    public static void markItemAsUnstorable(ItemStack stack) {
        if(isItemNotSatisfy(stack)) return;
        NBT.modify(stack, nbt -> {nbt.setBoolean(UNSTORABLE_ITEM, true);});
    }
    
    public static void markItemAsUncraftable(ItemStack stack) {
        if(isItemNotSatisfy(stack)) return;
        NBT.modify(stack, nbt -> {nbt.setBoolean(UNCRAFTABLE_ITEM, true);});
    }
    
    public static boolean checkItemIfUndroppable(ItemStack stack) {
        if(isItemNotSatisfy(stack)) return false;
        Boolean value = NBT.get(stack, nbt -> (Boolean)nbt.getBoolean(UNDROPPABLE_ITEM));
        return value != null ? value : false;
    }

    public static boolean checkItemIfUnstorable(ItemStack stack) {
        if(isItemNotSatisfy(stack)) return false;
        Boolean value = NBT.get(stack, nbt -> (Boolean)nbt.getBoolean(UNSTORABLE_ITEM));
        return value != null ? value : false;
    }
    
    public static boolean checkItemIfUncraftable(ItemStack stack) {
        if(isItemNotSatisfy(stack)) return false;
        Boolean value = NBT.get(stack, nbt -> (Boolean)nbt.getBoolean(UNCRAFTABLE_ITEM));
        return value != null ? value : false;
    }
    
    private static boolean isItemNotSatisfy(ItemStack itemStack) {
        return itemStack == null || itemStack.getAmount() < 1 || itemStack.getType() == Material.AIR;
    }
}
