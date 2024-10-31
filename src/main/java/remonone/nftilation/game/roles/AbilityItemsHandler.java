package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import lombok.Builder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.InventoryUtils;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Map;

@Builder
public class AbilityItemsHandler implements Listener {

    private String container;
    private Role maintainRole;
    private Map<String, IAbilityHandler> handlerMap;

    @EventHandler
    public void onItemInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(model == null) return;
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        Role role = Role.getRoleByID((String)model.getParameters().get(PropertyConstant.PLAYER_ROLE_ID));
        if(!role.equals(maintainRole)) return;
        ItemStack stack = event.getItem();
        if(stack == null || stack.getType() == Material.AIR || stack.getAmount() < 1) return;
        if(InventoryUtils.isCooldownRemain(stack)) {
            InventoryUtils.notifyAboutCooldown(player, stack);
            return;
        }
        String usedItem = NBT.get(stack, (nbt) -> (String)nbt.getString(container));
        if(!handlerMap.containsKey(usedItem)) return;
        event.setCancelled(true);
        IAbilityHandler handler = handlerMap.get(usedItem);
        if(!handler.executeHandle(model)) return;
        InventoryUtils.setCooldownForItem(model, stack, handler.getCooldown(model));
    }
}
