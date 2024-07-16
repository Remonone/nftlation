package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.events.StageEvent;
import remonone.nftilation.utils.CommandUtils;
import remonone.nftilation.utils.Logger;

import static org.bukkit.Bukkit.getServer;

public class StageSwitchCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandUtils.State state = CommandUtils.verifyPlayerSender(sender, args, 1);
        Logger.log(state.toString());
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        Player player = (Player) sender;
        String stage = args[0];
        try {
            Stage value = Stage.valueOf(stage);
            getServer().getPluginManager().callEvent(new StageEvent(Store.getInstance().getGameStage().getStage(), value, player.getWorld()));
        } catch (IllegalArgumentException ex) {
            Logger.broadcast(ChatColor.RED + "Wrong stage name. Possible stages: IDLE, LOBBY, IN_GAME");
        }
        return true;
    }
}
