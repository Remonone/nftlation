package remonone.nftilation.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import remonone.nftilation.effects.SphereEffect;
import remonone.nftilation.effects.props.SphereProps;
import remonone.nftilation.effects.strategies.ParticleRepulsionStrategy;
import remonone.nftilation.effects.strategies.ParticleStaticStrategy;

import java.util.Collection;

import static org.bukkit.Bukkit.getServer;

public class AttackPresets {
    
    @SuppressWarnings("deprecation")
    public static void summonExplosion(Location loc, Player attacker, double range, double damage, int explosion_density, int explosion_quality, int dust_density, double radius, boolean isHuge) {
        SphereEffect effect = new SphereEffect();
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, .2f);
        Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, range, range, range);
        Vector center = loc.toVector();
        Particle explosion = isHuge ? Particle.EXPLOSION_HUGE : Particle.EXPLOSION_LARGE;
        SphereProps mainExplosion = SphereProps.builder()
                .particle(explosion)
                .particleStrategy(new ParticleStaticStrategy(explosion_quality, new Vector(0, .5, 0)))
                .radius(radius)
                .center(loc.toVector())
                .world(loc.getWorld())
                .density(explosion_density)
                .build();
        SphereProps dust = SphereProps.builder()
                .particle(Particle.CLOUD)
                .particleStrategy(new ParticleRepulsionStrategy(loc.toVector(), 1.2f))
                .radius(range / 1.5D)
                .center(loc.toVector())
                .world(loc.getWorld())
                .density(dust_density)
                .build();
        effect.execute(mainExplosion);
        effect.execute(dust);
        Vector directionShifter = VectorUtils.UP.clone().multiply(.3);
        for(Entity entity : entities) {
            if(!(entity instanceof LivingEntity)) {
                continue;
            }
            if(entity.equals(attacker)) continue;
            Vector position = entity.getLocation().toVector();
            Vector direction = position.subtract(center);
            double scale = .1F;
            if(direction.length() > .1) {
                scale = 1 / direction.length();
            }
            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(attacker, entity, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, damage * scale);
            getServer().getPluginManager().callEvent(event);
            if(event.isCancelled()) continue;
            Vector n_direction = direction.add(directionShifter).normalize();
            LivingEntity target = (LivingEntity) entity;
            target.setHealth(target.getHealth() - event.getFinalDamage());
            entity.setVelocity(n_direction.multiply(scale * 8));
        }
    }
}
