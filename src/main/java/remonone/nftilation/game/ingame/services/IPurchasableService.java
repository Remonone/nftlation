package remonone.nftilation.game.ingame.services;

import org.bukkit.entity.Player;

public interface IPurchasableService {
    
    String getServiceName();
    void OnPurchase(Player buyer, int price);
    
}
