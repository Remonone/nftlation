package remonone.nftilation.game.ingame.core;

import lombok.Getter;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.TeamData;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.game.rules.RuleManager;

public class Core {
    
    @Getter
    private int health;
    @Getter
    private final TeamData teamData;
    
    private final Runnable onDieFunction;
    
    public Core(String teamName, Runnable onDie) {
        teamData = Store.getInstance().getDataInstance().getTeamData().stream().filter(teamData -> teamData.getTeamName().equals(teamName)).findFirst().orElse(null);
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
