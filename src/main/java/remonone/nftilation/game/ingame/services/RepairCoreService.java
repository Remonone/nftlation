package remonone.nftilation.game.ingame.services;

import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.game.GameInstance;

public class RepairCoreService implements IPurchasableService {
    
    @Override
    public String getServiceName() {
        return "base-repair";
    }

    @Override
    public void OnPurchase(Player buyer, float price) {
        String team = Store.getInstance().getDataInstance().getPlayerTeam(buyer.getUniqueId());
        GameInstance.getInstance().healCore(buyer, team, price);
    }
}
