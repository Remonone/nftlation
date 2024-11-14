package remonone.nftilation.game.ingame.services.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
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
import remonone.nftilation.utils.MathUtils;
import remonone.nftilation.utils.PlayerUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArmorEnforcementEvent implements IPurchasableService {

    private static final Map<UUID, ArmorEnforcementUnit> enforcementUnits = new HashMap<>();

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
            buyer.sendMessage(MessageConstant.NOT_ENOUGH_MONEY);
        }
    }

    private void adjustTeamToughnessForPlayer(PlayerModel model) {
        String teamName = (String) model.getParameters().get(PropertyConstant.PLAYER_TEAM_NAME);
        ITeam team = GameInstance.getInstance().getTeam(teamName);
        List<PlayerModel> models = team.getPlayers();
        Map<UUID, Double> armorMap = new HashMap<>();
        for(PlayerModel p : models) {
            armorMap.put(p.getReference().getUniqueId(), p.getReference().getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue());
        }
        enforcementUnits.put(team.getTeamID(), new ArmorEnforcementUnit(armorMap));
        for(PlayerModel p : models) {
            double currentEnforcement = enforcementUnits.get(team.getTeamID()).playerEnforcementMap.get(p.getReference().getUniqueId());
            p.getReference().getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(MathUtils.boundValues(currentEnforcement / 0.2, 0D, 20D));
            Player player = p.getReference();
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1F, 1F);
            player.sendMessage(PlayerUtils.getOriginalPlayerName(model.getReference()) + ChatColor.GOLD + MessageConstant.EVENT_PURCHASE + "Усиление брони");
        }
        resetEnforcementAfterTime(team);
    }

    private void resetEnforcementAfterTime(ITeam team) {
        ArmorEnforcementUnit unit = enforcementUnits.get(team.getTeamID());
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Map.Entry<UUID, Double> player : unit.playerEnforcementMap.entrySet()) {
                    Player p = Bukkit.getPlayer(player.getKey());
                    p.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(player.getValue());
                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
                }
            }
        }.runTaskLater(Nftilation.getInstance(), DataConstants.TICKS_IN_MINUTE);
    }

    private static class ArmorEnforcementUnit {
        Map<UUID, Double> playerEnforcementMap;
        public ArmorEnforcementUnit(Map<UUID, Double> playerEnforcementMap) {
            this.playerEnforcementMap = playerEnforcementMap;
        }
    }
}
