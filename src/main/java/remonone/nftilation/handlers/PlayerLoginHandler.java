package remonone.nftilation.handlers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.events.OnPlayerLoginValidateEvent;
import remonone.nftilation.events.PlayerLoginEvent;
import remonone.nftilation.enums.LoginState;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerUtils;

import java.util.EnumSet;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class PlayerLoginHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void verifyPlayerLogin(final PlayerLoginEvent event) {
        Player player = event.getPlayer();
        PlayerData data = event.getPlayerData();
        Store instance = Store.getInstance();
        // Check if player able to join
        DataInstance dataInstance = instance.getDataInstance();

        LoginState state = dataInstance.tryAddPlayerToGame(data, player);
        if(!EnumSet.of(LoginState.ALREADY_LOGGED_IN, LoginState.LOGGED_IN).contains(state)) {
            event.setCancelled(true);
            kickPlayerWithReason(player, state);
            return;
        }
        if(state.equals(LoginState.ALREADY_LOGGED_IN)) {
            player.sendMessage(MessageConstant.ALREADY_LOGGED_IN);
            event.setCancelled(true);
            return;
        }
        if(!instance.getGameStage().getStage().equals(Stage.IN_GAME) || !PlayerRole.PLAYER.equals(data.getRole())) {
            Logger.log("Player " + player.getDisplayName() + " has authenticated to the game!");
            getServer().getPluginManager().callEvent(new OnPlayerLoginValidateEvent(event.getPlayerData(), event.getPlayer()));
            return;
        }
        DataInstance.PlayerInfo playerData = instance.getDataInstance().FindPlayerByID(player.getUniqueId());
        if(playerData.getData() == null) {
            event.setCancelled(true);
            player.kickPlayer(MessageConstant.NO_PERMISSION_TO_JOIN);
            return;
        }
        if(playerData.getData().getTeam() == null || playerData.getData().getTeam().getTeamName() == null) {
            event.setCancelled(true);
            player.kickPlayer(MessageConstant.NO_PERMISSION_TO_JOIN);
            return;
        }
        Role playerRole = instance.getDataInstance().getPlayerRole(player.getUniqueId());
        if(playerRole == null) {
            event.setCancelled(true);
            Logger.warn("Player " + player.getDisplayName() + " have empty role! Kicking...");
            player.kickPlayer(MessageConstant.NO_PERMISSION_TO_JOIN);
            return;
        }
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        Map<String, Object> params = model.getParameters();
        int level = (int) params.getOrDefault(PropertyConstant.PLAYER_LEVEL_PARAM, -1);
        if(level < 0) {
            Logger.warn("Player " + player.getDisplayName() + " have incorrect level! Kicking...");
            player.kickPlayer(MessageConstant.UNKNOWN_KICK);
        }
        Logger.log("Player " + player.getDisplayName() + " has authenticated to the game!");
        getServer().getPluginManager().callEvent(new OnPlayerLoginValidateEvent(event.getPlayerData(), event.getPlayer()));
    }

    private void kickPlayerWithReason(Player player, LoginState state) {
        String reason = GetReason(state);
        player.kickPlayer(reason);
    }

    private String GetReason(LoginState state) {
        switch(state) {
            case NOT_PRESENTED:
                return MessageConstant.NO_TOURNAMENT_PRESENTED;
            case NOT_ALLOWED:
                return MessageConstant.NO_PERMISSION_TO_JOIN;
            default:
                return MessageConstant.UNKNOWN_KICK;
        }
    }
    
    @EventHandler
    public void onPlayerDisconnect(final PlayerQuitEvent event) {
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        dataInstance.disconnectPlayer(event.getPlayer().getUniqueId());
    }


}
