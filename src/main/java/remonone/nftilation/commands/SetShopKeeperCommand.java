package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.utils.ConfigUtils;


public class SetShopKeeperCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player");
            return true;
        }
        Player player = (Player) sender;
        if(ConfigUtils.trySendMessageOnProhibited(player, Store.getInstance().getDataInstance().FindPlayerByName(player.getUniqueId()).getData())) {
            return true;
        }
        if(args.length == 0) {
            return false;
        }
        if(!ConfigManager.getInstance().trySetShopKeeper(args[0], player.getLocation())) {
            player.sendMessage(ChatColor.RED + "This position is not existing!");
        }
        return true;
    }
}
