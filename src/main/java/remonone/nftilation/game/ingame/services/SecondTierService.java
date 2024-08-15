package remonone.nftilation.game.ingame.services;

import org.bukkit.entity.Player;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.game.GameInstance;

public class SecondTierService implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "second-tier";
    }

    @Override
    public void OnPurchase(Player buyer, int price) {
        PlayerInteractComponent playerInteract = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        if(playerInteract == null) return;
        playerInteract.upgradePlayer(buyer, 2, price);
    }
}
