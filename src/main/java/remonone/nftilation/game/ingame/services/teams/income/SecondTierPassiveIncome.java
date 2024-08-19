package remonone.nftilation.game.ingame.services.teams.income;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.ingame.services.IPurchasableService;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.PlayerUtils;

public class SecondTierPassiveIncome implements IPurchasableService {
    @Override
    public String getServiceName() {
        return "passive-income-second";
    }

    @Override
    public void OnPurchase(Player buyer, int price) {
        PlayerModel model = PlayerUtils.getModelFromPlayer(buyer);
        if(model == null) return;
        String teamName = (String) model.getParameters().get(PropertyConstant.PLAYER_TEAM_NAME);
        if(StringUtils.isBlank(teamName)) return;
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        if(team == null) return;
        Double currentValue = (Double) team.getParameters().get(PropertyConstant.TEAM_PASSIVE_INCOME);
        if(currentValue == null || currentValue - DataConstants.ZERO_THRESHOLD > 0) {
            buyer.closeInventory();
            return;
        }
        team.getParameters().put(PropertyConstant.TEAM_PASSIVE_INCOME, 3D);
        String playerName = Store.getInstance().getDataInstance().FindPlayerByName(buyer.getUniqueId()).getData().getLogin();
        team.getPlayers().forEach(playerModel -> {
            Player player = playerModel.getReference();
            player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1f, 2f);
            player.sendMessage(ChatColor.WHITE + playerName + ChatColor.GOLD + MessageConstant.TEAM_UPGRADE + MessageConstant.TEAM_UPGRADE_PASSIVE_INCOME + 1);
        });
    }
}
