package remonone.nftilation.handlers;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.events.OnTokenTransactionEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.PlayerUtils;

public class MoneyRainPickupHandler implements Listener {

    @EventHandler
    public void onMoneyPickup(final EntityPickupItemEvent e) {
        if(!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        Item item = e.getItem();
        ItemStack stack = item.getItemStack();
        if(stack == null || !stack.getType().equals(Material.GOLD_NUGGET) || stack.getAmount() < 1) return;
        Float moneyChip = NBT.get(stack, (nbt) -> (Float)nbt.getFloat("money-rain"));
        if(moneyChip == null) return;
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(model == null) {
            e.setCancelled(true);
            return;
        }
        PlayerInteractComponent component = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        if(component == null) return;
        int amount = stack.getAmount();
        component.adjustPlayerTokens(player, moneyChip * amount, OnTokenTransactionEvent.TransactionType.RESOURCE_GAIN);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 2f);
        e.setCancelled(true);
        item.remove();
    }
}
