package remonone.nftilation.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.hints.Hint;
import remonone.nftilation.utils.PlayerUtils;

public class AddHintCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!Store.getInstance().getGameStage().getStage().equals(Stage.IDLE)) return true;
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return true;
        }
        Player player = (Player) commandSender;
        if(PlayerUtils.trySendMessageOnProhibited(player, Store.getInstance().getDataInstance().FindPlayerByName(player.getUniqueId()).getData())) {
            return true;
        }

        Location location = player.getLocation();
        Hint hint = Hint.builder().location(location).data("a").build();
        ConfigManager.getInstance().addHint(hint);
        return false;
    }
}
