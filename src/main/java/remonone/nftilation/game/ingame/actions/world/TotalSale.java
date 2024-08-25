package remonone.nftilation.game.ingame.actions.world;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Map;

public class TotalSale implements IAction {
    @Override
    public void Init(Map<String, Object> params) {
        Logger.log("Starting an " + getClass().getSimpleName() + " event...");
        RuleManager.getInstance().setRule(PropertyConstant.RULE_PRICE_SCALE, .9F);
        Bukkit.getOnlinePlayers().forEach(PlayerUtils::updateShopInventoryForPlayer);
        new BukkitRunnable() {
            @Override
            public void run() {
                RuleManager.getInstance().setRule(PropertyConstant.RULE_PRICE_SCALE, 1F);
                Bukkit.getOnlinePlayers().forEach(PlayerUtils::updateShopInventoryForPlayer);
            }
        }.runTaskLater(Nftilation.getInstance(), 30 * DataConstants.TICKS_IN_SECOND);
    }

    @Override
    public String getTitle() {
        return "Тотальная распродажа";
    }

    @Override
    public String getDescription() {
        return "Я спрашиваю вас: хотите ли вы тотальной распродажи?";
    }

    @Override
    public Sound getSound() {
        return Sound.ENTITY_VILLAGER_YES;
    }
}
