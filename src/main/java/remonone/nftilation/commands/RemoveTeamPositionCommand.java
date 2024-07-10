package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.config.ConfigManager;

public class RemoveTeamPositionCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player");
            return true;
        }
        Player player = (Player) sender;
        if(args.length == 0) {
            return false;
        }
        
        if(ConfigManager.getInstance().isPositionExisting(args[0])) {
            ConfigManager.getInstance().removeTeamSpawnPosition(args[0]);
        } else {
            player.sendMessage(ChatColor.RED + "Team was not found!");
        }
        
        return true;
    }
}
