package remonone.nftilation.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.events.OnPhaseUpdateEvent;
import remonone.nftilation.game.ingame.actions.ActionContainer;
import remonone.nftilation.game.ingame.actions.ActionType;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.Logger;

import java.util.HashMap;

public class PhaseUpdateHandler implements Listener {
    
    @EventHandler
    public void onPhaseUpdate(final OnPhaseUpdateEvent event) {
        int stage = event.getPhaseStage();
        
        switch (stage) {
            case 1: {
                Logger.broadcast("Stage 1 has begun!");
                break;
            }
            case 2: {
                Logger.broadcast("Stage 2 has begun!");
                RuleManager.getInstance().setRule(PropertyConstant.RULE_AVAILABLE_TIER, 2);
                break;
            }
            case 3: {
                Logger.broadcast("Stage 3 has begun!");
                RuleManager.getInstance().setRule(PropertyConstant.RULE_CORE_INVULNERABLE, false);
                break;
            }
            case 4: {
                Logger.broadcast("Stage 4 has begun!");
                RuleManager.getInstance().setRule(PropertyConstant.RULE_AVAILABLE_TIER, 3);
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        ActionContainer.InitAction(ActionType.CRYPT_DROP, new HashMap<>());
                    }
                };
                runnable.runTaskLater(Nftilation.getInstance(), 100);
                break;
            }
            case 5: {
                Logger.broadcast("Stage 5 has begun!");
                RuleManager.getInstance().setRule(PropertyConstant.RULE_CORE_SELF_DESTRUCTIVE, true);
                RuleManager.getInstance().setRule(PropertyConstant.RULE_INVENTORY_AUTO_CLEAR, false);
                break;
            }
        }
    }
}
