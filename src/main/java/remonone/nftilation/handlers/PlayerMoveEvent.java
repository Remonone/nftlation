package remonone.nftilation.handlers;

import org.apache.commons.lang.ObjectUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import remonone.nftilation.Store;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.PlayerUtils;

public class PlayerMoveEvent implements Listener {

    public static RuleManager rules = RuleManager.getInstance();

    @EventHandler
    public void onPlayerMove(final org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(isNotAuthenticated(player) || (PlayerUtils.getModelFromPlayer(player) != null &&  isMoveRuleSet())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(final EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if(isNotAuthenticated(player) ||
                !Store.getInstance().getGameStage().getStage().equals(Stage.IN_GAME) ||
                (PlayerUtils.getModelFromPlayer(player) != null && isMoveRuleSet())) {
            event.setCancelled(true);
        }
    }

    private boolean isMoveRuleSet() {
        return (Boolean)rules.getRuleOrDefault(PropertyConstant.RULE_PLAYERS_ABLE_TO_MOVE, true);
    }

    private boolean isNotAuthenticated(final Player player) {
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        return ObjectUtils.equals(dataInstance.FindPlayerByName(player.getUniqueId()), null);
    }
}
