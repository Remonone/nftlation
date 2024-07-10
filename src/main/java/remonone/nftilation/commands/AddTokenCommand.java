package remonone.nftilation.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameInstance;

public class AddTokenCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return true;
        if(!(sender instanceof Player)) return true;
        if(args.length != 1) return false;
        Player player = (Player) sender;
        PlayerData data = Store.getInstance().getDataInstance().FindPlayerByName(player.getName());
        GameInstance.getInstance().awardPlayer(data.getTeam().getTeamName(), player, Integer.parseInt(args[0]));
        return true;
    }
}
