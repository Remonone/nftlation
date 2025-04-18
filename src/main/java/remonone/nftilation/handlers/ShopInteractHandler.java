package remonone.nftilation.handlers;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import remonone.nftilation.Store;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.services.IPurchasableService;
import remonone.nftilation.game.ingame.services.ServiceContainer;
import remonone.nftilation.game.services.InventoryService;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.game.shop.content.CategoryElement;
import remonone.nftilation.game.shop.content.IShopElement;
import remonone.nftilation.game.shop.content.ItemElement;
import remonone.nftilation.game.shop.content.ServiceElement;
import remonone.nftilation.game.shop.registry.ShopItemRegistry;
import remonone.nftilation.utils.Logger;

public class ShopInteractHandler implements Listener {
    
    @EventHandler
    public void onShopInteract(final InventoryClickEvent e) {
        if(!Store.getInstance().getGameStage().getStage().equals(Stage.IN_GAME)) return;
        
        if(!e.getInventory().getName().startsWith(NameConstants.SHOP_TAB)) return;
        if(!(e.getInventory().getHolder() instanceof Player)) return;
        Player player = (Player) e.getInventory().getHolder();
        ItemStack item = e.getCurrentItem();
        if(item == null || item.getType().equals(Material.AIR) || item.getAmount() < 1) return;
        e.setCancelled(true);
        String id = NBT.get(item, (nbt) -> (String)nbt.getString("id"));
        if(StringUtils.isEmpty(id)) {
            Logger.error("Shop item have no id!");
            return;
        }
        IShopElement element = ShopItemRegistry.getItem(id);
        if(element == null) {
            Logger.error("Item with such id has not been found!");
            return;
        }
        if(element instanceof ItemElement) {
            ItemElement el = (ItemElement) element;
            HandleItemPurchase(player, el.getPurchasedItem(), el.getPrice());
        }
        if(element instanceof ServiceElement) {
            ServiceElement el = (ServiceElement) element;
            HandleServicePurchase(player, el.getServiceName(), el.getPrice());
        }
        if(element instanceof CategoryElement) {
            CategoryElement el = (CategoryElement) element;
            Inventory inventory = InventoryService.buildShopKeeperInventory(player, el);
            if(inventory == null) return;
            player.openInventory(inventory);
        }
    }

    private void HandleServicePurchase(Player player, String serviceName, int price) {
        IPurchasableService service = ServiceContainer.getService(serviceName);
        if(service == null) {
            Logger.error("Service name is incorrect! Provided: " + serviceName);
            return;
        }
        Float discount = (Float) RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_PRICE_SCALE, 1F);
        service.OnPurchase(player, price * discount);
    }

    private void HandleItemPurchase(Player player, ItemStack item, int price) {
        PlayerInteractComponent playerInteract = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        Float discount = (Float) RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_PRICE_SCALE, 1F);
        if(playerInteract == null) return;
        if (!playerInteract.adjustPlayerTokens(player, -price * discount, TransactionType.PURCHASE)) {
            player.sendMessage(ChatColor.RED + MessageConstant.NOT_ENOUGH_MONEY);
            return;
        }
        player.getInventory().addItem(item);
    }
}
