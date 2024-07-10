package remonone.nftilation.game.ingame.services;

import org.bukkit.entity.Player;
import remonone.nftilation.game.GameInstance;

public class ThirdTierService implements IPurchasableService {
    
    @Override
    public String getServiceName() {
        return "third-tier";
    }

    @Override
    public void OnPurchase(Player buyer, int price) {
        GameInstance.getInstance().upgradePlayer(buyer, 3, price);
    }
}
