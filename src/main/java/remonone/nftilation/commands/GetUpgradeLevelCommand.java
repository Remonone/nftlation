package remonone.nftilation.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.game.GameInstance;

public class GetUpgradeLevelCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if(player == null) return false;
        PlayerData data = Store.getInstance().getDataInstance().FindPlayerByName(player.getName());
        player.sendMessage(String.valueOf(GameInstance.getInstance().getPlayerModelFromTeam(data.getTeam().getTeamName(), player).getUpgradeLevel()));
        return true;
    }
}
