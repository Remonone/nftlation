package remonone.nftilation.game.ingame.objects;

import org.bukkit.*;
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
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.EntityDamageByPlayerLog;
import remonone.nftilation.utils.MathUtils;
import remonone.nftilation.utils.PlayerUtils;

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
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                castEffect();
            }
        };
        task.runTaskTimer(Nftilation.getInstance(), 0, 4L);
        this.taskCastId = task.getTaskId();
    }

    private void castEffect() {
        for(float rotation = 0; rotation < 360; rotation += 2) {
            Vector rotationVector = MathUtils.getRotationVector(rotation);
            Location pos = this.center.clone().add(rotationVector.multiply(DISTANCE));
            this.center.getWorld().spawnParticle(Particle.REDSTONE, pos, 1, 0, 0, 0);
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
