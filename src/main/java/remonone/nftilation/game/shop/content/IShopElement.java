package remonone.nftilation.game.shop.content;

import org.bukkit.inventory.ItemStack;
import remonone.nftilation.game.models.RequisiteContainer;

import java.util.List;

public interface IShopElement {
    String getId();
    ItemStack getDisplay();
    List<String> getDescription();
    RequisiteContainer getRequisites();
}
