package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.utils.ConfigUtils;

import java.util.Collection;

public class GetTeamInfoCommand implements CommandExecutor {
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
        Collection<TeamSpawnPoint> teamSpawnPoints = ConfigManager.getInstance().getTeamSpawnList();
        if(teamSpawnPoints.isEmpty()) {
            player.sendMessage(ChatColor.BLUE + "Team Point are not set");
        }
        for(TeamSpawnPoint spawnPoint : teamSpawnPoints) {
            player.sendMessage(ChatColor.BLUE + spawnPoint.toString());
        }
        return true;
    }
}
