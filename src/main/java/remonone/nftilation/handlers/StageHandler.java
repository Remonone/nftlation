package remonone.nftilation.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.application.services.MiddlewareService;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.events.StageEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.utils.Logger;

import java.util.List;

public class StageHandler implements Listener {

    @EventHandler
    public void onStageChanged(StageEvent e) {

        if(e.getOldStage() == Stage.IN_GAME) {
            e.setCancelled(true);
            Logger.warn("Trying to change stage during game! Aborting...");
            return;
        }
        if(e.getNewStage() == Stage.LOBBY) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    List<TeamData> data = MiddlewareService.fetchTeams();
                    if(data == null) {
                        e.setCancelled(true);
                        Logger.log("Fetch was failed aborting...");
                        return;
                    }
                    if(ConfigManager.getInstance().positionsSize() != data.size()) {
                        Bukkit.broadcastMessage(ChatColor.RED + MessageConstant.TEAM_NOT_EQUALS);
                        Logger.warn("Team count is not same as reserved! Reserved: " + ConfigManager.getInstance().positionsSize() + ". Received: " + data.size() + ". Aborting...");
                        e.setCancelled(true);
                        return;
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Store.getInstance().getDataInstance().Init(data, e.getWorld());
                            Store.getInstance().getGameStage().setStage(e.getNewStage());
                        }
                    }.runTask(Nftilation.getInstance());
                }
            }.runTaskAsynchronously(Nftilation.getInstance());
        }
        if(e.getNewStage() == Stage.IN_GAME) {
            new BukkitRunnable() {
                @Override
                public void run() {
//                    if(!MiddlewareService.applyPlayers()) {
//                        return;
//                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            GameInstance.getInstance().startGame();
                            Store.getInstance().getGameStage().setStage(e.getNewStage());
                        }
                    }.runTask(Nftilation.getInstance());
                }
            }.runTaskAsynchronously(Nftilation.getInstance());
        }
    }
}
