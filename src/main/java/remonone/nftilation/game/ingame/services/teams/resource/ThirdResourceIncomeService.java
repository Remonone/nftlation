package remonone.nftilation.game.ingame.services.teams.resource;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.events.OnTokenTransactionEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.services.IPurchasableService;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Map;

public class ThirdResourceIncomeService implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "resource-income-third";
    }

    @Override
    public void OnPurchase(Player buyer, int price) {
        PlayerModel model = PlayerUtils.getModelFromPlayer(buyer);
        if(model == null) return;
        PlayerInteractComponent component = (PlayerInteractComponent) GameInstance.getComponentByName(NameConstants.PLAYER_INTERACT_NAME);
        boolean isTransactionSuccessful = component.adjustPlayerTokens(model, -price, OnTokenTransactionEvent.TransactionType.PURCHASE);
        if(!isTransactionSuccessful) {
            buyer.sendMessage(MessageConstant.NOT_ENOUGH_MONEY);
            return;
        }
        String teamName = (String) model.getParameters().get(PropertyConstant.PLAYER_TEAM_NAME);
        if(teamName == null) return;
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        if(team == null) return;
        Map<String, Object> params = team.getParameters();
        params.put(PropertyConstant.TEAM_RESOURCE_INCOME, 60D);
        buyer.closeInventory();
        team.getPlayers().forEach(playerModel -> playerModel.getReference().playSound(playerModel.getReference().getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 1f, 1f));
    }
}
