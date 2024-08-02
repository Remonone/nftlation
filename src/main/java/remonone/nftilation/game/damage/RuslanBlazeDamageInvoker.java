package remonone.nftilation.game.damage;

import net.minecraft.server.v1_12_R1.EntityBlaze;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftBlaze;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.mob.RuslanBlaze;
import remonone.nftilation.utils.PlayerUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class RuslanBlazeDamageInvoker extends BaseDamageInvoker {

    private final static List<PotionEffectType> negativeEffects = Arrays.asList(PotionEffectType.CONFUSION, PotionEffectType.BLINDNESS, PotionEffectType.POISON, PotionEffectType.SLOW, PotionEffectType.SLOW_DIGGING, PotionEffectType.WITHER, PotionEffectType.HARM, PotionEffectType.WEAKNESS, PotionEffectType.HUNGER, PotionEffectType.LEVITATION);

    private final static Random random = new Random(System.currentTimeMillis());
        
    public static PotionEffectType getRandomNegativeEffect() {
        return negativeEffects.get(random.nextInt(negativeEffects.size()));
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void OnEntityDamageDealing(EntityDamageByEntityEvent e, PlayerUtils.AttackerInfo info) {
        if(!(e.getDamager() instanceof Fireball)) return;
        Fireball fireball = (Fireball) e.getDamager();
        if(!(info.source instanceof Blaze)) return;
        Blaze blaze = (Blaze) fireball.getShooter();

        if(!(e.getEntity() instanceof LivingEntity)) return;
        LivingEntity livingEntity = (LivingEntity) e.getEntity();
        EntityBlaze entityBlaze = ((CraftBlaze)blaze).getHandle();
        if(!(entityBlaze instanceof RuslanBlaze)) {
            return;
        }
        RuslanBlaze ruslanBlaze = (RuslanBlaze) entityBlaze;
        Player host = (Player) ruslanBlaze.getOwner().getBukkitEntity();
        if(host == null) {
            return;
        }
        e.setCancelled(true);
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(host, livingEntity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, RoleConstant.RUSLAN_BLAZE_DAMAGE);
        getServer().getPluginManager().callEvent(event);
        if(!event.isCancelled()) {
            livingEntity.setHealth(livingEntity.getHealth() - event.getFinalDamage());
            livingEntity.setLastDamageCause(event);
            livingEntity.addPotionEffect(new PotionEffect(getRandomNegativeEffect(), 5 * DataConstants.TICKS_IN_SECOND, 1, false, true));
        }
    }
}
