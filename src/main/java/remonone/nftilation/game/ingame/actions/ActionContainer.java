package remonone.nftilation.game.ingame.actions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerUtils;

import java.util.*;

public class ActionContainer {
    private static final Map<ActionType, IAction> actions = new HashMap<>();
    
    public static void registerAction(ActionType type, IAction action) {
        if(actions.containsKey(type)) {
            Logger.error("Action type has been reserved already! Provided: " + type.toString());
            return;
        }
        actions.put(type, action);
    }
    
    public static void InitAction(ActionType type, Map<String, Object> parameters) {
        if(!actions.containsKey(type)) return;
        IAction action = actions.get(type);
        
        action.Init(parameters);
        
        
        if(!parameters.containsKey(PropertyConstant.ACTION_SILENT) || parameters.get(PropertyConstant.ACTION_SILENT).equals(false)) {
            NotifyActionStart(action, type, parameters);
        }
    }

    @SuppressWarnings("unchecked")
    private static void NotifyActionStart(IAction action, ActionType type, Map<String, Object> parameters) {
        List<Player> players;
        if(parameters.containsKey(PropertyConstant.ACTION_TEAM)) {
            String teamName = (String)parameters.get(PropertyConstant.ACTION_TEAM);
            players = PlayerUtils.getPlayersFromTeam(teamName);

        } else if(parameters.containsKey(PropertyConstant.ACTION_PLAYER)) {
            String playerName = (String)parameters.get(PropertyConstant.ACTION_PLAYER);
            DataInstance.PlayerInfo player = Store.getInstance().getDataInstance().getPlayers().stream().filter(playerInfo -> playerInfo.getData().getLogin().equals(playerName)).findFirst().orElse(null);
            if(player == null) return;
            players = Collections.singletonList(Bukkit.getPlayer(player.getPlayerId()));
        } else {
            players = (List<Player>) Bukkit.getOnlinePlayers();
        }
        players.forEach(player -> {
            player.sendTitle(action.getTitle(), action.getDescription(), 5, 100, 5);
            player.playSound(player.getLocation(), action.getSound(), 1, 1);
            if(parameters.containsKey(PropertyConstant.ACTION_SEND_MESSAGE)) {
                String sender = "Anonymous";
                if (parameters.containsKey(PropertyConstant.ACTION_SENDER)) {
                    sender = parameters.get(PropertyConstant.ACTION_SENDER).toString();
                }
                player.sendMessage(ChatColor.GOLD + "[DONATION] " + sender + " начал донатное событие: " + type.name());
                if(parameters.containsKey(PropertyConstant.ACTION_COMMENT)) {
                    String comment = parameters.get(PropertyConstant.ACTION_COMMENT).toString();
                    player.sendMessage(ChatColor.GOLD + "[DONATION] " + comment);
                }
            }
        });
    }

}
