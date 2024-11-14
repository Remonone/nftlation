package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.effects.CircleEffect;
import remonone.nftilation.effects.SphereEffect;
import remonone.nftilation.effects.props.CircleProps;
import remonone.nftilation.effects.props.SphereProps;
import remonone.nftilation.effects.strategies.ParticleColorStrategy;
import remonone.nftilation.effects.strategies.ParticleRepulsionStrategy;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.*;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class SybilAttacker extends Role {
    
    public SybilAttacker() {
        super("SA");
        super.registerHandlers(new HashMap<String, IAbilityHandler>() {{
            put(RoleConstant.SYBIL_ATTACKER_PISTOL, new IAbilityHandler() {
                @Override
                public boolean executeHandle(PlayerModel model) {
                    return shotArrow(model);
                }

                @Override
                public float getCooldown(PlayerModel model) {
                    return ((Double)getMetaByName(model, MetaConstants.META_SA_SHOT_COOLDOWN)).floatValue();
                }
            });
            put(RoleConstant.SYBIL_ATTACKER_EXPLOSION, new IAbilityHandler() {
                @Override
                public boolean executeHandle(PlayerModel model) {
                    return summonArtillery(model);
                }

                @Override
                public float getCooldown(PlayerModel model) {
                    return ((Double)getMetaByName(model, MetaConstants.META_SA_EXPLOSION_COOLDOWN)).floatValue();
                }
            });
        }}, RoleConstant.SYBIL_ATTACKER_NBT_CONTAINER);
    }

    private boolean summonArtillery(PlayerModel model) {
        Player player = model.getReference();
        Double range = (Double) getMetaByName(model, MetaConstants.META_SA_EXPLOSION_RANGE);
        int delay = (int)(((Double) getMetaByName(model, MetaConstants.META_SA_EXPLOSION_DELAY)) * DataConstants.TICKS_IN_SECOND);
        int amount = (Integer) getMetaByName(model, MetaConstants.META_SA_EXPLOSION_AMOUNT);
        float area = ((Double)getMetaByName(model, MetaConstants.META_SA_EXPLOSION_AREA)).floatValue();
        double radius = (Double)getMetaByName(model, MetaConstants.META_SA_EXPLOSION_SINGLE_RADIUS);
        double damage = (Double)getMetaByName(model, MetaConstants.META_SA_EXPLOSION_SINGLE_DAMAGE);
        Location location = shiftLocationUntilGround(BlockUtils.getBlockLookedAt(player, range.intValue()));
        int explosionTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                Vector vec = VectorUtils.getRandomPosInCircle(location.toVector(), area);
                Location loc = new Location(location.getWorld(), vec.getX(), vec.getY(), vec.getZ());
                AttackPresets.summonExplosion(loc, player, radius, damage, 2, 12, 50, radius, false);
            }
        }.runTaskTimer(Nftilation.getInstance(), 10, delay).getTaskId();
        CircleProps props = CircleProps.builder()
                .world(location.getWorld())
                .particle(Particle.REDSTONE)
                .radius(area)
                .center(location.toVector())
                .minAngle(0).maxAngle(360).step(.5F)
                .particleStrategy(new ParticleColorStrategy(RGBConstants.white))
                .offset(new Vector(0, .5F, 0))
                .build();
        CircleEffect effect = new CircleEffect();
        int areaTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                effect.execute(props);
            }
        }.runTaskTimer(Nftilation.getInstance(), 0, 4L).getTaskId();
        new BukkitRunnable() {
            @Override
            public void run() {
                getServer().getScheduler().cancelTask(areaTaskId);
                getServer().getScheduler().cancelTask(explosionTaskId);
            }
        }.runTaskLater(Nftilation.getInstance(), (long) delay * amount);
        return true;
    }

    private Location shiftLocationUntilGround(Location location) {
        Block block = location.getBlock();
        while(block.getType().equals(Material.AIR)) {
            block = location.add(new Vector(0, -1, 0)).getBlock();
        }
        return block.getLocation().add(new Vector(0,1,0));
    }

    private boolean shotArrow(PlayerModel model) {
        Arrow arrow = model.getReference().launchProjectile(Arrow.class);
        double scale = (Double) getMetaByName(model, MetaConstants.META_SA_SHOT_STRENGTH);
        
        arrow.setVelocity(arrow.getVelocity().multiply(scale));
        boolean isExplosive = (Boolean) getMetaByName(model, MetaConstants.META_SA_SHOT_EXPLOSIVE);
        arrow.setMetadata(RoleConstant.SYBIL_ATTACKER_ARROW_EXPLOSIVE, new FixedMetadataValue(Nftilation.getInstance(), isExplosive));
        return true;
    }

    @Override
    public String getRoleID() {
        return "SA";
    }
    
    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> params) {
        List<ItemStack> items = new ArrayList<>();
        int level = (Integer) params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        ItemStack pistol = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta pistolMeta = pistol.getItemMeta();
        pistolMeta.setDisplayName(ChatColor.DARK_PURPLE + "Пистолет Макарова");
        pistol.setItemMeta(pistolMeta);
        NBT.modify(pistol, (nbt) -> {
            nbt.setString(RoleConstant.SYBIL_ATTACKER_NBT_CONTAINER, RoleConstant.SYBIL_ATTACKER_PISTOL);
            nbt.setString(RoleConstant.ROLE, getRoleID());
        });
        items.add(pistol);
        boolean isAccessible = (Boolean)getMetaInfo(MetaConstants.META_SA_EXPLOSION_AVAILABILITY, level);
        if(isAccessible) {
            ItemStack explosion = new ItemStack(Material.FIREWORK);
            ItemMeta explosionMeta = explosion.getItemMeta();
            String name = (String) getMetaInfo(MetaConstants.META_SA_EXPLOSION_NAME, level);
            explosionMeta.setDisplayName(name);
            explosion.setItemMeta(explosionMeta);
            NBT.modify(explosion, (nbt) -> {
                nbt.setString(RoleConstant.SYBIL_ATTACKER_NBT_CONTAINER, RoleConstant.SYBIL_ATTACKER_EXPLOSION);
                nbt.setString(RoleConstant.ROLE, getRoleID());
            });
            items.add(explosion);
        }
        return items;
    }

    @EventHandler
    public void onArrowHit(final ProjectileHitEvent e) {
        if(!(e.getEntity().getShooter() instanceof Player)) return;
        Player shooter = (Player) e.getEntity().getShooter();
        DataInstance data = Store.getInstance().getDataInstance();
        Role role = data.getPlayerRole(shooter.getUniqueId());
        if(!(role instanceof SybilAttacker)) return;
        PlayerModel model = PlayerUtils.getModelFromPlayer(shooter);
        if(model == null) return;
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        int upgradeLevel = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        ITeam team = GameInstance.getInstance().getTeam((String)model.getParameters().get(PropertyConstant.PLAYER_TEAM_NAME));
        if(team == null) return;
        if(upgradeLevel < 3) return;
        Location location = null;
        if(e.getHitEntity() != null) {
            location = e.getHitEntity().getLocation();
        }
        if(e.getHitBlock() != null) {
            location = e.getHitBlock().getLocation();
            location = location.add(new Vector(0, 1, 0));
        }
        if(location != null) {
            double power = (Double) getMetaByName(model, MetaConstants.META_SA_SHOT_EXPLOSIVE_POWER);
            double range = (Double) getMetaByName(model, MetaConstants.META_SA_SHOT_EXPLOSIVE_RANGE);
            AttackPresets.summonExplosion(location, shooter, range, power, 2, 10, 60, .3, true);
            SphereProps incarnation = SphereProps.builder()
                    .density(200)
                    .center(location.toVector())
                    .radius(3)
                    .particleStrategy(new ParticleRepulsionStrategy(location.toVector(), 1f))
                    .world(location.getWorld())
                    .particle(Particle.FLAME)
                    .build();
            new SphereEffect().execute(incarnation);
            e.getEntity().remove();
        }
    }


}
