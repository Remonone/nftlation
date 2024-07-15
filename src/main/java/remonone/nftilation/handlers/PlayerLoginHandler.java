package remonone.nftilation.handlers;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.events.PlayerLoginEvent;
import remonone.nftilation.enums.LoginState;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.scoreboard.ScoreboardHandler;
import remonone.nftilation.game.stage.GameStage;
import remonone.nftilation.utils.PlayerNMSUtil;
import remonone.nftilation.utils.ResetUtils;

import java.util.EnumSet;

public class PlayerLoginHandler implements Listener {

    @EventHandler
    public void onPlayerLogin(final PlayerLoginEvent event) {
        Player player = event.getPlayer();
        PlayerData data = event.getPlayerData();
        Store instance = Store.getInstance();
        GameStage stage = instance.getGameStage();
        // Check if player able to join
        DataInstance dataInstance = instance.getDataInstance();

        LoginState state = dataInstance.TryAddPlayerToGame(data, player);
        if(!EnumSet.of(LoginState.ALREADY_LOGGED_IN, LoginState.LOGGED_IN).contains(state)) {
            KickPlayerWithReason(player, state);
            return;
        }
        
        if(state.equals(LoginState.ALREADY_LOGGED_IN)) {
            player.sendMessage(MessageConstant.ALREADY_LOGGED_IN);
            return;
        }
        ResetUtils.globalResetPlayerStats(player);
        
        // Setting up the player
        String toDisplay;
        if(data.getRole() == PlayerRole.PLAYER) {
            toDisplay = formatNickname(data.getTeam().getTeamColor(), data.getTeam().getTeamShort(), player.getName());
        } else {
            toDisplay = formatNickname(ChatColor.RED.getChar(), data.getRole().toString(), player.getName());
        }
        player.setDisplayName(toDisplay);
        player.setPlayerListName(toDisplay);
        player.setCustomName(toDisplay);
        player.setPlayerListName(toDisplay);
        player.sendMessage(ChatColor.GREEN + MessageConstant.SUCCESSFUL_LOGIN);
        PlayerNMSUtil.changePlayerName(player, toDisplay);
        // Transferring the player
        if(stage.getStage() == Stage.LOBBY) {
            instance.getLobbyDisposer().DisposePlayer(data, player);
            player.setGameMode(GameMode.ADVENTURE);
        }
        
        if(stage.getStage() == Stage.IN_GAME) {
            player.setGameMode(GameMode.SURVIVAL);
            Role role = instance.getDataInstance().getPlayerRole(player.getUniqueId());
            DataInstance.PlayerInfo playerData = instance.getDataInstance().FindPlayerByName(player.getUniqueId());
            GameInstance.PlayerModel model = GameInstance.getInstance()
                    .getTeamPlayers(playerData.getData().getTeam().getTeamName())
                    .stream()
                    .filter(playerModel -> playerModel.getReference().getUniqueId().equals(playerData.getPlayerId()))
                    .findFirst()
                    .orElse(null);
            if(model == null) {
                player.kickPlayer(MessageConstant.NO_PERMISSION_TO_JOIN);
                return;
            }
            model.setReference(player);
            ScoreboardHandler.updateScoreboard(model);
            GameInstance.getInstance().getCounter().bar.addPlayer(player);
            Role.UpdatePlayerAbilities(player, role, model.getUpgradeLevel());
        }
            
    }

    private void KickPlayerWithReason(Player player, LoginState state) {
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

    private String formatNickname(char color, String prefix, String name) {
        return ChatColor.getByChar(color) + "[" + prefix + "] " + name;
    }
    
    @EventHandler
    public void onPlayerDisconnect(final PlayerQuitEvent event) {
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        dataInstance.DisconnectPlayer(event.getPlayer().getUniqueId());
    }


}
