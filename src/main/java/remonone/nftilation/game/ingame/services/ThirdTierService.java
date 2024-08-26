package remonone.nftilation.game.ingame.services;

import org.bukkit.entity.Player;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.utils.PlayerUtils;

public class ThirdTierService implements IPurchasableService {
    
    @Override
    public String getServiceName() {
        return "third-tier";
    }

    @Override
    public void OnPurchase(Player buyer, float price) {
        PlayerInteractComponent playerInteract = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        if(playerInteract == null) return;
        PlayerModel model = PlayerUtils.getModelFromPlayer(buyer);
        if(model == null) return;
        if(PlayerInteractComponent.isPlayerNotAbleToUpgrade(buyer, 3)) return;
        if(playerInteract.adjustPlayerTokens(model, -price, TransactionType.PURCHASE)) {
            playerInteract.upgradePlayer(buyer, 3);
            PlayerUtils.updateShopInventoryForPlayer(buyer);
        }
    }
}
