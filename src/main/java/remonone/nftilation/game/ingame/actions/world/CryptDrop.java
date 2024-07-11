package remonone.nftilation.game.ingame.actions.world;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.ingame.actions.IAction;
import remonone.nftilation.game.rules.RuleManager;

import java.util.Map;

public class CryptDrop implements IAction {
    @Override
    public void Init(Map<String, Object> params) {
        RuleManager.getInstance().setRule(PropertyConstant.RULE_RESOURCE_RESPAWNABLE, false);
        long timeToSpawnAgain = System.currentTimeMillis() + 2 * DataConstants.ONE_MINUTE;
        RuleManager.getInstance().setRule(PropertyConstant.RULE_RESOURCE_SPAWN_AUTO_ENABLE_AT, timeToSpawnAgain);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                RuleManager.getInstance().setRule(PropertyConstant.RULE_RESOURCE_RESPAWNABLE, true);
            }
        };
        task.runTaskLater(Nftilation.getInstance(), 2 * DataConstants.TICKS_IN_MINUTE);
    }

    @Override
    public String getTitle() {
        return ChatColor.DARK_RED + "" + ChatColor.BOLD + "Crypt drop";
    }

    @Override
    public String getDescription() {
        return ChatColor.GOLD + "Экономика есть искусство удовлетворять безграничные потребности при помощи ограниченных ресурсов";
    }

    @Override
    public Sound getSound() {
        return Sound.ENTITY_VILLAGER_NO;
    }
}
