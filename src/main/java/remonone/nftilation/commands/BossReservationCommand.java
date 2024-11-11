package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;

public class BossReservationCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only player can execute command!");
            return true;
        }
        Player player = (Player) commandSender;
        DataInstance instance = Store.getInstance().getDataInstance();
        DataInstance.PlayerInfo info = instance.FindPlayerByName(player.getUniqueId());
        if(info == null) {
            commandSender.sendMessage("You are not authorized!");
            return true;
        }
        if(info.getData().getRole().equals(PlayerRole.PLAYER)) {
            return true;
        }
        if(!GameInstance.getInstance().setReservation(player.getUniqueId())) {
            commandSender.sendMessage("Boss reservation is already taken!");
            return true;
        }
        commandSender.sendMessage(ChatColor.RED + "It's time to beat up some butts! >:3");
        return true;
    }
}
