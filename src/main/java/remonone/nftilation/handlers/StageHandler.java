package remonone.nftilation.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import remonone.nftilation.Store;
import remonone.nftilation.application.services.MiddlewareService;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.events.StageEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.utils.Logger;

public class StageHandler implements Listener {
    
    @EventHandler
    public void onStageChanged(StageEvent e) {

        if(e.getOldStage() == Stage.IN_GAME) {
            e.setCancelled(true);
            Logger.warn("Trying to change stage during game! Aborting...");
            return;
        }
        if(e.getNewStage() == Stage.LOBBY) {
            MiddlewareService.fetchTeams((teams) -> {
                if(ConfigManager.getInstance().positionsSize() != teams.size()) {
                    Bukkit.broadcastMessage(ChatColor.RED + MessageConstant.TEAM_NOT_EQUALS);
                    Logger.warn("Team count is not same as reserved! Reserved: " + ConfigManager.getInstance().positionsSize() + ". Received: " + teams.size() + ". Aborting...");
                    e.setCancelled(true);
                    return null;
                }
                Store.getInstance().getDataInstance().Init(teams, e.getWorld());
                Store.getInstance().getGameStage().setStage(e.getNewStage());
                return null;
            }, (err) -> {
                e.setCancelled(true);
                Logger.error(err);
                return null;
            });
        }
        if(e.getNewStage() == Stage.IN_GAME) {
            GameInstance.getInstance().startGame();
            Store.getInstance().getGameStage().setStage(e.getNewStage());
        }
    }
}
