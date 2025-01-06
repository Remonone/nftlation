package remonone.nftilation.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import remonone.nftilation.Store;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.enums.PlayerRole;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.CommandUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MoveToPlayer implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        CommandUtils.State state = CommandUtils.verifyPlayerSender(commandSender, strings, 1);
        if(!state.equals(CommandUtils.State.NONE)) return state.getValue();
        RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_PLAYERS_ABLE_TO_MOVE, false);
        if(GameInstance.getInstance().getCounter() == null) {
            commandSender.sendMessage(ChatColor.RED + MessageConstant.GAME_NOT_START_YET);
            return true;
        }
        DataInstance.PlayerInfo playerInfo = Store.getInstance().getDataInstance().getPlayers().stream().filter(info -> info.getData().getLogin().equals(strings[0])).findFirst().orElse(null);
        if(playerInfo == null) {
            return false;
        }
        Player player = Bukkit.getPlayer(playerInfo.getPlayerId());
        Player sender = (Player) commandSender;
        sender.teleport(player.getLocation());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!Store.getInstance().getGameStage().getStage().equals(Stage.IN_GAME)) return Collections.emptyList();
        if(!(commandSender instanceof Player)) return Collections.emptyList();
        Player player = (Player) commandSender;
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        if(dataInstance.FindPlayerByName(player.getUniqueId()).getData().getRole().equals(PlayerRole.PLAYER)) return Collections.emptyList();
        if(strings.length > 1) return Collections.emptyList();
        List<String> playerNicknames = dataInstance.getPlayers().stream().filter(playerInfo -> playerInfo.getData().getRole().equals(PlayerRole.PLAYER)).map(playerInfo -> playerInfo.getData().getLogin()).collect(Collectors.toList());
        if(strings[0] == null || strings[0].isEmpty()) return playerNicknames;
        StringUtil.copyPartialMatches(strings[0], playerNicknames, playerNicknames);
        return playerNicknames;
    }
}
