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
import remonone.nftilation.effects.strategies.ParticleRepulsionStrategy;
import remonone.nftilation.game.damage.WatcherOnKillHandler;
import remonone.nftilation.game.models.IDamageHandler;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.*;

import java.util.*;

public class Watcher extends Role {
    @Override
    public String getRoleID() {
        return "WA";
    }
    
    @Override
    public boolean checkForRoleAccess(Role role) {
        return true;
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

    @SuppressWarnings("unchecked")
    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> playerParams) {
        ItemStack teleport = new ItemStack(Material.BLAZE_ROD);
        String wormholeName = (String)getMetaInfo(MetaConstants.META_WA_WORMHOLE_NAME, 1);
        List<String> wormholeDescription = new ArrayList<>((List<String>)getMetaInfo(MetaConstants.META_WA_WORMHOLE_DESCRIPTION, 1));
        wormholeDescription.add(0, ChatColor.GOLD + "LEGENDARY");
        ItemMeta meta = teleport.getItemMeta();
        meta.setDisplayName(wormholeName);
        meta.setLore(wormholeDescription);
        teleport.setItemMeta(meta);
        NBT.modify(teleport, (nbt) -> {
            nbt.setString(RoleConstant.WATCHER_NBT_CONTAINER, RoleConstant.WATCHER_WORMHOLE);
            nbt.setString(RoleConstant.ROLE, getRoleID());
        });
        ItemStack soulSuppression = new ItemStack(Material.STRING);
        String suppressionName = (String)getMetaInfo(MetaConstants.META_WA_SUPPRESSION_NAME, 1);
        List<String> suppressionDescription = new ArrayList<>((List<String>)getMetaInfo(MetaConstants.META_WA_SUPPRESSION_DESCRIPTION, 1));
        suppressionDescription.add(0, ChatColor.GOLD + "LEGENDARY");
        ItemMeta soulSuppressionMeta = soulSuppression.getItemMeta();
        soulSuppressionMeta.setDisplayName(suppressionName);
        soulSuppressionMeta.setLore(suppressionDescription);
        soulSuppression.setItemMeta(soulSuppressionMeta);
        NBT.modify(soulSuppression, (nbt) -> {
            nbt.setString(RoleConstant.WATCHER_NBT_CONTAINER, RoleConstant.WATCHER_SUPPRESSION);
            nbt.setString(RoleConstant.ROLE, getRoleID());
        });
        ItemStack windGust = new ItemStack(Material.FEATHER);
        String gustName = (String)getMetaInfo(MetaConstants.META_WA_GUST_NAME, 1);
        List<String> gustDescription = new ArrayList<>((List<String>)getMetaInfo(MetaConstants.META_WA_GUST_DESCRIPTION, 1));
        gustDescription.add(0, ChatColor.BLUE + "EPIC");
        ItemMeta windGustMeta = windGust.getItemMeta();
        windGustMeta.setDisplayName(gustName);
        windGustMeta.setLore(gustDescription);
        windGust.setItemMeta(windGustMeta);
        NBT.modify(windGust, (nbt) -> {
            nbt.setString(RoleConstant.WATCHER_NBT_CONTAINER, RoleConstant.WATCHER_WIND_GUST);
            nbt.setString(RoleConstant.ROLE, getRoleID());
        });
        return Arrays.asList(teleport, soulSuppression, windGust);
    }

    private boolean useGustItem(PlayerModel model) {
        Player performer = model.getReference();
        Location loc = performer.getLocation();
        Vector direction = loc.getDirection();
        double range = (Double)getMetaByName(model, MetaConstants.META_WA_GUST_RANGE);
        double scale = (Double)getMetaByName(model, MetaConstants.META_WA_GUST_SCALE);
        Vector size = new Vector(range / direction.getX(), range, range / direction.getZ());
        Collection<Entity> entities = performer.getWorld().getNearbyEntities(loc, size.getX(), size.getY(), size.getZ());
        performer.getWorld().playSound(performer.getLocation(), Sound.ITEM_ELYTRA_FLYING, 1f, .8f);
        SphereProps props = SphereProps.builder()
                .density(300)
                .particle(Particle.CLOUD)
                .world(performer.getWorld())
                .particleStrategy(new ParticleRepulsionStrategy(model.getReference().getLocation().toVector(), 2d))
                .radius(2D)
                .center(performer.getLocation().toVector())
                .build();
        new SphereEffect().execute(props);
        Vector additionalForce = VectorUtils.UP.clone().multiply(.5);
        for(Entity entity : entities) {
            if(entity.equals(performer)) continue;
            Vector entityPosition = entity.getLocation().toVector().subtract(loc.toVector()).normalize();
            Vector entityVelocity = entity.getVelocity();
            entityVelocity.add(entityPosition).multiply(scale).add(additionalForce);
            entity.setVelocity(entityVelocity);
        }
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
        target.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, .3f);
        SphereProps props = SphereProps.builder()
                .density(200)
                .particleStrategy(new ParticleRepulsionStrategy(target.getLocation().toVector().add(new Vector(0, .5D, 0)), .7f))
                .world(target.getWorld())
                .radius(3)
                .center(target.getLocation().toVector().add(VectorUtils.UP.clone().multiply(.5)))
                .particle(Particle.DRAGON_BREATH)
                .build();
        SphereEffect effect = new SphereEffect();
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
                effect.execute(props);
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
                .density(400)
                .particle(Particle.FLAME)
                .world(player.getWorld())
                .particleStrategy(new ParticleDirectionalStrategy(model.getReference().getLocation().toVector(), .4d))
                .radius(8)
                .center(player.getLocation().toVector())
                .build();
        SphereEffect effect = new SphereEffect();
        effect.execute(props);
        Location loc = player.getLocation();
        new BukkitRunnable() {
            @Override
            public void run() {
                AttackPresets.summonExplosion(loc, player, 8, 10, 2, 15, 100, 2, true, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION);
            }
        }.runTaskLater(Nftilation.getInstance(), 40);
    }
    
    
    @Override
    public List<IDamageHandler> getDamageHandlers() {
        return Collections.singletonList(new WatcherOnKillHandler());
    }
}
