package remonone.nftilation.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.utils.ConfigUtils;

public class SetRoboSybylSpawnPointCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!Store.getInstance().getGameStage().getStage().equals(Stage.IDLE)) return true;
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return true;
        }
        Player player = (Player) commandSender;
        if(ConfigUtils.trySendMessageOnProhibited(player, Store.getInstance().getDataInstance().FindPlayerByName(player.getName()))) {
            return true;
        }
        Location location = player.getLocation();
        ConfigManager.getInstance().addRoboSybylPoint(location);
        return true;
    }
}
