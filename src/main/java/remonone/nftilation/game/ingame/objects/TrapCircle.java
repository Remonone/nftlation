package remonone.nftilation.game.ingame.objects;

import lombok.Builder;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.effects.CircleEffect;
import remonone.nftilation.effects.SpherePlainEffect;
import remonone.nftilation.effects.props.CircleProps;
import remonone.nftilation.effects.props.SpherePlainProps;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.EntityDamageByPlayerLog;
import remonone.nftilation.utils.PlayerUtils;
import remonone.nftilation.utils.RGBConstants;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

@Builder
public class TrapCircle implements Listener {
    private Player trappee;
    private Player trapper;
    
    private Location center;
    private double range;
    private double knockback;
    private double damage;
    private double duration;
    private World world;
    
    private int taskCastId;

    public void initTrap() {
        getServer().getPluginManager().registerEvents(this, Nftilation.getInstance());
        this.center = new Location(world, trappee.getLocation().getX(), trappee.getLocation().getY(), trappee.getLocation().getZ());
        knockbackRedundantPlayers();
        CircleProps props = CircleProps.builder()
                .world(this.center.getWorld())
                .particle(Particle.REDSTONE)
                .radius(range)
                .center(this.center.toVector().clone())
                .minAngle(0).maxAngle(360).step(2)
                .count(0).offset(new Vector(0, .5F, 0))
                .build();
        props.setCustomOffset(RGBConstants.purple);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                new CircleEffect().execute(props);
            }
        };
        task.runTaskTimer(Nftilation.getInstance(), 0, 4L);
        this.taskCastId = task.getTaskId();
        new BukkitRunnable() {
            @Override
            public void run() {
                unregisterTrap();
            }
        }.runTaskLater(Nftilation.getInstance(), (long)duration);
    }

    private void knockbackRedundantPlayers() {
        List<Entity> players = trappee.getNearbyEntities(range, range, range);
        for(Entity player : players) {
            if(!(player instanceof Player)) continue;
            if(player.getUniqueId().equals(trapper.getUniqueId())) continue;
            double distance = this.center.distance(player.getLocation()) - range;
            player.setVelocity(player.getLocation().clone().subtract(this.center).toVector().normalize().multiply(distance));
        }
    }

    private void unregisterTrap() {
        PlayerMoveEvent.getHandlerList().unregister(this);
        getServer().getScheduler().cancelTask(this.taskCastId);
    }


    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player mover = event.getPlayer();
        
        if(mover.getLocation().distance(this.center) <= range) return;
        
        if(mover.getUniqueId().equals(trapper.getUniqueId())) {
            unregisterTrap();
            return;
        }
        if(mover.getUniqueId().equals(trappee.getUniqueId())) {
            trappee.getLocation().getWorld().playSound(trappee.getLocation(), Sound.ENTITY_GUARDIAN_AMBIENT, 1f, 1f);
            pushPlayerToCenter(mover);
            SpherePlainProps props = SpherePlainProps.builder()
                    .tense(50)
                    .particle(Particle.REDSTONE)
                    .world(trapper.getWorld())
                    .shift(new Vector(0, .5F, 0))
                    .localCenterPoint(mover.getLocation().toVector().clone().subtract(this.center.toVector().clone()))
                    .planeRadius(1.5D)
                    .sphereGlobalPoint(this.center.toVector().clone())
                    .projectedSphereRadius(range)
                    .shift(new Vector(0, .5F, 0))
                    .count(0)
                    .build();
            props.setCustomOffset(RGBConstants.purple);
            new SpherePlainEffect().execute(props);
            EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(trapper, trappee, EntityDamageEvent.DamageCause.MAGIC, damage);
            getServer().getPluginManager().callEvent(e);
            if(e.isCancelled()) {
                PlayerModel model = PlayerUtils.getModelFromPlayer(mover);
                if(!(Boolean)model.getParameters().get(PropertyConstant.PLAYER_IS_ALIVE_PARAM)) {
                    unregisterTrap();
                }
            }
            trappee.setHealth(trappee.getHealth() - e.getFinalDamage());
            trappee.setLastDamageCause(e);
            EntityDamageByPlayerLog.insertLogEvent(trappee, trapper);
        }
    }

    private void pushPlayerToCenter(Player mover) {
        double distance = mover.getLocation().distance(this.center) - range;
        mover.setVelocity(this.center.clone().subtract(mover.getLocation()).toVector().normalize().multiply(distance * knockback));
    }
}
