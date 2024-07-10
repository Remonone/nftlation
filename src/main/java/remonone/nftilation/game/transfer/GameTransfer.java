package remonone.nftilation.game.transfer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.utils.Logger;


public class GameTransfer {
    
    public void MoveToAdminRoom(Player player, PlayerData data) {
        if(data.getRole() == PlayerRole.PLAYER) {
            Logger.warn("Trying to transfer player to admin room! Aborting...");
            throw new RuntimeException("Cannot move player to admin room!");
        }
        if(Store.getInstance().getGameStage().getStage() == Stage.IDLE) {
            return;
        }
        Vector adminRoom = ConfigManager.getInstance().getAdminRoomCoords();
        player.teleport(new Location(player.getWorld(), adminRoom.getX(), adminRoom.getY(), adminRoom.getZ()));
    }
    
    public void MoveToLobby(Player player) {
        Vector lobbyRoom = ConfigManager.getInstance().getLobbyRoomCoords();
        player.teleport(new Location(player.getWorld(), lobbyRoom.getX(), lobbyRoom.getY(), lobbyRoom.getZ()));
    }
}
