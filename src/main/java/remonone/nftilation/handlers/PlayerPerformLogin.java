package remonone.nftilation.handlers;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.application.services.SkinCache;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.events.OnPlayerLoginValidateEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.scoreboard.ScoreboardHandler;
import remonone.nftilation.utils.PlayerNMSUtil;
import remonone.nftilation.utils.PlayerUtils;
import remonone.nftilation.utils.ResetUtils;

public class PlayerPerformLogin implements Listener {
    @EventHandler
    public void onPlayerLogin(final OnPlayerLoginValidateEvent event) {
        Player player = event.getPlayer();
        PlayerData data = event.getPlayerData();
        // Reset stats if players
        if(PlayerRole.PLAYER.equals(data.getRole())) {
            ResetUtils.globalResetPlayerStats(player);
        }
        Store instance = Store.getInstance();
        
        // Setting up the player
        String toDisplay = getPlayerNickname(data);
        player.setDisplayName(toDisplay);
        player.setPlayerListName(toDisplay);
        player.setCustomName(toDisplay);
        player.setPlayerListName(toDisplay);
        player.sendMessage(ChatColor.GREEN + MessageConstant.SUCCESSFUL_LOGIN);
        PlayerNMSUtil.changePlayerName(player, toDisplay);
        // Transferring the player
        if(!PlayerRole.PLAYER.equals(data.getRole())) {
            if(Stage.IN_GAME.equals(instance.getGameStage().getStage()))
                GameInstance.getInstance().getCounter().getBarWorker().getBar().addPlayer(player);
            return;
        }
        if(!Stage.IN_GAME.equals(instance.getGameStage().getStage())) {
            instance.getLobbyService().DisposePlayer(data, player);
            player.setGameMode(GameMode.ADVENTURE);
            return;
        }
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        model.setReference(player);
        Role role = instance.getDataInstance().getPlayerRole(player.getUniqueId());
        PlayerNMSUtil.changePlayerSkin(player, SkinCache.getInstance().getTexture(role.getRoleID()), SkinCache.getInstance().getSignature(role.getRoleID()));
        ScoreboardHandler.updateScoreboard(model);
        GameInstance.getInstance().getCounter().getBarWorker().getBar().addPlayer(player);
        Role.updatePlayerAbilities(player);
    }

    private String getPlayerNickname(PlayerData playerData) {
        if(playerData.getRole() == PlayerRole.PLAYER) {
            return formatNickname(playerData.getTeam().getTeamColor(), playerData.getTeam().getTeamShort(), playerData.getLogin());
        } else {
            return formatNickname(ChatColor.RED.getChar(), playerData.getRole().toString(), playerData.getLogin());
        }
    }

    private String formatNickname(char color, String prefix, String name) {
        return ChatColor.getByChar(color) + "[" + prefix + "] " + name;
    }
}
