package remonone.nftilation.game.shop.content;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class ItemElement implements IShopElement, IPurchasable {

    @Getter
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

    @Override
    public int getPrice() {
        return price;
    }

    public ItemStack getPurchasedItem() {
        ItemStack copyElement = new ItemStack(element.getType());
        copyElement.setAmount(element.getAmount());
        if(shouldCopyMeta) {
            ItemMeta meta = copyElement.getItemMeta();
            meta.setDisplayName(element.getItemMeta().getDisplayName());
            copyElement.setItemMeta(meta);
        }
        return copyElement;
    }

    @Override
    public ItemStack getDisplay() {
        return element.clone();
    }
}
