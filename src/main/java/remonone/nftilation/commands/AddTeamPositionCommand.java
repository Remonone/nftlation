package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.utils.CommandUtils;

public class AddTeamPositionCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandUtils.State state = CommandUtils.verifyPlayerSender(sender, args, 0);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        Player player = (Player) sender;
        String teamId = ConfigManager.getInstance().addTeamSpawnPosition(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Team with id " + teamId + " have been created");
        return true;
    }
}
