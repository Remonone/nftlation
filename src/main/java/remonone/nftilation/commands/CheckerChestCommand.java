package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.utils.ConfigUtils;

public class CheckerChestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("This command can only be executed by a player");
            return true;
        }
        Player player = (Player) commandSender;
        if(ConfigUtils.trySendMessageOnProhibited(player, Store.getInstance().getDataInstance().FindPlayerByName(player.getUniqueId()).getData())) {
            return true;
        }
        if(strings.length == 0) {
            return false;
        }
        if(!ConfigManager.getInstance().trySetCheckerPosition(strings[0], player.getLocation())) {
            player.sendMessage(ChatColor.RED + "This position is not existing!");
        }
        return true;
    }
}
