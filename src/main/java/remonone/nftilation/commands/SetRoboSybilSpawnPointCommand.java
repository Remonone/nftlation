package remonone.nftilation.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.utils.CommandUtils;

public class SetRoboSybilSpawnPointCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        CommandUtils.State state = CommandUtils.verifyPlayerSender(commandSender, strings, 0);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        Player player = (Player) commandSender;
        Location location = player.getLocation();
        ConfigManager.getInstance().addRoboSybilPoint(location);
        return true;
    }
}
