package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.CommandUtils;

public class PauseGameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        CommandUtils.State state = CommandUtils.verifyEligibleSender(commandSender, strings, 0, false);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_PLAYERS_ABLE_TO_MOVE, false);
        if(GameInstance.getInstance().getCounter() == null) {
            commandSender.sendMessage(ChatColor.RED + MessageConstant.GAME_NOT_START_YET);
            return true;
        }
        if((Boolean)RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_GAME_IS_RUNNING, true)) {
            GameInstance.getInstance().getCounter().PauseCounter();
            commandSender.sendMessage(ChatColor.GREEN + MessageConstant.GAME_PAUSED);
        } else {
            GameInstance.getInstance().getCounter().ResumeCounter();
            commandSender.sendMessage(ChatColor.GREEN + MessageConstant.GAME_RESUMED);
        }
        return true;
    }
}
