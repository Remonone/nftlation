package remonone.nftilation.game.ingame.services.teams.core;

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
import remonone.nftilation.events.OnTokenTransactionEvent;
import remonone.nftilation.game.ingame.services.IPurchasableService;
import remonone.nftilation.game.meta.MetaConfig;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.utils.NestedObjectFetcher;
import remonone.nftilation.utils.PlayerUtils;


public class ThirdTierCoreService implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "third-tier-core";
    }

    @Override
    public void OnPurchase(Player buyer, int price) {
        ITeam team = PlayerUtils.getTeamFromPlayer(buyer);
        if(team == null) return;
        Integer currentLevel = (Integer) team.getParameters().get(PropertyConstant.TEAM_CORE_BLOCK);
        if(currentLevel != 1) {
            buyer.closeInventory();
            return;
        }
        if(!PlayerUtils.tryWithdrawTokens(buyer, price, OnTokenTransactionEvent.TransactionType.PURCHASE)) {
            buyer.sendMessage(MessageConstant.NOT_ENOUGH_MONEY);
            return;
        }
        String nextLevelMaterial = (String) NestedObjectFetcher.getNestedObject(MetaConstants.META_UPGRADES_CORE, MetaConfig.getInstance().getUpgrades(), 2);
        team.getParameters().put(PropertyConstant.TEAM_CORE_BLOCK, 2);
        Material material = Material.getMaterial(nextLevelMaterial);
        Vector corePos = team.getTeamSpawnPoint().getCoreCenter();
        Location location = new Location(buyer.getWorld(), corePos.getX(), corePos.getY(), corePos.getZ());
        location.getBlock().setType(material);
        String playerName = Store.getInstance().getDataInstance().FindPlayerByName(buyer.getUniqueId()).getData().getLogin();
        buyer.closeInventory();
        team.getPlayers().forEach(playerModel -> {
            Player player = playerModel.getReference();
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, .8f, .6f);
            player.sendMessage(ChatColor.WHITE + playerName + ChatColor.GOLD + MessageConstant.TEAM_UPGRADE + MessageConstant.TEAM_UPGRADE_CORE + " " + 2);
        });
    }
}
