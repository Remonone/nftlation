package remonone.nftilation.game.ingame.services;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.events.OnCoreHealEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.game.scoreboard.ScoreboardHandler;
import remonone.nftilation.utils.NotificationUtils;

import static org.bukkit.Bukkit.getServer;

public class RepairCoreService implements IPurchasableService {
    
    @Override
    public String getServiceName() {
        return "base-repair";
    }

    @Override
    public void OnPurchase(Player buyer, float price) {
        String team = Store.getInstance().getDataInstance().getPlayerTeam(buyer.getUniqueId());
        PlayerInteractComponent component = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        if(component == null) return;
        if(!component.adjustPlayerTokens(buyer, -price, TransactionType.PURCHASE)) {
            NotificationUtils.sendNotification(buyer, MessageConstant.NOT_ENOUGH_MONEY, NotificationUtils.NotificationType.FAIL, false);
            return;
        }
        OnCoreHealEvent event = new OnCoreHealEvent(team);
        getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) {
            // Cannot heal core
            NotificationUtils.sendNotification(buyer, MessageConstant.CANNOT_HEAL_CORE, NotificationUtils.NotificationType.FAIL, false);
            component.adjustPlayerTokens(buyer, price, TransactionType.TRANSFER);
            return;
        }
        // Notify about heal
        ITeam healedTeam = GameInstance.getInstance().getTeam(team);
        healedTeam.getPlayers().forEach(ScoreboardHandler::updateScoreboard);
        World world = buyer.getWorld();
        world.playSound(buyer.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
    }
}
