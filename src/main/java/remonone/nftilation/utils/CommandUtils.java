package remonone.nftilation.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;

public class CommandUtils {
    public static State verifyPlayerSender(CommandSender commandSender, String[] args, int mandatoryLength) {
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("This command can only be executed by a player");
            return State.FAILED_EXECUTION;
        }
        Player player = (Player) commandSender;
        if(PlayerUtils.trySendMessageOnProhibited(player, Store.getInstance().getDataInstance().FindPlayerByName(player.getUniqueId()).getData())) {
            return State.FAILED_EXECUTION;
        }
        if(args.length != mandatoryLength) {
            return State.IMPROPER_ARGS;
        }
        return State.NONE;
    }

    public static State verifyEligibleSender(CommandSender commandSender, String[] args, int mandatoryLength, boolean isIdlePermitted) {
        if(args.length != mandatoryLength) {
            return State.IMPROPER_ARGS;
        }
        if(commandSender instanceof ConsoleCommandSender) return State.NONE;
        if(!(commandSender instanceof Player)) {
            return State.FAILED_EXECUTION;
        }
        Player player = (Player) commandSender;
        if(PlayerUtils.trySendMessageOnProhibited(player, Store.getInstance().getDataInstance().FindPlayerByName(player.getUniqueId()).getData())) {
            return State.FAILED_EXECUTION;
        }
        if(isIdlePermitted && PlayerUtils.trySendMessageOnWrongStage(player)) {
            return State.FAILED_EXECUTION;
        }
        
        return State.NONE;
    }
    
    public enum State {
        NONE(true),
        FAILED_EXECUTION(true),
        IMPROPER_ARGS(false);
        private final boolean stateValue;
        
        State(boolean value) {
            this.stateValue = value;
        }
        
        public boolean getValue() {
            return stateValue;
        }
    }
}
