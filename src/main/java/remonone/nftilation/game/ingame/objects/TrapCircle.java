package remonone.nftilation.game.ingame.objects;

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


public class TrapCircle implements Listener {
    private final double DISTANCE = 5D;
    
    private final Player trappee;
    private final Player trapper;
    
    private final Location center;
    
    private int taskCastId = -1;
    
    public TrapCircle(Player trappee, Player trapper, int duration, World w) {
        this.trappee = trappee;
        this.trapper = trapper;
        Vector rawCenter = this.trappee.getLocation().toVector();
        this.center = new Location(w, rawCenter.getX(), rawCenter.getY(), rawCenter.getZ());
        registerTrap();
        new BukkitRunnable() {
            @Override
            public void run() {
                unregisterTrap();
            }
        }.runTaskLater(Nftilation.getInstance(), duration);
    }

    private void registerTrap() {
        getServer().getPluginManager().registerEvents(this, Nftilation.getInstance());
        knockbackRedundantPlayers();
        CircleProps props = CircleProps.builder()
                .world(this.center.getWorld())
                .particle(Particle.SPELL_MOB)
                .radius(DISTANCE)
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
    }

    private void knockbackRedundantPlayers() {
        List<Entity> players = trappee.getNearbyEntities(5, 5, 5);
        for(Entity player : players) {
            if(!(player instanceof Player)) continue;
            if(player.getUniqueId().equals(trapper.getUniqueId())) continue;
            double distance = this.center.distance(player.getLocation()) - DISTANCE;
            player.setVelocity(player.getLocation().clone().subtract(this.center).toVector().normalize().multiply(distance * 2D));
        }
    }

    private void unregisterTrap() {
        PlayerMoveEvent.getHandlerList().unregister(this);
        getServer().getScheduler().cancelTask(this.taskCastId);
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player mover = event.getPlayer();
        
        if(mover.getLocation().distance(this.center) <= DISTANCE) return;
        
        if(mover.getUniqueId().equals(trapper.getUniqueId())) {
            unregisterTrap();
            return;
        }
        if(mover.getUniqueId().equals(trappee.getUniqueId())) {
            trappee.getLocation().getWorld().playSound(trappee.getLocation(), Sound.ENTITY_GUARDIAN_AMBIENT, 1f, 1f);
            pushPlayerToCenter(mover);
            SpherePlainProps props = SpherePlainProps.builder()
                    .tense(50)
                    .particle(Particle.SPELL_MOB)
                    .world(trapper.getWorld())
                    .shift(new Vector(0, .5F, 0))
                    .localCenterPoint(mover.getLocation().toVector().clone().subtract(this.center.toVector().clone()))
                    .planeRadius(1.5D)
                    .sphereGlobalPoint(this.center.toVector().clone())
                    .projectedSphereRadius(DISTANCE)
                    .shift(new Vector(0, .5F, 0))
                    .count(0)
                    .build();
            props.setCustomOffset(RGBConstants.purple);
            new SpherePlainEffect().execute(props);
            EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(trapper, trappee, EntityDamageEvent.DamageCause.MAGIC, 1F);
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
        double distance = mover.getLocation().distance(this.center) - DISTANCE;
        mover.setVelocity(this.center.clone().subtract(mover.getLocation()).toVector().normalize().multiply(distance * 1.2D));
    }
}
