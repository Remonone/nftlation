package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.utils.CommandUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SetTeamCoreHealth implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        CommandUtils.State state = CommandUtils.verifyPlayerSender(sender, args, 2);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        Player player = (Player) sender;
        String teamName = args[0];
        int health;
        try {
            health = Integer.parseInt(args[1]);
        } catch(NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid health number: " + args[1]);
            return false;
        }
        if(health < 0 || health > 100) {
            player.sendMessage(ChatColor.RED + "Invalid health number: " + args[1]);
            return true;
        }
        if(!GameInstance.getInstance().setHealth(player, teamName, health)) {
            player.sendMessage(ChatColor.RED + "Failed to set health for the team core: " + teamName);
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(!(commandSender instanceof Player)) {
            return Collections.emptyList();
        }
        Player performer = (Player) commandSender;
        DataInstance data = Store.getInstance().getDataInstance();
        if(data.FindPlayerByID(performer.getUniqueId()) == null || !data.FindPlayerByID(performer.getUniqueId()).getData().getRole().equals(PlayerRole.ADMIN)) {
            return Collections.emptyList();
        }
        List<String> info = new ArrayList<>();
        GameInstance instance = GameInstance.getInstance();
        instance.getTeamIterator().forEachRemaining(team -> info.add(team.getTeamName()));
        return info;
    }
}
