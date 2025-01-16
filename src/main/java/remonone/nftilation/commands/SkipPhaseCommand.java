package remonone.nftilation.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import remonone.nftilation.Store;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.utils.CommandUtils;

public class SkipPhaseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandUtils.State state = CommandUtils.verifyPlayerSender(sender, args, 0);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return true;
        GameInstance.getInstance().getCounter().skipPhase();
        return true;
    }
}
