package remonone.nftilation.game.ingame.services.teams;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.events.OnTokenTransactionEvent;
import remonone.nftilation.game.ingame.services.IPurchasableService;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.roles.Role;
import remonone.nftilation.utils.NestedObjectFetcher;
import remonone.nftilation.utils.PlayerUtils;

public class ItemUtilityService implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "utility-service";
    }

    @Override
    public void OnPurchase(Player buyer, int price) {
        ITeam team = PlayerUtils.getTeamFromPlayer(buyer);
        if(team == null) return;
        int currentLevel = (Integer) team.getParameters().getOrDefault(PropertyConstant.TEAM_UTILITY_ITEM_LEVEL, 0);
        if(!NestedObjectFetcher.containsExactLevelForPath(MetaConstants.META_UPGRADES_UTILITY, currentLevel + 1, MetaConfig.getInstance().getUpgrades())) {
            return;
        }
        if(!PlayerUtils.tryWithdrawTokens(buyer, price, OnTokenTransactionEvent.TransactionType.PURCHASE)) {
            buyer.sendMessage(MessageConstant.NOT_ENOUGH_MONEY);
            return;
        }
        team.getParameters().put(PropertyConstant.TEAM_UTILITY_ITEM_LEVEL, currentLevel + 1);
        String playerName = Store.getInstance().getDataInstance().FindPlayerByName(buyer.getUniqueId()).getData().getLogin();
        buyer.closeInventory();
        team.getPlayers().forEach(playerModel -> {
            Player player = playerModel.getReference();
            player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, .7f, .4f);
            player.sendMessage(ChatColor.WHITE + playerName + ChatColor.GOLD + MessageConstant.TEAM_UPGRADE + MessageConstant.TEAM_UPGRADE_UTILITY + " " + currentLevel);
            Role.refillInventoryWithItems(playerModel);
        });
    }
}
