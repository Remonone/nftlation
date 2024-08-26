package remonone.nftilation.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import remonone.nftilation.Store;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TokenTransferCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("This command can only be executed by a player");
            return true;
        }
        if(args.length < 2) {
            return false;
        }
        String recipientPlayer = args[0];
        if(StringUtils.isBlank(recipientPlayer)) {
            commandSender.sendMessage(ChatColor.RED + MessageConstant.TOKEN_TRANSFER_INCORRECT_RECIPIENT);
            return true;
        }
        DataInstance.PlayerInfo recipientInfo = Store.getInstance().getDataInstance().getPlayers().stream().filter(info -> info.getData().getLogin().equals(args[0])).findFirst().orElse(null);
        if(recipientInfo == null) {
            commandSender.sendMessage(ChatColor.RED + MessageConstant.TOKEN_TRANSFER_INCORRECT_RECIPIENT);
            return true;
        }

        Player player = (Player) commandSender;
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(model == null) {
            commandSender.sendMessage("You have no ability to use this command");
            return true;
        }
        Player recipient = Bukkit.getPlayer(recipientInfo.getPlayerId());
        PlayerModel recipientModel = PlayerUtils.getModelFromPlayer(recipient);

        if(!GameInstance.getInstance().checkIfPlayersInSameTeam(recipient, player)) {
            commandSender.sendMessage(ChatColor.RED + MessageConstant.TOKEN_TRANSFER_IMPOSSIBLE_TO_TRANSFER);
            return true;
        }

        if(player.getLocation().distance(recipient.getLocation()) > DataConstants.DISTANCE_FOR_TRANSFER) {
            commandSender.sendMessage(ChatColor.RED + MessageConstant.TOKEN_TRANSFER_TOO_FAR);
            return true;
        }

        float tokenToTransfer;
        try {
            tokenToTransfer = Float.parseFloat(args[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        PlayerInteractComponent interactComponent = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        if(interactComponent == null) {
            commandSender.sendMessage(MessageConstant.TOKEN_TRANSFER_INCORRECT_STAGE);
            return true;
        }
        boolean result = interactComponent.adjustPlayerTokens(model, -tokenToTransfer, TransactionType.TRANSFER);
        if(!result) {
            commandSender.sendMessage(MessageConstant.TOKEN_TRANSFER_INSUFFICIENT_AMOUNT);
            return true;
        }
        interactComponent.adjustPlayerTokens(recipientModel, tokenToTransfer, TransactionType.TRANSFER);
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!Store.getInstance().getGameStage().getStage().equals(Stage.IN_GAME)) return Collections.emptyList();
        if(!(commandSender instanceof Player)) return Collections.emptyList();
        Player player = (Player) commandSender;
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(model == null) return Collections.emptyList();
        if(strings.length != 0 && !strings[0].isEmpty()) return Collections.emptyList();

        String team = (String)model.getParameters().get(PropertyConstant.PLAYER_TEAM_NAME);
        if(StringUtils.isBlank(team)) return Collections.emptyList();
        List<PlayerModel> teammates = GameInstance.getInstance().getTeam(team).getPlayers();
        DataInstance instance = Store.getInstance().getDataInstance();
        List<String> playerNicknames = teammates.stream().map(mate -> instance.FindPlayerByName(mate.getReference().getUniqueId()).getData().getLogin()).collect(Collectors.toList());
        if(StringUtils.isNotBlank(strings[0])) {
            StringUtil.copyPartialMatches(strings[0], playerNicknames, playerNicknames);
        }
        return playerNicknames;
    }
}
