package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import remonone.nftilation.restore.DumpReader;
import remonone.nftilation.utils.CommandUtils;
import remonone.nftilation.utils.Logger;

public class RestoreGameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        CommandUtils.State state = CommandUtils.verifyEligibleSender(commandSender, strings, 1, false);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        String fileName = "dumps/" + strings[0] + ".yml";
        boolean result = DumpReader.readDump(fileName);
        if(!result) {
            Logger.error("Cannot restore game!");
            commandSender.sendMessage(ChatColor.RED + "Cannot restore game!");
            return true;
        }
        commandSender.sendMessage(ChatColor.GREEN + "Game from file " + fileName + " restored!");
        return true;
    }
}
