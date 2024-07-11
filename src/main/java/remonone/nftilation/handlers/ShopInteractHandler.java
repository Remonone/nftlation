package remonone.nftilation.handlers;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.services.IPurchasableService;
import remonone.nftilation.game.ingame.services.ServiceContainer;
import remonone.nftilation.game.inventory.InventoryBuilder;
import remonone.nftilation.utils.Logger;

public class ShopInteractHandler implements Listener {
    
    @EventHandler
    public void onShopInteract(final InventoryClickEvent e) {
        if(!Store.getInstance().getGameStage().getStage().equals(Stage.IN_GAME)) return;
        
        if(!e.getInventory().getName().equals(NameConstants.SHOP_TAB)) return;
        if(!(e.getInventory().getHolder() instanceof Player)) return;
        Player player = (Player) e.getInventory().getHolder();
        ItemStack item = e.getCurrentItem();
        if(item == null || item.getType().equals(Material.AIR) || item.getAmount() < 1) return;
        e.setCancelled(true);
        String productType = NBT.get(item, nbt -> (String) nbt.getString(PropertyConstant.NBT_PRODUCT_TYPE));
        if(productType == null || productType.isEmpty()) return;
        switch(productType) {
            case DataConstants.NBT_TYPE_CATEGORY:
                HandleCategorySelect(player, item);
                break;
            case DataConstants.NBT_TYPE_ITEM:
                HandleItemPurchase(player, item);
                break;
            case DataConstants.NBT_TYPE_SERVICE:
                HandleServicePurchase(player, item);
                break;
        }
    }

    private void HandleServicePurchase(Player player, ItemStack item) {
        String serviceName = NBT.get(item, nbt -> (String) nbt.getString(PropertyConstant.NBT_SERVICE_NAME));
        if(serviceName == null || serviceName.isEmpty()) return;
        IPurchasableService service = ServiceContainer.getService(serviceName);
        if(service == null) {
            Logger.error("Service name is incorrect! Provided: " + serviceName);
            return;
        }
        if (!tryWithdrawPrice(player, item)) return;
        int price = NBT.get(item, nbt -> (Integer) nbt.getInteger(PropertyConstant.NBT_PRICE));
        service.OnPurchase(player, price);
    }

    private void HandleItemPurchase(Player player, ItemStack item) {
        if (!tryWithdrawPrice(player, item)) return;
        ItemStack reward = new ItemStack(item.getType());
        if(item.getType() == Material.POTION) {
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            reward.setItemMeta(meta);
        }
        reward.setAmount(item.getAmount());
        player.getInventory().addItem(reward);
    }

    private boolean tryWithdrawPrice(Player player, ItemStack item) {
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getName());
        GameInstance.PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, player);
        if(model == null) return false;
        int price = NBT.get(item, nbt -> (Integer) nbt.getInteger(PropertyConstant.NBT_PRICE));
        if(model.getTokens() < price) {
            player.sendMessage(ChatColor.RED + MessageConstant.NOT_ENOUGH_MONEY);
            return false;
        }
        GameInstance.getInstance().withdrawFunds(team, player, price);
        return true;
    }

    private void HandleCategorySelect(Player player, ItemStack stack) {
        String categoryName = NBT.get(stack, nbt -> (String) nbt.getString(PropertyConstant.NBT_CATEGORY_NAME)); 
        if(categoryName == null || categoryName.isEmpty()) return;
        player.closeInventory();
        switch (categoryName) {
            case DataConstants.NBT_CATEGORY_POTIONS:
                player.openInventory(InventoryBuilder.getShopKeeperPotions(player));
                return;
            case DataConstants.NBT_CATEGORY_UPGRADES:
                player.openInventory(InventoryBuilder.getShopKeeperUpgrades(player));
                return;
            case DataConstants.NBT_CATEGORY_FOOD:
                player.openInventory(InventoryBuilder.getShopKeeperGoods(player));
        }
    }
}
