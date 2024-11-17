package remonone.nftilation.game.ingame.services.teams;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
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


public class CoreUpgradeService implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "upgrade-core";
    }

    @Override
    public void OnPurchase(Player buyer, float price) {
        ITeam team = PlayerUtils.getTeamFromPlayer(buyer);
        if(team == null) return;
        int currentLevel = (Integer) team.getParameters().getOrDefault(PropertyConstant.TEAM_CORE_BLOCK, 0);
        if(!NestedObjectFetcher.containsExactLevelForPath(MetaConstants.META_UPGRADES_CORE, ++currentLevel, MetaConfig.getInstance().getUpgrades())) {
            return;
        }
        if(!PlayerUtils.tryWithdrawTokens(buyer, price, TransactionType.PURCHASE)) {
            NotificationUtils.sendNotification(buyer, MessageConstant.NOT_ENOUGH_MONEY, NotificationUtils.NotificationType.FAIL, false);
            return;
        }
        String nextLevelMaterial = (String) NestedObjectFetcher.getNestedObject(MetaConstants.META_UPGRADES_CORE, MetaConfig.getInstance().getUpgrades(), currentLevel);
        team.getParameters().put(PropertyConstant.TEAM_CORE_BLOCK, currentLevel);
        Material material = Material.getMaterial(nextLevelMaterial);
        Vector corePos = team.getTeamSpawnPoint().getCoreCenter();
        Location location = new Location(buyer.getWorld(), corePos.getX(), corePos.getY(), corePos.getZ());
        location.getBlock().setType(material);
        String playerName = Store.getInstance().getDataInstance().FindPlayerByName(buyer.getUniqueId()).getData().getLogin();
        int finalCurrentLevel = currentLevel;
        team.getPlayers().forEach(playerModel -> {
            Player player = playerModel.getReference();
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, .8f, .6f);
            player.sendMessage(ChatColor.WHITE + playerName + ChatColor.GOLD + MessageConstant.TEAM_UPGRADE + MessageConstant.TEAM_UPGRADE_CORE + " " + finalCurrentLevel);
        });
    }
}
