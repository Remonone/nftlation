package remonone.nftilation.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.events.OnTokenTransactionEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.utils.NestedObjectFetcher;

import java.util.Map;

public class OnTokenGainHandler implements Listener {
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onTransactionEvent(OnTokenTransactionEvent event) {
        if(!event.getTransactionType().equals(TransactionType.RESOURCE_GAIN)) {
            return;
        }
        float initialTokens = event.getTokensAmount();
        PlayerModel receiver = event.getPlayer();
        String teamName = (String)receiver.getParameters().get(PropertyConstant.PLAYER_TEAM_NAME);
        if(teamName == null) {
            return;
        }
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        Map<String, Object> params = team.getParameters();
        int level = (Integer)params.getOrDefault(PropertyConstant.TEAM_RESOURCE_INCOME, 0);
        Double additivePercent = (Double)NestedObjectFetcher.getNestedObject(MetaConstants.META_UPGRADES_PASSIVE, MetaConfig.getInstance().getUpgrades(), level);
        if(additivePercent == null) {
            additivePercent = 0D;
        }
        float additiveTokens = initialTokens * additivePercent.floatValue() / 100;
        event.setTokensAmount(initialTokens + additiveTokens);
    }
    
}
