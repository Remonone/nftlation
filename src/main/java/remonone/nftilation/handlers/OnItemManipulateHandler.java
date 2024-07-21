package remonone.nftilation.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.components.ItemStatModifierComponent;
import remonone.nftilation.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class OnItemManipulateHandler implements Listener {
    
    private final List<InventoryType> bannedTypes = new ArrayList<InventoryType>() {{
        add(InventoryType.HOPPER);
        add(InventoryType.CHEST);
        add(InventoryType.FURNACE);
        add(InventoryType.DISPENSER);
        add(InventoryType.DROPPER);
        add(InventoryType.WORKBENCH);
        add(InventoryType.BEACON);
    }};
    
    @EventHandler
    public void onDropItem(final PlayerDropItemEvent e) {
        ItemStack stack = e.getItemDrop().getItemStack();
        if(ItemStatModifierComponent.checkItemIfUndroppable(stack)) {
            e.setCancelled(true);
            if(ItemStatModifierComponent.checkItemIfDefault(stack)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        e.getPlayer().getInventory().removeItem(stack);
                    }
                }.runTaskLater(Nftilation.getInstance(), 2);
            }
        }
    }
    
    @EventHandler
    public void onStoreItem(final InventoryClickEvent e) {
        if(e.getClick().equals(ClickType.NUMBER_KEY) && e.getView().getBottomInventory().getType().equals(InventoryType.PLAYER)) {
            int itemPos = e.getHotbarButton();
            ItemStack itemStack = e.getView().getBottomInventory().getItem(itemPos);
            if(ItemStatModifierComponent.checkItemIfUnstorable(itemStack) && bannedTypes.contains(e.getView().getTopInventory().getType())) {
                e.setCancelled(true);
                return;
            }
        }
        if(!ItemStatModifierComponent.checkItemIfUnstorable(e.getCurrentItem())) return;
        Inventory top = e.getView().getTopInventory();
        Inventory bottom = e.getView().getBottomInventory();
        
        if(bottom.getType().equals(InventoryType.PLAYER) && bannedTypes.contains(top.getType())) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryMove(final InventoryMoveItemEvent e) {
        Logger.log(e.getItem().toString());
    }
    
    @EventHandler
    public void onCraftItem(final CraftItemEvent e) {
        for(ItemStack itemStack : e.getInventory().getMatrix()) {
            if(itemStack != null && ItemStatModifierComponent.checkItemIfUncraftable(itemStack)) {
                e.setCancelled(true);
                return;
            }
        }
    }
}
