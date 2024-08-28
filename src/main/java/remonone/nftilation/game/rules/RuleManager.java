package remonone.nftilation.game.rules;

import lombok.Setter;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.RuleConstants;

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
        inGameRules.put(RuleConstants.RULE_CORE_INVULNERABLE, true);
        inGameRules.put(RuleConstants.RULE_RESOURCE_RESPAWNABLE, true);
        inGameRules.put(RuleConstants.RULE_CORE_SELF_DESTRUCTIVE, false);
        inGameRules.put(RuleConstants.RULE_INVENTORY_AUTO_CLEAR, true);
        inGameRules.put(RuleConstants.RULE_RESPAWN_TIMER, (long) 5 * 20);
        inGameRules.put(RuleConstants.RULE_CORE_HEALTH_LOST_PERIOD, (long) 9 * DataConstants.TICKS_IN_SECOND);
        inGameRules.put(RuleConstants.RULE_AVAILABLE_TIER, 1);
        inGameRules.put(RuleConstants.RULE_CORE_DAMAGE_INTAKE, 2);
        inGameRules.put(RuleConstants.RULE_IMMINENT_DEATH, false);
        inGameRules.put(RuleConstants.RULE_PLAYERS_ABLE_TO_MOVE, true);
        inGameRules.put(RuleConstants.RULE_GAME_IS_RUNNING, true);
        inGameRules.put(RuleConstants.RULE_PRICE_SCALE, 1F);
    }
    
    public Object getRuleOrDefault(String rule, Object defaultValue) {
        return inGameRules.getOrDefault(rule, defaultValue);
    }
    public void setRule(String rule, Object value) {
        inGameRules.put(rule, value);
    }
}
