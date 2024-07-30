package remonone.nftilation.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import remonone.nftilation.Store;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.utils.CommandUtils;

import java.util.Collections;
import java.util.List;

public class SetTokenCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandUtils.State state = CommandUtils.verifyPlayerSender(sender, args, 1);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        Bukkit.getOnlinePlayers().forEach(online -> {
            String team = Store.getInstance().getDataInstance().getPlayerTeam(online.getUniqueId());
            if(team.isEmpty()) return;
            GameInstance.PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, online);
            if(model == null) return;
            model.setTokens(Integer.parseInt(args[0]));
        });
//        String teamName = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
//        GameInstance.getInstance().awardPlayer(teamName, player, Integer.parseInt(args[0]));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {

        return Collections.emptyList();
    }
}
