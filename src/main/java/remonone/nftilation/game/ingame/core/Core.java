package remonone.nftilation.game.ingame.core;

import lombok.Getter;
import lombok.Setter;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.rules.RuleManager;

public class Core implements ICoreData{
    
    @Getter
    @Setter
    private int health;
    
    private final Runnable onDieFunction;
    
    public Core(Runnable onDie) {
        health = 100;
        this.onDieFunction = onDie;
    }
    
    public boolean TakeDamage(boolean isPlayerDamager) {
        if(health < 1) return false;
        int damage = 1;
        if(isPlayerDamager) damage = (int) RuleManager.getInstance().getRuleOrDefault(PropertyConstant.RULE_CORE_DAMAGE_INTAKE, 1);
        health -= damage;
        if(health < 1) {
            health = 0;
            Die();
            return true;
        }
        return false;
    }
    
    public boolean Heal() {
        if(health < 1 || health >= 100) return false;
        health++;
        return true;
    }
    
    
    private void Die() {
        this.onDieFunction.run();
    }
}
