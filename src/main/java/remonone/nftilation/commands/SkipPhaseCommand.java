package remonone.nftilation.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameInstance;

public class SkipPhaseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return true;
        GameInstance.getInstance().getCounter().SkipPhase();
        return true;
    }
}
