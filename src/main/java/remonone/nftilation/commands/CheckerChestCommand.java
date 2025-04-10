package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.utils.CommandUtils;

public class CheckerChestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        CommandUtils.State state = CommandUtils.verifyPlayerSender(commandSender, strings, 1);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        Player player = (Player) commandSender;
        if(!ConfigManager.getInstance().trySetCheckerPosition(strings[0], player.getLocation())) {
            player.sendMessage(ChatColor.RED + "This position is not existing!");
        }
        return true;
    }
}
