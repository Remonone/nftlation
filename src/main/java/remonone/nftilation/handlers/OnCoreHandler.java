package remonone.nftilation.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.events.OnCoreDamageEvent;
import remonone.nftilation.events.OnCoreDestroyEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.objects.ICoreData;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.game.scoreboard.ScoreboardHandler;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.NotificationUtils;

import java.util.List;

public class OnCoreHandler implements Listener {
    
    @EventHandler
    public void onCoreDestroyed(OnCoreDestroyEvent e) {
        ITeam destroyedTeam = GameInstance.getInstance().getTeam(e.getDestroyedTeam());
        notifyAboutDestruction(destroyedTeam);
        destroyedTeam.getTeamSpawnPoint().getCoreCenter().getBlock().setType(Material.AIR);
        PlayerInteractComponent component = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        List<PlayerModel> players = GameInstance.getInstance().getTeam(e.getDestroyerTeam()).getPlayers();
        for(PlayerModel model : players) {
            component.adjustPlayerTokens(model, DataConstants.TOKEN_PER_DESTRUCTION, TransactionType.DESTROY_GAIN);
        }
        GameInstance.getInstance().checkOnActiveTeams();
    }
    
    @EventHandler
    public void onCoreDamaged(OnCoreDamageEvent e) {
        if(e.isCancelled()) return;
        ITeam team = GameInstance.getInstance().getTeam(e.getTeamName());
        team.getPlayers().forEach(ScoreboardHandler::updateScoreboard);
        if(e.getAttacker() == null) return;
        ICoreData core = team.getCoreData();
        int oldScale = core.getOldHealth() % 5;
        int newScale = core.getHealth() % 5;
        if(newScale > oldScale) {
            team.getPlayers().forEach(playerModel -> {
                Player player = playerModel.getReference();
                NotificationUtils.sendNotification(player, MessageConstant.TEAM_DAMAGED_MESSAGE, NotificationUtils.NotificationType.WARNING, false);
                player.playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 1f, .7f);
            });
        }
    }

    private void notifyAboutDestruction(ITeam team) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            String title;
            String subTitle;
            if(team.getPlayers().stream().anyMatch(playerModel -> playerModel.getReference().getUniqueId().equals(player.getUniqueId()))) {
                title = ChatColor.RED + "" + ChatColor.BOLD + MessageConstant.CORE_DESTROYED_TITLE;
                subTitle = ChatColor.GOLD + MessageConstant.CORE_DESTROYED_SUBTITLE;
            } else {
                title = ChatColor.GREEN + "" + ChatColor.BOLD + String.format(MessageConstant.OTHER_CORE_DESTROYED_TITLE, team.getTeamName());
                subTitle = ChatColor.WHITE + MessageConstant.OTHER_CORE_DESTROYED_SUBTITLE;
            }
            player.sendTitle(title, subTitle, 10, 60, 10);
        });
        Logger.broadcast(ChatColor.GOLD + String.format(MessageConstant.CORE_DESTROYED_BROADCAST, team.getTeamName()));
    }
    
    
}
