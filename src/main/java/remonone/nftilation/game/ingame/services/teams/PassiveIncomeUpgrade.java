package remonone.nftilation.game.ingame.services.teams;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.ingame.services.IPurchasableService;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.utils.NestedObjectFetcher;
import remonone.nftilation.utils.NotificationUtils;
import remonone.nftilation.utils.PlayerUtils;

public class PassiveIncomeUpgrade implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "passive-income";
    }

    @Override
    public void OnPurchase(Player buyer, float price) {
        ITeam team = PlayerUtils.getTeamFromPlayer(buyer);
        if(team == null) return;
        int currentLevel = (Integer) team.getParameters().getOrDefault(PropertyConstant.TEAM_PASSIVE_INCOME, 0);
        if(!NestedObjectFetcher.containsExactLevelForPath(MetaConstants.META_UPGRADES_PASSIVE, ++currentLevel, MetaConfig.getInstance().getUpgrades())) {
            return;
        }
        if(!PlayerUtils.tryWithdrawTokens(buyer, price, TransactionType.PURCHASE)) {
            NotificationUtils.sendNotification(buyer, MessageConstant.NOT_ENOUGH_MONEY, NotificationUtils.NotificationType.FAIL, false);
            return;
        }
        team.getParameters().put(PropertyConstant.TEAM_PASSIVE_INCOME, currentLevel);
        String playerName = Store.getInstance().getDataInstance().FindPlayerByID(buyer.getUniqueId()).getData().getLogin();
        int finalCurrentLevel = currentLevel;
        team.getPlayers().forEach(playerModel -> {
            Player player = playerModel.getReference();
            player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1f, 2f);
            player.sendMessage(ChatColor.WHITE + playerName + ChatColor.GOLD + MessageConstant.TEAM_UPGRADE + MessageConstant.TEAM_UPGRADE_PASSIVE_INCOME + " " + finalCurrentLevel);
            PlayerUtils.updateShopInventoryForPlayer(player);
        });
    }
}
