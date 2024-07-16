package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.utils.CommandUtils;


public class SetTeamCoreBlockCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandUtils.State state = CommandUtils.verifyPlayerSender(sender, args, 1);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        Player player = (Player) sender;
        Vector position = player.getLocation().toVector();
        if(!ConfigManager.getInstance().trySetTeamSpawnCore(args[0], position)) {
            player.sendMessage(ChatColor.RED + "This position is not existing!");
        }
        return true;
    }
}
