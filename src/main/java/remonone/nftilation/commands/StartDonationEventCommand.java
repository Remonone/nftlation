package remonone.nftilation.commands;

import com.google.gson.Gson;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.Donation;
import remonone.nftilation.application.services.MiddlewareService;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.ingame.actions.ActionContainer;
import remonone.nftilation.utils.Logger;

public class StartDonationEventCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!(commandSender instanceof ConsoleCommandSender)) return true;
        if(!Store.getInstance().getGameStage().getStage().equals(Stage.IN_GAME)) return true;
        if(args.length < 1) return false;
        Donation donation = new Gson().fromJson(args[0], Donation.class);
        try {
            if(donation.getType() == null) throw new Exception("Empty type");
            donation.getParameters().put(PropertyConstant.ACTION_SEND_MESSAGE, true);
            ActionContainer.InitAction(donation.getType(), donation.getParameters());
            new BukkitRunnable(){
                @Override
                public void run() {
                    MiddlewareService.confirmDonation(donation);
                }
            }.runTaskAsynchronously(Nftilation.getInstance());
        } catch (Exception e) {
            Logger.error("Trying to invoke event which have no result: "+ e.getMessage());
        }
        
        return true;
    }
}
