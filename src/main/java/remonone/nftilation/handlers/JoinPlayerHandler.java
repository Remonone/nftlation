package remonone.nftilation.handlers;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import remonone.nftilation.constants.MessageConstant;

import static org.bukkit.Bukkit.getServer;

public class JoinPlayerHandler implements Listener {
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().setScoreboard(getServer().getScoreboardManager().getNewScoreboard());
        event.setJoinMessage(event.getPlayer().getDisplayName() + ChatColor.RESET + MessageConstant.JOIN_GAME);
    }
}
