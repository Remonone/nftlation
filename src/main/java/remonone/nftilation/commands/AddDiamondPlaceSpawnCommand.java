package remonone.nftilation.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.utils.ConfigUtils;

public class AddDiamondPlaceSpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if(ConfigUtils.trySendMessageOnProhibited(player, Store.getInstance().getDataInstance().FindPlayerByName(player.getName()))) {
            return true;
        }
        ConfigManager.getInstance().addDiamondsSpawnPoint(player.getLocation());
        return true;
    }
}
