package remonone.nftilation.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.game.GameInstance;

public class GetUpgradeLevelCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if(player == null) return false;
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        player.sendMessage(String.valueOf(GameInstance.getInstance().getPlayerModelFromTeam(team, player).getUpgradeLevel()));
        return true;
    }
}
