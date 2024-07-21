package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.utils.CommandUtils;
import remonone.nftilation.utils.Logger;

public class AddAirDropCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        CommandUtils.State state = CommandUtils.verifyPlayerSender(commandSender, args, 1);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        Player player = (Player) commandSender;
        Location loc = player.getLocation();
        if(!ConfigManager.getInstance().trySetTeamAirDropPosition(args[0], loc)) {
            Logger.log("Wrong id");
            return true;
        }
        Logger.log(ChatColor.GREEN + "You have been successfully set air drop position for team id: " + args[0]);
        return true;
    }
}
