package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.effects.LineEffect;
import remonone.nftilation.effects.SphereEffect;
import remonone.nftilation.effects.props.SphereProps;
import remonone.nftilation.effects.strategies.ParticleColorStrategy;
import remonone.nftilation.effects.strategies.ParticleDirectionalStrategy;
import remonone.nftilation.effects.props.LineProps;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.BlockUtils;
import remonone.nftilation.utils.PlayerUtils;
import remonone.nftilation.utils.RGBConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Watcher extends Role{
    @Override
    public String getRoleID() {
        return "WA";
    }

    public Watcher() {
        super("WA");
        super.registerHandlers(new HashMap<String, IAbilityHandler>() {
            {
                put(RoleConstant.WATCHER_WIND_GUST, new IAbilityHandler() {
                    @Override
                    public boolean executeHandle(PlayerModel model) {
                        return useGustItem(model);
                    }

                    @Override
                    public float getCooldown(PlayerModel model) {
                        return ((Double)getMetaByName(model, MetaConstants.META_WA_GUST_COOLDOWN)).floatValue();
                    }
                });
                put(RoleConstant.WATCHER_SUPPRESSION, new IAbilityHandler() {
                    @Override
                    public boolean executeHandle(PlayerModel model) {
                        return onSoulSuppression(model);
                    }

                    @Override
                    public float getCooldown(PlayerModel model) {
                        return ((Double)getMetaByName(model, MetaConstants.META_WA_SUPPRESSION_COOLDOWN)).floatValue();
                    }
                });
                put(RoleConstant.WATCHER_WORMHOLE, new IAbilityHandler() {
                    @Override
                    public boolean executeHandle(PlayerModel model) {
                        return onWormholeUsed(model);
                    }

                    @Override
                    public float getCooldown(PlayerModel model) {
                        return ((Double)getMetaByName(model, MetaConstants.META_WA_WORMHOLE_COOLDOWN)).floatValue();
                    }
                });
            }
        }, RoleConstant.WATCHER_NBT_CONTAINER);
    }

    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> playerParams) {
        // Teleport
        // Soul suppression
        // Wind gust
        ItemStack teleport = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = teleport.getItemMeta();
        meta.setDisplayName("Teleport");
        teleport.setItemMeta(meta);
        NBT.modify(teleport, (nbt) -> {
            nbt.setString(RoleConstant.WATCHER_NBT_CONTAINER, RoleConstant.WATCHER_WORMHOLE);
        });
        ItemStack soulSuppression = new ItemStack(Material.STRING);
        ItemMeta soulSuppressionMeta = soulSuppression.getItemMeta();
        soulSuppressionMeta.setDisplayName("Suppression");
        soulSuppression.setItemMeta(soulSuppressionMeta);
        NBT.modify(soulSuppression, (nbt) -> {
            nbt.setString(RoleConstant.WATCHER_NBT_CONTAINER, RoleConstant.WATCHER_SUPPRESSION);
        });
        ItemStack windGust = new ItemStack(Material.FEATHER);
        ItemMeta windGustMeta = windGust.getItemMeta();
        windGustMeta.setDisplayName("Gust");
        windGust.setItemMeta(windGustMeta);
        NBT.modify(windGust, (nbt) -> {
            nbt.setString(RoleConstant.WATCHER_NBT_CONTAINER, RoleConstant.WATCHER_WIND_GUST);
        });
        return Arrays.asList(teleport, soulSuppression, windGust);
    }

    private boolean useGustItem(PlayerModel model) {
        return true;
    }

    private boolean onSoulSuppression(PlayerModel model) {
        Player player = model.getReference();
        double stun = (Double)getMetaByName(model, MetaConstants.META_WA_SUPPRESSION_DURATION);
        Entity entity = PlayerUtils.getEntityLookedAt(model.getReference(), (Double)getMetaByName(model, MetaConstants.META_WA_SUPPRESSION_RANGE));
        if(entity == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, .5f, 1f);
            return false;
        }
        if(!(entity instanceof Player)) {
            player.playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, .5f, 1f);
            return false;
        }
        Player target = (Player)entity;
        PlayerModel targetModel = PlayerUtils.getModelFromPlayer(target);
        long formattedStun = (long) stun * DataConstants.ONE_SECOND + System.currentTimeMillis();
        targetModel.getParameters().put(PropertyConstant.PLAYER_STUN_DURATION, formattedStun);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int)stun * DataConstants.TICKS_IN_SECOND, 5, false, false));
        target.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, .5f);
        int taskId = new BukkitRunnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(target, player, EntityDamageEvent.DamageCause.WITHER, 2);
                if(event.isCancelled()) {
                    return;
                }
                target.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, .5f);
                target.damage(event.getFinalDamage());
            }
        }.runTaskTimer(Nftilation.getInstance(), 0, 20).getTaskId();
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().cancelTask(taskId);
            }
        }.runTaskLater(Nftilation.getInstance(), (long)stun * DataConstants.TICKS_IN_SECOND);
        return true;
    }

    private boolean onWormholeUsed(PlayerModel model) {
        Player player = model.getReference();
        int level = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        Integer range = (Integer)getMetaInfo(MetaConstants.META_WA_WORMHOLE_DISTANCE, level);
        Location positionToTeleport = BlockUtils.getBlockLookedAt(player, range).add(new Vector(.5F, 0, .5F));
        positionToTeleport.setPitch(player.getLocation().getPitch());
        positionToTeleport.setYaw(player.getLocation().getYaw());
        LineProps props = LineProps.builder()
                .world(player.getWorld())
                .from(player.getEyeLocation().toVector())
                .to(positionToTeleport.toVector())
                .particle(Particle.REDSTONE)
                .step(.1D)
                .particleStrategy(new ParticleColorStrategy(RGBConstants.purple))
                .build();
        new LineEffect().execute(props);
        player.teleport(positionToTeleport);
        player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1f, .2f);
        summonExplosion(model);
        return true;
    }

    private void summonExplosion(PlayerModel model) {
        Player player = model.getReference();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, 2f, .4f);
        SphereProps props = SphereProps.builder()
                .density(300)
                .particle(Particle.FLAME)
                .world(player.getWorld())
                .particleStrategy(new ParticleDirectionalStrategy(model.getReference().getLocation().toVector(), .4d))
                .radius(8)
                .center(player.getLocation().toVector())
                .build();
        new SphereEffect().execute(props);
        Location loc = player.getLocation();
        new BukkitRunnable() {
            @Override
            public void run() {
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, .4f);
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5, 100, false, false));
                loc.getWorld().createExplosion(loc, 5, false);
            }
        }.runTaskLater(Nftilation.getInstance(), 40);
    }

}
