package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.restore.DumpCollector;
import remonone.nftilation.utils.CommandUtils;

public class CreateBackupCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        CommandUtils.State state = CommandUtils.verifyEligibleSender(commandSender, strings, 0, false);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        if(GameInstance.getInstance().getCounter() == null) {
            commandSender.sendMessage(ChatColor.RED + MessageConstant.GAME_NOT_START_YET);
            return true;
        }
        DumpCollector.generateDump();
        commandSender.sendMessage(ChatColor.GRAY + "Backup was successfully created!");
        return true;
    }
}
