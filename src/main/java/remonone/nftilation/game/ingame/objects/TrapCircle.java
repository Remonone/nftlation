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
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.EntityDamageByPlayerLog;
import remonone.nftilation.utils.MathUtils;
import remonone.nftilation.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;


public class TrapCircle implements Listener {
    
    private final static Random RANDOM = new Random();
    
    private final double DISTANCE = 5D;
    
    private final double red = 160D / 255;
    private final double green = 32D / 255;
    private final double blue = 240D / 255;
    
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
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                castEffect();
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

    private void castEffect() {
        Vector shift = new Vector(0, .1f, 0);
        for(float rotation = 0; rotation < 360; rotation += 2) {
            Vector rotationVector = MathUtils.getRotationVector(rotation);
            Location pos = this.center.clone().add(rotationVector.multiply(DISTANCE).add(shift));
            this.center.getWorld().spawnParticle(Particle.SPELL_MOB, pos, 0, red, green, blue);
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
            castBlocker(mover);
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
    
    private void castBlocker(Player caster) {
        Vector centerVector = this.center.toVector();
        Vector shift = new Vector(0, .5F, 0);
        List<Vector> points = getPointsWithinSphere(caster.getLocation().toVector().clone().subtract(centerVector), 50, DISTANCE, 3D);
        for(Vector point : points) {
            Vector pos = point.add(centerVector).add(shift);
            caster.getWorld().spawnParticle(Particle.SPELL_MOB, pos.getX(), pos.getY(), pos.getZ(), 0, red, green, blue);
        }
    }
    
    private List<Vector> getPointsWithinSphere(Vector pos, int amount, double R, double radius) {
        List<Vector> points = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            double dTheta = (RANDOM.nextDouble() - 0.5) * (radius / R) * 2;
            double dPhi = (RANDOM.nextDouble() - 0.5) * (radius / R) * 2;

            // Углы центральной точки
            double theta = Math.acos(pos.getZ() / R);
            double phi = Math.atan2(pos.getY(), pos.getX());

            // Преобразование в декартовы координаты
            double newTheta = theta + dTheta;
            double newPhi = phi + dPhi;

            // Преобразование в декартовы координаты
            double x = R * Math.sin(newTheta) * Math.cos(newPhi);
            double y = R * Math.sin(newTheta) * Math.sin(newPhi);
            double z = R * Math.cos(newTheta);

            points.add(new Vector(x,y,z));
        }
        return points;
    }
}
