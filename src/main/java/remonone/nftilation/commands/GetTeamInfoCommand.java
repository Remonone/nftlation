package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.config.TeamSpawnPoint;
import remonone.nftilation.utils.CommandUtils;

import java.util.Collection;

import static remonone.nftilation.utils.CommandUtils.verifyEligibleSender;

public class GetTeamInfoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandUtils.State state = verifyEligibleSender(sender, args, 0, false);
        if(state != CommandUtils.State.NONE) return state.getValue();
        
        Collection<TeamSpawnPoint> teamSpawnPoints = ConfigManager.getInstance().getTeamSpawnList();
        if(teamSpawnPoints.isEmpty()) {
            sender.sendMessage(ChatColor.BLUE + "Team Points are not set");
        }
        for(TeamSpawnPoint spawnPoint : teamSpawnPoints) {
            sender.sendMessage(ChatColor.BLUE + spawnPoint.toString());
        }
        return true;
    }
}
