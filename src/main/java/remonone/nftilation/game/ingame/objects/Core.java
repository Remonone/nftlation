package remonone.nftilation.game.ingame.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RuleConstants;
import remonone.nftilation.events.OnCoreDamageEvent;
import remonone.nftilation.events.OnCoreDestroyEvent;
import remonone.nftilation.game.rules.RuleManager;
import remonone.nftilation.utils.Logger;

import static org.bukkit.Bukkit.getServer;

@Setter
public class Core implements ICoreData, Listener {
    
    @Getter
    private int health;
    @Getter
    private int oldHealth;
    private String teamName;
    
    public Core(String teamName) {
        health = 100;
        this.teamName = teamName;
    }
    
    private boolean takeDamage(boolean isPlayerDamager) {
        if(health < 1) return false;
        int damage = 1;
        if(isPlayerDamager) damage = (int) RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_CORE_DAMAGE_INTAKE, 1);
        oldHealth = health;
        health -= damage;
        return health < 1;
    }
    
    public boolean isCoreCannotBeHealed() {
        return health < 1 || health >= 100;
    }
    
    public void Heal() {
        if(isCoreCannotBeHealed()) return;
        oldHealth = health;
        health++;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(OnCoreDamageEvent event) {
        if(event.getAttacker() != null) {
            if(teamName.equals(event.getAttacker().getParameters().get(PropertyConstant.PLAYER_TEAM_NAME))) return;
        }
        if(!event.getTeamName().equals(teamName) || health < 1) return;
        boolean isInvulnerable = (boolean)RuleManager.getInstance().getRuleOrDefault(RuleConstants.RULE_CORE_INVULNERABLE, false);
        if(isInvulnerable) {
            if(event.getAttacker() != null)
                event.getAttacker().getReference().sendMessage(ChatColor.RED + MessageConstant.CORE_INVULNERABLE);
            event.setCancelled(true);
            return;
        }
        if(takeDamage(event.getAttacker() != null)) {
            OnCoreDestroyEvent e = new OnCoreDestroyEvent(teamName, event.getTeamName());
            getServer().getPluginManager().callEvent(e);
        }
    }
}
