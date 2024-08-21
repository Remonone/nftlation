package remonone.nftilation.game.ingame.services.teams.resource;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.events.OnTokenTransactionEvent;
import remonone.nftilation.game.ingame.services.IPurchasableService;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.utils.NestedObjectFetcher;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Map;

public class SecondResourceIncomeService implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "resource-income-second";
    }

    @Override
    public void OnPurchase(Player buyer, int price) {
        ITeam team = PlayerUtils.getTeamFromPlayer(buyer);
        if(team == null) return;
        Double currentValue = (Double) team.getParameters().get(PropertyConstant.TEAM_RESOURCE_INCOME);
        Double requiredValue = (Double) NestedObjectFetcher.getNestedObject(MetaConstants.META_UPGRADES_RESOURCE, MetaConfig.getInstance().getUpgrades(), 0);
        if(requiredValue == null) {
            requiredValue = 0D;
        }
        if(currentValue == null || currentValue - DataConstants.ZERO_THRESHOLD > requiredValue) {
            buyer.closeInventory();
            return;
        }
        if(!PlayerUtils.tryWithdrawTokens(buyer, price, OnTokenTransactionEvent.TransactionType.PURCHASE)) {
            buyer.sendMessage(MessageConstant.NOT_ENOUGH_MONEY);
            return;
        }
        Map<String, Object> params = team.getParameters();
        Double nextLevelValue = (Double) NestedObjectFetcher.getNestedObject(MetaConstants.META_UPGRADES_RESOURCE, MetaConfig.getInstance().getUpgrades(), 1);
        params.put(PropertyConstant.TEAM_RESOURCE_INCOME, nextLevelValue);
        buyer.closeInventory();
        String playerName = Store.getInstance().getDataInstance().FindPlayerByName(buyer.getUniqueId()).getData().getLogin();
        team.getPlayers().forEach(playerModel -> {
            Player player = playerModel.getReference();
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 1f, 1f);
            player.sendMessage(ChatColor.WHITE + playerName + ChatColor.GOLD + MessageConstant.TEAM_UPGRADE + MessageConstant.TEAM_UPGRADE_RESOURCE_INCOME + " " + 1);
        });
    }
}
