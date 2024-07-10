package remonone.nftilation.handlers;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class OnPlayerDropItemHandler implements Listener {
    
    @EventHandler
    public void onDropItem(final PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        ItemStack stack = e.getItemDrop().getItemStack();
        
        String owner = NBT.get(stack, nbt -> (String) nbt.getString("owner"));
        if(StringUtils.isEmpty(owner)) return;
        if(player.getUniqueId().toString().equals(owner)) {
            e.setCancelled(true);
        }
    }
}
