package remonone.nftilation.game.lobby;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.inventory.InventoryBuilder;
import remonone.nftilation.game.transfer.GameTransfer;
import remonone.nftilation.utils.InventoryUtils;


public class LobbyDisposer implements Listener {
    
    public void DisposePlayer(PlayerData data, Player player) {
        GameTransfer gameTransfer = Store.getInstance().getGameTransfer();
        if(data.getRole() == PlayerRole.PLAYER) {
            if(Store.getInstance().getGameStage().getStage() == Stage.LOBBY) {
                gameTransfer.MoveToLobby(player);
                player.getInventory().clear();
                InventoryUtils.giveRoleSelector(player);
            }
        } else {
            gameTransfer.MoveToAdminRoom(player, data);
        }
    }

    
    @EventHandler
    public void onItemInteract(PlayerInteractEvent event) {
        ItemStack stack = event.getItem();
        if(stack == null) return;
        ItemMeta meta = stack.getItemMeta();
        if(meta == null) return;
        if(!StringUtils.equals(meta.getDisplayName(), NameConstants.ROLE_SELECTOR)) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        PlayerData data = Store.getInstance().getDataInstance().FindPlayerByName(player.getName());
        if(data.getRole().equals(PlayerRole.ADMIN) || data.getRole().equals(PlayerRole.DEV)) return;
        String teamName = data.getTeam().getTeamName();
        if(teamName == null) return;
        Inventory inventory = InventoryBuilder.getRoleSelectionInventory(player, teamName);
        player.openInventory(inventory);
    }

    
}
