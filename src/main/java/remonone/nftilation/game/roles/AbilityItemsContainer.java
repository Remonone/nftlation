package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.InventoryUtils;
import remonone.nftilation.utils.PlayerUtils;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class AbilityItemsContainer implements Listener {
    private final Map<String, AbilityItemsHandler> abilityItems = new HashMap<>();
    
    public AbilityItemsContainer() {
        getServer().getPluginManager().registerEvents(this, Nftilation.getInstance());
    }
    
    public void registerAbilityHandler(Role role, final AbilityItemsHandler handler) {
        abilityItems.put(role.getRoleID(), handler);
    }

    @EventHandler
    public void onItemInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(model == null) return;
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        ItemStack itemStack = event.getItem();
        if(itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() < 1) return;
        String roleId = NBT.get(itemStack, nbt -> (String)nbt.getString(RoleConstant.ROLE));
        AbilityItemsHandler handler = abilityItems.get(roleId);
        Role role = Role.getRoleByID((String)model.getParameters().get(PropertyConstant.PLAYER_ROLE_ID));
        if(handler == null) return;
        if(!handler.getMaintainRole().checkForRoleAccess(role)) return;
        ItemStack stack = event.getItem();
        if(stack == null || stack.getType() == Material.AIR || stack.getAmount() < 1) return;
        if(InventoryUtils.isCooldownRemain(stack)) {
            InventoryUtils.notifyAboutCooldown(player, stack);
            return;
        }
        String usedItem = NBT.get(stack, (nbt) -> (String)nbt.getString(handler.getContainer()));
        if(!handler.getHandlerMap().containsKey(usedItem)) return;
        event.setCancelled(true);
        IAbilityHandler abilityHandler = handler.getHandlerMap().get(usedItem);
        if(!abilityHandler.executeHandle(model)) return;
        InventoryUtils.setCooldownForItem(model, stack, abilityHandler.getCooldown(model));
    }
}
