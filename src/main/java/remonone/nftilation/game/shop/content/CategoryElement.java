package remonone.nftilation.game.shop.content;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import remonone.nftilation.game.models.RequisiteContainer;

import java.util.*;

@Setter
@Getter
@SerializableAs("CategoryElement")
public class CategoryElement implements IShopElement, IExpandable, ConfigurationSerializable {

    @Getter
    private final String id;
    private final List<String> elements;
    private final ItemStack displayItem;
    @Getter
    private final RequisiteContainer requisites;

    public CategoryElement(String id, Material mat, String name, List<String> elements, RequisiteContainer requisites) {
        this.elements = elements;
        this.displayItem = new ItemStack(mat);
        ItemMeta meta = displayItem.getItemMeta();
        meta.setDisplayName(name);
        displayItem.setItemMeta(meta);
        this.id = id;
        this.requisites = requisites;
    }

    @Override
    public List<String> getExpandableElements() {
        return elements;
    }

    @Override
    public ItemStack getDisplay() {
        return displayItem;
    }

    @Override
    public List<String> getDescription() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> serialize() {
        return Collections.emptyMap();
    }
    
    @SuppressWarnings("unchecked")
    public static CategoryElement deserialize(Map<String, Object> map) {
        String id = (String) map.get("id");
        Material mat = Material.getMaterial((String)map.get("display_material"));
        String displayName = (String) map.get("display_name");
        List<String> elements = new ArrayList<>();
        RequisiteContainer container;
        if(map.containsKey("elements")) {
            elements = (List<String>) map.get("elements");
        }
        if(map.containsKey("requisites")) {
            container = (RequisiteContainer) map.get("requisites");
        } else {
            container = new RequisiteContainer(new ArrayList<>());
        }
        return new CategoryElement(id, mat, displayName, elements, container);
    }
    
}
