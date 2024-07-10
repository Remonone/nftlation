package remonone.nftilation.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.events.StageEvent;

import static org.bukkit.Bukkit.getServer;

public class StartLobbyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (!player.isOp()) {
            return true;
        }
        getServer().getPluginManager().callEvent(new StageEvent(Store.getInstance().getGameStage().getStage(), Stage.LOBBY, player.getWorld()));

        return true;
    }
}
