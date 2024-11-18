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
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.game.roles.RoleItemDispenser;
import remonone.nftilation.utils.NestedObjectFetcher;
import remonone.nftilation.utils.NotificationUtils;
import remonone.nftilation.utils.PlayerUtils;

public class ItemUtilityService implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "utility-service";
    }

    @Override
    public void OnPurchase(Player buyer, float price) {
        ITeam team = PlayerUtils.getTeamFromPlayer(buyer);
        if(team == null) return;
        int currentLevel = (Integer) team.getParameters().getOrDefault(PropertyConstant.TEAM_UTILITY_ITEM_LEVEL, 0);
        if(!NestedObjectFetcher.containsExactLevelForPath(MetaConstants.META_UPGRADES_UTILITY + RoleItemDispenser.ItemType.SWORD.getName(), ++currentLevel, MetaConfig.getInstance().getUpgrades())) {
            return;
        }
        if(!PlayerUtils.tryWithdrawTokens(buyer, price, TransactionType.PURCHASE)) {
            NotificationUtils.sendNotification(buyer, MessageConstant.NOT_ENOUGH_MONEY, NotificationUtils.NotificationType.FAIL, false);
            return;
        }
        team.getParameters().put(PropertyConstant.TEAM_UTILITY_ITEM_LEVEL, currentLevel);
        String playerName = Store.getInstance().getDataInstance().FindPlayerByName(buyer.getUniqueId()).getData().getLogin();
        
        int finalCurrentLevel = currentLevel;
        team.getPlayers().forEach(playerModel -> {
            Player player = playerModel.getReference();
            player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, .7f, .4f);
            player.sendMessage(ChatColor.WHITE + playerName + ChatColor.GOLD + MessageConstant.TEAM_UPGRADE + MessageConstant.TEAM_UPGRADE_UTILITY + " " + finalCurrentLevel);
            Role.setInventoryItems(playerModel);
            PlayerUtils.updateShopInventoryForPlayer(player);
        });
    }
}
