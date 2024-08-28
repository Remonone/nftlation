package remonone.nftilation.handlers;

import org.apache.commons.lang.ObjectUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import remonone.nftilation.Store;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Map;

public class PlayerMoveEvent implements Listener {

    public final static RuleManager rules = RuleManager.getInstance();

    @EventHandler
    public void onPlayerMove(final org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(isNotAuthenticated(player) ||
                model != null && (isMoveForbidden() || isModelContainBlockers(model))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(final EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        if(isNotAuthenticated(player) ||
                (Store.getInstance().getGameStage().getStage().equals(Stage.IN_GAME) &&
                model != null && isMoveForbidden())) {
            
            event.setCancelled(true);
        }
    }

    private boolean isModelContainBlockers(PlayerModel model) {
        Map<String, Object> params = model.getParameters();
        if(params.containsKey(PropertyConstant.PLAYER_STUN_DURATION)) {
            long stunDuration = (Long) params.get(PropertyConstant.PLAYER_STUN_DURATION);
            if(stunDuration < System.currentTimeMillis()) {
                params.remove(PropertyConstant.PLAYER_STUN_DURATION);
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isMoveForbidden() {
        return !(Boolean)rules.getRuleOrDefault(RuleConstants.RULE_PLAYERS_ABLE_TO_MOVE, true);
    }

    private boolean isNotAuthenticated(final Player player) {
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        return ObjectUtils.equals(dataInstance.FindPlayerByName(player.getUniqueId()), null);
    }
}
