package remonone.nftilation.game.rules;

import lombok.Setter;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;

import java.util.HashMap;
import java.util.Map;

@Setter
public class RuleManager {

    private static RuleManager instance;
    
    private final Map<String, Object> inGameRules = new HashMap<>();

    public static RuleManager getInstance() {
        if(instance == null) {
            instance = new RuleManager();
        }
        return instance;
    }
    
    public RuleManager() {
        inGameRules.put(PropertyConstant.RULE_CORE_INVULNERABLE, true);
        inGameRules.put(PropertyConstant.RULE_RESOURCE_RESPAWNABLE, true);
        inGameRules.put(PropertyConstant.RULE_CORE_SELF_DESTRUCTIVE, false);
        inGameRules.put(PropertyConstant.RULE_INVENTORY_AUTO_CLEAR, true);
        inGameRules.put(PropertyConstant.RULE_RESPAWN_TIMER, (long) 5 * 20);
        inGameRules.put(PropertyConstant.RULE_CORE_HEALTH_LOST_PERIOD, (long) 9 * DataConstants.TICKS_IN_SECOND);
        inGameRules.put(PropertyConstant.RULE_AVAILABLE_TIER, 1);
        inGameRules.put(PropertyConstant.RULE_CORE_DAMAGE_INTAKE, 2);
        inGameRules.put(PropertyConstant.RULE_IMMINENT_DEATH, false);
        inGameRules.put(PropertyConstant.RULE_PLAYERS_ABLE_TO_MOVE, true);
        inGameRules.put(PropertyConstant.RULE_GAME_IS_RUNNING, true);
    }
    
    public Object getRuleOrDefault(String rule, Object defaultValue) {
        return inGameRules.getOrDefault(rule, defaultValue);
    }
    public void setRule(String rule, Object value) {
        inGameRules.put(rule, value);
    }
}
