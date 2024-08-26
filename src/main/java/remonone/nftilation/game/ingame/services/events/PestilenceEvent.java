package remonone.nftilation.game.ingame.services.events;

import org.bukkit.entity.Player;
import remonone.nftilation.game.ingame.services.IPurchasableService;

public class PestilenceEvent implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "pestilence-event";
    }

    @Override
    public void OnPurchase(Player buyer, float price) {

    }
}
