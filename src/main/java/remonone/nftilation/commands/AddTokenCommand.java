package remonone.nftilation.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.utils.CommandUtils;

public class AddTokenCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandUtils.State state = CommandUtils.verifyPlayerSender(sender, args, 1);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        Player player = (Player) sender;
        Bukkit.getOnlinePlayers().forEach(online -> {
            String team = Store.getInstance().getDataInstance().getPlayerTeam(online.getUniqueId());
            GameInstance.PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, online);
            if(model == null) return;
            model.setTokens(Integer.parseInt(args[0]));
        });
        String teamName = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        GameInstance.getInstance().awardPlayer(teamName, player, Integer.parseInt(args[0]));
        return true;
    }
}
