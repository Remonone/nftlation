package remonone.nftilation.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import remonone.nftilation.Nftilation;
import remonone.nftilation.utils.CommandUtils;

public class ReloadConfigCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        CommandUtils.State state = CommandUtils.verifyEligibleSender(commandSender, strings, 0, false);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        Nftilation.getInstance().ReloadProperties();
        return true;
    }
}
