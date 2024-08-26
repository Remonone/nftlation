package remonone.nftilation.game.ingame.services.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import remonone.nftilation.config.ConfigManager;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.ingame.services.IPurchasableService;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.utils.EntityDamageByPlayerLog;
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
        }
    }

    @SuppressWarnings("deprecation")
    private void eliminateCenter(Player buyer) {
        PlayerModel buyerModel = PlayerUtils.getModelFromPlayer(buyer);
        Location center = ConfigManager.getInstance().getCenterLocation();
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(p.getLocation().distance(center) < 120) {
                PlayerModel model = PlayerUtils.getModelFromPlayer(p);
                if(model == null) continue;
                String buyerTeam = (String)buyerModel.getParameters().getOrDefault(PropertyConstant.PLAYER_TEAM_NAME, ".");
                String targetTeam = (String)model.getParameters().getOrDefault(PropertyConstant.PLAYER_TEAM_NAME, "_");
                if(buyerTeam.equals(targetTeam)) {
                    EntityDamageEvent e = new EntityDamageEvent(p, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, 1000000F);
                    EntityDamageByPlayerLog.removeLogEvent(p.getUniqueId());
                    Bukkit.getPluginManager().callEvent(e);
                    return;
                }
                EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(buyer, p, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, 100000F);
                getServer().getPluginManager().callEvent(e);
            }
        }
    }
}
