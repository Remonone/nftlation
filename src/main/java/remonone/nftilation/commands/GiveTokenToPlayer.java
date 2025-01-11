package remonone.nftilation.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import remonone.nftilation.Store;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.utils.CommandUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GiveTokenToPlayer implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        CommandUtils.State state = CommandUtils.verifyPlayerSender(sender, args, 2);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        DataInstance.PlayerInfo playerInfo = Store.getInstance().getDataInstance().getPlayers().stream().filter(info -> info.getData().getLogin().equals(args[0])).findFirst().orElse(null);
        if(playerInfo == null) {
            sender.sendMessage("Player with name " + args[0] + " not found!");
            return true;
        }
        if(!playerInfo.getData().getRole().equals(PlayerRole.PLAYER)) {
            sender.sendMessage("Cannot give token to player " + args[0]);
            return true;
        }
        int amount;
        try {
           amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid amount of tokens!");
            return true;
        }
        PlayerInteractComponent playerInteract = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        if(playerInteract == null) {
            sender.sendMessage("Incorrect stage to give tokens!");
            return true;
        }
        playerInteract.adjustPlayerTokens(Bukkit.getPlayer(playerInfo.getPlayerId()), amount, TransactionType.TRANSFER);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!Store.getInstance().getGameStage().getStage().equals(Stage.IN_GAME)) return Collections.emptyList();
        if(!(commandSender instanceof Player)) return Collections.emptyList();
        Player player = (Player) commandSender;
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        if(dataInstance.FindPlayerByID(player.getUniqueId()).getData().getRole().equals(PlayerRole.PLAYER)) return Collections.emptyList();
        if(strings.length > 1) return Collections.emptyList();
        List<String> playerNicknames = dataInstance.getPlayers().stream().filter(playerInfo -> playerInfo.getData().getRole().equals(PlayerRole.PLAYER)).map(playerInfo -> playerInfo.getData().getLogin()).collect(Collectors.toList());
        StringUtil.copyPartialMatches(strings[0], playerNicknames, playerNicknames);
        return playerNicknames;
    }
}
