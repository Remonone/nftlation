package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import remonone.nftilation.constants.RoleConstant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Watcher extends Role{
    @Override
    public String getRoleID() {
        return "WA";
    }

    public Watcher() {
        super("WA");
    }

    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> playerParams) {
        // Teleport
        // Soul suppression
        // Wind gust
        ItemStack teleport = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = teleport.getItemMeta();
        meta.setDisplayName("Teleport");
        teleport.setItemMeta(meta);
        NBT.modify(teleport, (nbt) -> {
            nbt.setString(RoleConstant.WATCHER_NBT_CONTAINER, "Teleport");
        });
        ItemStack soulSuppression = new ItemStack(Material.STRING);
        ItemMeta soulSuppressionMeta = soulSuppression.getItemMeta();
        soulSuppressionMeta.setDisplayName("Suppression");
        soulSuppression.setItemMeta(soulSuppressionMeta);
        ItemStack windGust = new ItemStack(Material.FEATHER);
        ItemMeta windGustMeta = windGust.getItemMeta();
        windGustMeta.setDisplayName("Gust");
        windGust.setItemMeta(windGustMeta);
        return Arrays.asList(teleport, soulSuppression, windGust);
    }


}
