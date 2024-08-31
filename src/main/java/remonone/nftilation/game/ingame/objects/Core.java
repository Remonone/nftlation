package remonone.nftilation.game.ingame.objects;

import lombok.Getter;
import lombok.Setter;
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.game.rules.RuleManager;

@Setter
public class Core implements ICoreData{
    
    @Getter
    private int health;

    private Runnable onDieFunction;
    
    public Core(Runnable onDie) {
        health = 100;
        this.onDieFunction = onDie;
    }
    
    public boolean TakeDamage(boolean isPlayerDamager) {
        if(health < 1) return false;
        int damage = 1;
        if(isPlayerDamager) damage = (int) RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_CORE_DAMAGE_INTAKE, 1);
        health -= damage;
        if(health < 1) {
            health = 0;
            Die();
            return true;
        }
        return false;
    }
    
    public boolean isCoreCannotBeHealed() {
        return health < 1 || health >= 100;
    }
    
    public void Heal() {
        if(isCoreCannotBeHealed()) return;
        health++;
    }
    
    
    private void Die() {
        this.onDieFunction.run();
    }
}
