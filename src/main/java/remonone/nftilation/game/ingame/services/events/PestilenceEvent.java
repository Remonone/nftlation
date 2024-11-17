package remonone.nftilation.game.ingame.services.events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.damage.PestilenceDamageHandler;
import remonone.nftilation.game.ingame.services.IPurchasableService;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.models.TransactionType;
import remonone.nftilation.utils.NotificationUtils;
import remonone.nftilation.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PestilenceEvent implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "pestilence-event";
    }

    @Override
    public void OnPurchase(Player buyer, float price) {
        if(!PlayerUtils.tryWithdrawTokens(buyer, price, TransactionType.PURCHASE)) {
            NotificationUtils.sendNotification(buyer, MessageConstant.NOT_ENOUGH_MONEY, NotificationUtils.NotificationType.FAIL, false);
            return;
        }
        List<PlayerModel> enemyModels = getEnemyModels(buyer);
        spreadPestilence(buyer, enemyModels);
    }

    private void spreadPestilence(Player buyer, List<PlayerModel> enemyModels) {
        for(PlayerModel model : enemyModels) {
            model.getReference().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 2 * DataConstants.TICKS_IN_MINUTE, 1, true, true));
            model.getReference().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5, 10, false, false));
            model.getDamageHandlers().add(new PestilenceDamageHandler(buyer));
            model.getReference().sendMessage(ChatColor.DARK_GREEN + MessageConstant.PESTILENCE_START);
        }
    }

    private List<PlayerModel> getEnemyModels(Player buyer) {
        PlayerModel model = PlayerUtils.getModelFromPlayer(buyer);
        Map<String, Object> params = model.getParameters();
        String teamName = (String) params.get(PropertyConstant.PLAYER_TEAM_NAME);
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        List<PlayerModel> teams = new ArrayList<>();
        Iterator<ITeam> teamsIt = GameInstance.getInstance().getTeamIterator();
        while(teamsIt.hasNext()) {
            ITeam t = teamsIt.next();
            if(t.getTeamName().equals(team.getTeamName())) continue;
            teams.addAll(t.getPlayers());
        }
        return teams;
    }

}
