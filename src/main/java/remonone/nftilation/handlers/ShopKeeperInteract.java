package remonone.nftilation.handlers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import remonone.nftilation.game.services.InventoryService;
import remonone.nftilation.game.shop.ShopBuilder;


public class ShopKeeperInteract implements Listener {
    
    @EventHandler
    public void onShopKeeperInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        String name = entity.getCustomName();
        if("Shop keeper".equals(name)) {
            event.setCancelled(true);
            Inventory inventory = InventoryService.buildShopKeeperInventory(player, ShopBuilder.getInstance().getMainElement());
            if(inventory == null) return;
            player.openInventory(inventory);
        }
    }
}
