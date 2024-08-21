package remonone.nftilation.game.shop;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import remonone.nftilation.Nftilation;
import remonone.nftilation.game.shop.content.CategoryElement;
import remonone.nftilation.game.shop.content.IShopElement;
import remonone.nftilation.game.shop.content.ItemElement;
import remonone.nftilation.game.shop.content.ServiceElement;
import remonone.nftilation.game.shop.registry.ShopItemRegistry;
import remonone.nftilation.utils.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShopBuilder {

    @Getter
    private final static ShopBuilder instance = new ShopBuilder();

    private YamlConfiguration configuration;
    
    public void Load() {
        Logger.log("Loading shop info...");
        File file = new File(Nftilation.getInstance().getDataFolder(), "shop.yml");
        if(!file.exists()) {
            Nftilation.getInstance().saveResource("shop.yml", false);
        }

        configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch(Exception e) {
            e.printStackTrace();
            Logger.error("Error during loading a configurations: " + e.getMessage());
        }

        LoadData();
    }
    
    @SuppressWarnings("unchecked")
    private void LoadData() {
        List<ItemElement> itemElements = (List<ItemElement>) configuration.getList("itemElements");
        if(itemElements == null) {
            itemElements = new ArrayList<>();
        }
        for(ItemElement itemElement : itemElements) {
            ShopItemRegistry.addRegistry(itemElement);
        }
        List<ServiceElement> serviceElements = (List<ServiceElement>) configuration.getList("serviceElements");
        if(serviceElements == null) {
            serviceElements = new ArrayList<>();
        }
        for(ServiceElement serviceElement : serviceElements) {
            ShopItemRegistry.addRegistry(serviceElement);
        }
        List<CategoryElement> categoryElements = (List<CategoryElement>) configuration.getList("categoryElements");
        if(categoryElements == null) {
            categoryElements = new ArrayList<>();
        }
        for(CategoryElement categoryElement : categoryElements) {
            ShopItemRegistry.addRegistry(categoryElement);
            ShopItemRegistry.addCategoryRegistryByName(categoryElement.getExpandableName(), categoryElement);
        }
    }
    
    public CategoryElement getMainElement() {
        IShopElement root = ShopItemRegistry.getItem("main");
        if(!(root instanceof CategoryElement)) {
            Logger.error("Main item is not existing!");
            return null;
        }
        return (CategoryElement) root;
    }
}
