package remonone.nftilation.handlers;

import com.google.gson.internal.LinkedTreeMap;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import remonone.nftilation.application.models.SkinProperty;
import remonone.nftilation.application.models.SkinRestorationContainer;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.RequestConstant;
import remonone.nftilation.utils.HttpRequestSender;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerNMSUtil;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class JoinPlayerHandler implements Listener {
    
    @SuppressWarnings("unchecked")
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().setScoreboard(getServer().getScoreboardManager().getNewScoreboard());
        event.setJoinMessage(event.getPlayer().getDisplayName() + ChatColor.RESET + MessageConstant.JOIN_GAME);
        try {
            List<?> playersList =  HttpRequestSender.post(RequestConstant.REQ_GET_PLAYER_ID, new String[] {event.getPlayer().getDisplayName()}, List.class);
            String uuid = (String)((LinkedTreeMap<String, Object>)playersList.get(0)).get("id");
            SkinRestorationContainer container = HttpRequestSender.get(RequestConstant.REQ_RESTORE_SKIN + uuid + "?unsigned=false", SkinRestorationContainer.class);
            if (container != null) {
                List<SkinProperty> properties = container.getProperties();
                SkinProperty skin = properties.get(0);
                PlayerNMSUtil.changePlayerSkin(event.getPlayer(), skin.getValue(), skin.getSignature());
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }
}
