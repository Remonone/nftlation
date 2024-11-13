package remonone.nftilation.game.ingame.objects;

import lombok.Builder;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.effects.SphereEffect;
import remonone.nftilation.effects.props.SphereProps;
import remonone.nftilation.effects.strategies.ParticleColorStrategy;
import remonone.nftilation.effects.strategies.ParticleStaticStrategy;
import remonone.nftilation.game.models.EffectPotion;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.utils.PlayerUtils;
import remonone.nftilation.utils.RGBConstants;

import java.util.List;
import java.util.Set;

import static org.bukkit.Bukkit.getServer;

@Builder
public class Barrier implements Listener {

    private final Player owner;
    private final ITeam teamOwner;
    private final double radius;
    private final Location center;
    private final double throwbackScale;
    private List<EffectPotion> potions;

    private final Set<Player> playerInRange;

    private int sphereTaskId;
    private int buffTaskId;
    private int unregisteringTaskId;

    public void initBarrier(int duration) {
        SphereProps props = SphereProps.builder()
                .density(400)
                .particle(Particle.REDSTONE)
                .particleStrategy(new ParticleColorStrategy(RGBConstants.amber))
                .world(center.getWorld())
                .center(center.toVector())
                .radius(radius)
                .build();
        SphereEffect sphereEffect = new SphereEffect();
        sphereTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                sphereEffect.execute(props);
            }
        }.runTaskTimer(Nftilation.getInstance(), 0, 4L).getTaskId();
        Vector shift = new Vector(0,.3,0);
        Vector zero = new Vector();
        buffTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                for(EffectPotion potion : potions) {
                    PotionEffectType type = PotionEffectType.getByName(potion.getEffect());
                    PotionEffect effect = new PotionEffect(type, 25, potion.getStrength(), false, false);
                    for(Player player : playerInRange) {
                        player.removePotionEffect(type);
                        player.addPotionEffect(effect);
                        SphereProps prop = SphereProps.builder()
                                .density(5)
                                .particleStrategy(new ParticleStaticStrategy(1, zero))
                                .radius(.5)
                                .center(player.getLocation().toVector().add(shift))
                                .particle(Particle.TOTEM)
                                .build();
                        sphereEffect.execute(prop);
                    }
                }
            }
        }.runTaskTimer(Nftilation.getInstance(), 0, 20L).getTaskId();
        unregisteringTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                unregisterBarrier(false);
            }
        }.runTaskLater(Nftilation.getInstance(), duration).getTaskId();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player mover = event.getPlayer();
        ITeam team = PlayerUtils.getTeamFromPlayer(mover);
        if(team == null) return;
        if(event.getTo().distance(center) > radius) {
            if(mover.getUniqueId().equals(owner.getUniqueId())) {
                unregisterBarrier(true);
                return;
            }
            playerInRange.remove(mover);
            return;
        }
        if(playerInRange.contains(mover)) return;
        if(team.getTeamID().equals(teamOwner.getTeamID())) {
            playerInRange.add(mover);
            return;
        }
        Vector dir = center.toVector().subtract(mover.getLocation().toVector());
        double naturalScale = 1D / dir.length();
        dir.normalize().multiply(naturalScale * throwbackScale);
        mover.setVelocity(dir);
    }

    private void unregisterBarrier(boolean isArtificiallyCanceled) {
        BukkitScheduler scheduler = getServer().getScheduler();
        if(!isArtificiallyCanceled) {
            scheduler.cancelTask(unregisteringTaskId);
        }
        scheduler.cancelTask(sphereTaskId);
        scheduler.cancelTask(buffTaskId);
    }
}
