package remonone.nftilation.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.utils.ConfigUtils;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.VectorUtils;

public class SetCenterPositionCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if(!Store.getInstance().getGameStage().getStage().equals(Stage.IDLE)) return true;
        Player player = (Player) sender;
        if(ConfigUtils.trySendMessageOnProhibited(player, Store.getInstance().getDataInstance().FindPlayerByName(player.getName()))) {
            return true;
        }
        Location loc = player.getLocation();
        ConfigManager.getInstance().setCenterLocation(loc);
        Logger.log("Center location has been set to position " + VectorUtils.convertRoundVectorString(loc.toVector()));
        player.sendMessage("You set center position to " + VectorUtils.convertRoundVectorString(loc.toVector()));
        return true;
    }
}
