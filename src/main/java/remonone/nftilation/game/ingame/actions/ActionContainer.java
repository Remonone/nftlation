package remonone.nftilation.game.ingame.actions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.utils.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        if(!parameters.containsKey("silent") || parameters.get("silent").equals(false)) {
            NotifyActionStart(action, type, parameters);
        }
    }

    private static void NotifyActionStart(IAction action, ActionType type, Map<String, Object> parameters) {
        List<Player> players;
        if(parameters.containsKey("team")) {
            players = GameInstance.getInstance()
                    .getTeamPlayers((String)parameters.get("team"))
                    .stream()
                    .map(GameInstance.PlayerModel::getReference)
                    .collect(Collectors.toList());

        } else {
            players = (List<Player>) Bukkit.getOnlinePlayers();
        }
        players.forEach(player -> {
            player.sendTitle(action.getTitle(), action.getDescription(), 5, 100, 5);
            player.playSound(player.getLocation(), action.getSound(), 1, 1);
            if(parameters.containsKey("sender")) {
                player.sendMessage(ChatColor.AQUA + (String) parameters.get("sender") + "have started action: " + type.toString());
            }
        });
    }

}
