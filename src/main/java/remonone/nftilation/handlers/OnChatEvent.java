package remonone.nftilation.handlers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import remonone.nftilation.Store;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TruncatedTeam;
import remonone.nftilation.utils.PlayerUtils;

public class OnChatEvent implements Listener {
    
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        Stage stage = Store.getInstance().getGameStage().getStage();
        if(!stage.equals(Stage.IN_GAME)) {
            event.setFormat(player.getName() + ": " + ChatColor.WHITE + message);
        } else {
            sendMessagePreference(event);
        }
        
    }

    private void sendMessagePreference(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(model == null) {
            event.setFormat(player.getName() + ": " + ChatColor.WHITE + message);
            return;
        }
        
        ITeam team = PlayerUtils.getTeamFromPlayer(player);
        if(team == null) {
            player.sendMessage(ChatColor.RED + "Вы не можете отправить сообщение в данный момент!");
            return;
        }
        if(team instanceof TruncatedTeam) {
            event.setFormat(player.getName() + ": " + ChatColor.WHITE + message);
            return;
        }
        if(message.startsWith("!")) {
            message = message.substring(1);
            event.setFormat(ChatColor.GRAY + "[ALL] " + player.getName() + ": " + ChatColor.GRAY + message);
            return;
        }
        event.setCancelled(true);
        for(PlayerModel teammate : team.getPlayers()) {
            teammate.getReference().sendMessage(ChatColor.GRAY + "[TEAM] " + player.getName() + ": " + ChatColor.WHITE + message);
        }
    }
}
