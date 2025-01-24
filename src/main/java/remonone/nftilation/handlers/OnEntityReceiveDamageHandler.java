package remonone.nftilation.handlers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import remonone.nftilation.game.damage.DamageType;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.PlayerUtils;

public class OnEntityReceiveDamageHandler implements Listener {
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(EntityDamageEvent.DamageCause.ENTITY_ATTACK.equals(event.getCause())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(validateDamage(event.getEntity(), null, event.getFinalDamage(), DamageType.getTypeByMinecraftType(event.getCause())));
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        event.setCancelled(validateDamage(event.getEntity(), event.getDamager(), event.getFinalDamage(), DamageType.getTypeByMinecraftType(event.getCause())));
    }

    private boolean validateDamage(Entity target, Entity attacker, double finalDamage, DamageType type) {
        if(!(target instanceof LivingEntity)) {
            return false;
        }
        LivingEntity liv = (LivingEntity) target;
        PlayerModel model = fetchModelFromAttacker(attacker);
        return true;
    }

    private PlayerModel fetchModelFromAttacker(Entity attacker) {
        if(attacker instanceof Player) {
            return PlayerUtils.getModelFromPlayer((Player) attacker);
        }
        if(attacker instanceof Fireball) {
            
        }
        return null;
        
    }
}
