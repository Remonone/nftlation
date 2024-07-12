package remonone.nftilation.game.shop.content;

import org.bukkit.inventory.ItemStack;

public interface IPurchasableItem extends IPurchasable {
    ItemStack getPurchasedItem();
}
