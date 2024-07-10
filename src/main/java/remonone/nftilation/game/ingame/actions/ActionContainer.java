package remonone.nftilation.game.ingame.actions;

import org.bukkit.Bukkit;
import remonone.nftilation.utils.Logger;

import java.util.HashMap;
import java.util.Map;

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
        IAction action = actions.get(type);
        action.Init(parameters);
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendTitle(action.getTitle(), action.getDescription(), 5, 100, 5);
            player.playSound(player.getLocation(), action.getSound(), 1, 1);
        });
    }
}
