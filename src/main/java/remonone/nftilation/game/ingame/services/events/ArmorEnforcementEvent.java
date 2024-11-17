package remonone.nftilation.game.ingame.services.events;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.services.IPurchasableService;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.utils.NotificationUtils;
import remonone.nftilation.utils.PlayerUtils;

import java.util.List;

public class ArmorEnforcementEvent implements IPurchasableService {

    @Override
    public String getServiceName() {
        return "armor-enforcement";
    }

    @Override
    public void OnPurchase(Player buyer, float price) {
        PlayerModel model = PlayerUtils.getModelFromPlayer(buyer);
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        if(PlayerUtils.tryWithdrawTokens(buyer, price, TransactionType.PURCHASE)) {
            adjustTeamToughnessForPlayer(model);
        } else {
            NotificationUtils.sendNotification(buyer, MessageConstant.NOT_ENOUGH_MONEY, NotificationUtils.NotificationType.FAIL, false);
        }
    }

    private void adjustTeamToughnessForPlayer(PlayerModel model) {
        String teamName = (String) model.getParameters().get(PropertyConstant.PLAYER_TEAM_NAME);
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        List<PlayerModel> models = team.getPlayers();
        for(PlayerModel p : models) {
            Player player = p.getReference();
            p.getParameters().put(PropertyConstant.PLAYER_ARMOR_ENFORCEMENT, System.currentTimeMillis() + 60 * DataConstants.ONE_SECOND);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1F, 1F);
            player.sendMessage(PlayerUtils.getOriginalPlayerName(model.getReference()) + ChatColor.GOLD + MessageConstant.EVENT_PURCHASE + "Усиление брони");
        }
        resetEnforcementAfterTime(team);
    }

    private void resetEnforcementAfterTime(ITeam team) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(PlayerModel model : team.getPlayers()) {
                    model.getParameters().remove(PropertyConstant.PLAYER_ARMOR_ENFORCEMENT);
                    model.getReference().playSound(model.getReference().getLocation(), Sound.BLOCK_ANVIL_FALL, 1F, 1F);
                }
            }
        }.runTaskLater(Nftilation.getInstance(), DataConstants.TICKS_IN_MINUTE);
    }
}
