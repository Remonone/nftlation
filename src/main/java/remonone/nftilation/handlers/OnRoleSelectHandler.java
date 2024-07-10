package remonone.nftilation.handlers;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.events.OnRoleSelectEvent;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.utils.Logger;

import static org.bukkit.Bukkit.getServer;

public class OnRoleSelectHandler implements Listener {
    
    @EventHandler
    public void onItemInteract(final InventoryClickEvent event) {
        ItemStack itemStack = event.getCurrentItem();
        if(itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() < 1) return;
        
        String roleName = NBT.get(itemStack, nbt -> (String) nbt.getString(PropertyConstant.NBT_ROLE));
        if(StringUtils.isEmpty(roleName)) return;
        LivingEntity entity = event.getWhoClicked();
        if(!(entity instanceof Player)) return;
        event.setCancelled(true);
        Player player = (Player) entity;
        boolean reserved = NBT.get(itemStack, nbt -> (Boolean) nbt.getBoolean(PropertyConstant.NBT_ROLE_RESERVED));
        if(reserved) {
            player.sendMessage(MessageConstant.ROLE_RESERVED);
            return;
        }
        Role role = Role.getRoleByID(roleName);
        if(role == null) {
            Logger.error("Role was not found! Inserted: " + roleName);
            return;
        }
        getServer().getPluginManager().callEvent(new OnRoleSelectEvent(role, player));
    }
}
