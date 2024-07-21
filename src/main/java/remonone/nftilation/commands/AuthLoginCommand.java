package remonone.nftilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.application.models.PlayerCredentials;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.application.services.MiddlewareService;
import remonone.nftilation.events.PlayerLoginEvent;
import remonone.nftilation.utils.Logger;


import static org.bukkit.Bukkit.getServer;

public class AuthLoginCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if(args.length < 2) return false;
        String adminRow = "";
        if(args.length > 2) {
            adminRow = args[2];
        }
        String pass = args[0];
        String team = args[1];
        PlayerCredentials credentials = new PlayerCredentials(player.getName(), pass, team);

        String finalAdminRow = adminRow;
        BukkitRunnable loginTask = new BukkitRunnable() {
            @Override
            public void run() {
                PlayerData data = MiddlewareService.logInPlayer(credentials, finalAdminRow.equals("ADMIN50329AAC"));
                if(data == null) {
                    Logger.error("Error during handling login command.");
                    player.sendMessage(ChatColor.RED + "Something went wrong...");
                    return;
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        getServer().getPluginManager().callEvent(new PlayerLoginEvent(data, player));
                    }
                }.runTask(Nftilation.getInstance());
            }
        };
        loginTask.runTaskAsynchronously(Nftilation.getInstance());
        
        return true;
    }
}
