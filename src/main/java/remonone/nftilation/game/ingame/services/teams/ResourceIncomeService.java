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

import java.util.Map;

public class ResourceIncomeService implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "resource-income";
    }

    @Override
    public void OnPurchase(Player buyer, float price) {
        ITeam team = PlayerUtils.getTeamFromPlayer(buyer);
        if(team == null) return;
        Integer currentLevel = (Integer) team.getParameters().getOrDefault(PropertyConstant.TEAM_RESOURCE_INCOME, 0);
        if(!NestedObjectFetcher.containsExactLevelForPath(MetaConstants.META_UPGRADES_RESOURCE, ++currentLevel, MetaConfig.getInstance().getUpgrades())) {
            return;
        }
        if(!PlayerUtils.tryWithdrawTokens(buyer, price, TransactionType.PURCHASE)) {
            NotificationUtils.sendNotification(buyer, MessageConstant.NOT_ENOUGH_MONEY, NotificationUtils.NotificationType.FAIL, false);
            return;
        }
        Map<String, Object> params = team.getParameters();
        params.put(PropertyConstant.TEAM_RESOURCE_INCOME, currentLevel);
        String playerName = Store.getInstance().getDataInstance().FindPlayerByName(buyer.getUniqueId()).getData().getLogin();
        Integer finalCurrentLevel = currentLevel;
        team.getPlayers().forEach(playerModel -> {
            Player player = playerModel.getReference();
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 1f, 1f);
            player.sendMessage(ChatColor.WHITE + playerName + ChatColor.GOLD + MessageConstant.TEAM_UPGRADE + MessageConstant.TEAM_UPGRADE_RESOURCE_INCOME + " " + finalCurrentLevel);
            PlayerUtils.updateShopInventoryForPlayer(player);
        });
    }
}
