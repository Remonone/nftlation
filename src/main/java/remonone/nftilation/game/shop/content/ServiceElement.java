package remonone.nftilation.game.shop.content;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ServiceElement implements IShopElement, IPurchasableAction {

    @Getter
    private String id;
    private final String serviceName;
    private final int price;

    private ItemStack displayItem;

    public ServiceElement(String id, Material mat, String name, String serviceName, int price) {
        this.serviceName = serviceName;
        this.price = price;
        this.id = id;
        this.displayItem = new ItemStack(mat);
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
        return null;
    }
}
