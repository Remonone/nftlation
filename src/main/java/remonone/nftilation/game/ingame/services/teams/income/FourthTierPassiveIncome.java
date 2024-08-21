package remonone.nftilation.game.ingame.services.teams.income;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.components.PlayerInteractComponent;
import remonone.nftilation.constants.*;
import remonone.nftilation.events.OnTokenTransactionEvent;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.services.IPurchasableService;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.NestedObjectFetcher;
import remonone.nftilation.utils.PlayerUtils;

public class FourthTierPassiveIncome implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "passive-income-fourth";
    }

    @Override
    public void OnPurchase(Player buyer, int price) {
        ITeam team = PlayerUtils.getTeamFromPlayer(buyer);
        if(team == null) return;
        Double currentValue = (Double) team.getParameters().get(PropertyConstant.TEAM_PASSIVE_INCOME);
        Double requiredValue = (Double) NestedObjectFetcher.getNestedObject(MetaConstants.META_UPGRADES_PASSIVE, MetaConfig.getInstance().getUpgrades(), 2);
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
        Double nextLevelValue = (Double) NestedObjectFetcher.getNestedObject(MetaConstants.META_UPGRADES_PASSIVE, MetaConfig.getInstance().getUpgrades(), 3);
        team.getParameters().put(PropertyConstant.TEAM_PASSIVE_INCOME, nextLevelValue);
        String playerName = Store.getInstance().getDataInstance().FindPlayerByName(buyer.getUniqueId()).getData().getLogin();
        buyer.closeInventory();
        team.getPlayers().forEach(playerModel -> {
            Player player = playerModel.getReference();
            player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1f, 2f);
            player.sendMessage(ChatColor.WHITE + playerName + ChatColor.GOLD + MessageConstant.TEAM_UPGRADE + MessageConstant.TEAM_UPGRADE_PASSIVE_INCOME + " " + 3);
        });
    }
}
