package remonone.nftilation.game.ingame.services.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.ingame.services.IPurchasableService;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.utils.NotificationUtils;
import remonone.nftilation.utils.PlayerUtils;

import static org.bukkit.Bukkit.getServer;

public class NukeEvent implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "nuke-event";
    }

    @Override
    public void OnPurchase(Player buyer, float price) {
        PlayerModel model = PlayerUtils.getModelFromPlayer(buyer);
        if(PlayerUtils.tryWithdrawTokens(model, price, TransactionType.PURCHASE)) {
            eliminateCenter(buyer);
        } else {
            NotificationUtils.sendNotification(buyer, MessageConstant.NOT_ENOUGH_MONEY, NotificationUtils.NotificationType.FAIL, false);
        }
    }

    @SuppressWarnings("deprecation")
    private void eliminateCenter(Player buyer) {
        PlayerModel buyerModel = PlayerUtils.getModelFromPlayer(buyer);
        Location center = ConfigManager.getInstance().getCenterLocation();
        for(Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2f, .7f);
            if(p.getLocation().distance(center) < 120) {
                PlayerModel model = PlayerUtils.getModelFromPlayer(p);
                if(model == null) continue;
                String buyerTeam = (String)buyerModel.getParameters().getOrDefault(PropertyConstant.PLAYER_TEAM_NAME, ".");
                String targetTeam = (String)model.getParameters().getOrDefault(PropertyConstant.PLAYER_TEAM_NAME, "_");
                if(buyerTeam.equals(targetTeam)) {
                    p.damage(10000000F);
                    continue;
                }
                EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(buyer, p, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, 100000F);
                getServer().getPluginManager().callEvent(e);
            }
        }
    }
}
