package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.utils.ConfigUtils;
import remonone.nftilation.utils.Logger;

public class SetDieCenterSpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        PlayerData data = Store.getInstance().getDataInstance().FindPlayerByName(player.getUniqueId()).getData();
        if(ConfigUtils.trySendMessageOnProhibited(player, data)) {
            return true;
        }
        Location l = player.getLocation();
        Vector toSave = l.toVector();
        toSave.setX((int)toSave.getX() + .5);
        toSave.setY((int)toSave.getY());
        toSave.setZ((int)toSave.getZ() + .5);
        ConfigManager.getInstance().setCenterDeadZoneCoords(toSave);
        player.sendMessage(ChatColor.GREEN + MessageConstant.DIE_CENTER_COMMAND);
        Logger.log(player.getName() + " has set center die position coords to " + toSave);
        return true;
    }
}
