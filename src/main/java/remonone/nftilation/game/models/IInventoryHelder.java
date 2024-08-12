package remonone.nftilation.game.models;

import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public interface IInventoryHelder {
    String getName();
    Material getMaterial();
    List<String> getDescription();
    int getIndex();
    Map<String, Object> getMeta();
}
