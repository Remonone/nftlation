package remonone.nftilation.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.utils.CommandUtils;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.VectorUtils;

public class SetCenterPositionCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        CommandUtils.State state = CommandUtils.verifyPlayerSender(sender, args, 0);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        Player player = (Player) sender;
        Location loc = player.getLocation();
        ConfigManager.getInstance().setCenterLocation(loc);
        Logger.log("Center location has been set to position " + VectorUtils.convertRoundVectorString(loc.toVector()));
        player.sendMessage("You set center position to " + VectorUtils.convertRoundVectorString(loc.toVector()));
        return true;
    }
}
