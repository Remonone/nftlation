package remonone.nftilation.game.ingame.services;

import org.bukkit.entity.Player;
import remonone.nftilation.game.GameInstance;

public class SecondTierService implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "second-tier";
    }

    @Override
    public void OnPurchase(Player buyer, int price) {
        GameInstance.getInstance().upgradePlayer(buyer, 2, price);
    }
}
