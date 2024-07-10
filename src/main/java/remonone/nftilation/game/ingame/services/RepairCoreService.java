package remonone.nftilation.game.ingame.services;

import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.game.GameInstance;

public class RepairCoreService implements IPurchasableService {
    
    @Override
    public String getServiceName() {
        return "base-repair";
    }

    @Override
    public void OnPurchase(Player buyer, int price) {
        PlayerData data = Store.getInstance().getDataInstance().FindPlayerByName(buyer.getName());
        GameInstance.getInstance().healCore(buyer, data.getTeam().getTeamName(), price);
    }
}
