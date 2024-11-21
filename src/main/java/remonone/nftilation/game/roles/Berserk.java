package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.effects.CircleEffect;
import remonone.nftilation.effects.props.CircleProps;
import remonone.nftilation.effects.strategies.ParticleRepulsionStrategy;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.damage.BerserkUltimateHandler;
import remonone.nftilation.game.models.IDamageHandler;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.PlayerUtils;
import remonone.nftilation.utils.VectorUtils;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class Berserk extends Role {
    
    public Berserk() {
        super("GT");
        super.registerHandlers(new HashMap<String, IAbilityHandler>() {{
            put(RoleConstant.BERSERK_NBT_RAGE, new IAbilityHandler() {
                @Override
                public boolean executeHandle(PlayerModel model) {
                    return startBerserk(model);
                }

                @Override
                public float getCooldown(PlayerModel model) {
                    return ((Double)getMetaByName(model, MetaConstants.META_BERSERK_RAGE_COOLDOWN)).floatValue();
                }
            });
            put(RoleConstant.BERSERK_NBT_FEAR, new IAbilityHandler() {
                @Override
                public boolean executeHandle(PlayerModel model) {
                    return fearEnemies(model);
                }

                @Override
                public float getCooldown(PlayerModel model) {
                    return ((Double)getMetaByName(model, MetaConstants.META_BERSERK_FEAR_COOLDOWN)).floatValue();
                }
            });
        }}, RoleConstant.BERSERK_NBT_CONTAINER);
    }

    private boolean fearEnemies(PlayerModel model) {
        Player player = model.getReference();
        double range = (Double)getMetaByName(model, MetaConstants.META_BERSERK_FEAR_RANGE);
        List<Entity> entities = player.getNearbyEntities(range, range, range);
        double duration = (Double)getMetaByName(model, MetaConstants.META_BERSERK_FEAR_DURATION);
        CircleEffect effect = new CircleEffect();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_SCREAM, 1f, .3F);
        for(Entity entity: entities) {
            if(!(entity instanceof Player)) continue;
            if(GameInstance.getInstance().checkIfPlayersInSameTeam(player, (Player)entity)) continue;
            PlayerModel target = PlayerUtils.getModelFromPlayer((Player) entity);
            target.getParameters().put(PropertyConstant.PLAYER_FRAGILITY_DURATION, (long)(System.currentTimeMillis() + duration));
            initFearEffect(target, effect, duration);
        }
        return true;
    }

    private void initFearEffect(PlayerModel target, CircleEffect effect, double duration) {
        int fearTask = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = target.getReference();
                CircleProps props = CircleProps.builder()
                        .world(player.getWorld())
                        .radius(.7F)
                        .minAngle(0)
                        .maxAngle(360)
                        .center(player.getLocation().add(0, .5, 0).toVector())
                        .step(36)
                        .offset(VectorUtils.ZERO)
                        .particle(Particle.VILLAGER_ANGRY)
                        .particleStrategy(new ParticleRepulsionStrategy(player.getLocation().toVector().add(VectorUtils.UP), .1F))
                        .build();
                effect.execute(props);
            }
        }.runTaskTimer(Nftilation.getInstance(), 0, 20).getTaskId();
        new BukkitRunnable(){
            @Override
            public void run() {
                getServer().getScheduler().cancelTask(fearTask);
            }
        }.runTaskLater(Nftilation.getInstance(), (int)(duration * DataConstants.TICKS_IN_SECOND));
    }

    @Override
    public String getRoleID() {
        return "GT";
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> params) {
        List<ItemStack> items = new ArrayList<>();
        int level = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        String fearName = (String)getMetaInfo(MetaConstants.META_BERSERK_FEAR_NAME, level);
        ItemStack fear = new ItemStack(Material.GHAST_TEAR);
        ItemMeta fearMeta = fear.getItemMeta();
        fearMeta.setDisplayName(fearName);
        List<String> fearDescr = (List<String>) getMetaInfo(MetaConstants.META_BERSERK_FEAR_DESCRIPTION, level);
        fearMeta.setLore(fearDescr);
        fear.setItemMeta(fearMeta);
        NBT.modify(fear, (nbt) -> {
            nbt.setString(RoleConstant.BERSERK_NBT_CONTAINER, RoleConstant.BERSERK_NBT_FEAR);
            nbt.setString(RoleConstant.ROLE, getRoleID());
        });
        items.add(fear);
        
        if((Boolean)getMetaInfo(MetaConstants.META_BERSERK_RAGE_AVAILABILITY, level)) {
            ItemStack collapser = new ItemStack(Material.FERMENTED_SPIDER_EYE);
            ItemMeta meta = collapser.getItemMeta();
            String name = (String)getMetaInfo(MetaConstants.META_BERSERK_RAGE_NAME, level);
            List<String> rageDescr = (List<String>) getMetaInfo(MetaConstants.META_BERSERK_RAGE_DESCRIPTION, level);
            meta.setDisplayName(name);
            meta.setLore(rageDescr);
            collapser.setItemMeta(meta);
            NBT.modify(collapser, (nbt) -> {
                nbt.setString(RoleConstant.BERSERK_NBT_CONTAINER, RoleConstant.BERSERK_NBT_RAGE);
                nbt.setString(RoleConstant.ROLE, getRoleID());
            });
            items.add(collapser);
        }
        return items;
    }

    private boolean startBerserk(PlayerModel model) {
        Player player = model.getReference();
        Vector destination = VectorUtils.getBlockPositionOnDirection(player.getWorld(), player.getEyeLocation().toVector(), player.getEyeLocation().getDirection(), 25F);
        if (destination == null) {
            player.sendMessage(ChatColor.RED + "Неправильно выбранная точка!");
            return false;
        }
        player.setVelocity(VectorUtils.UP);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                Vector direction = destination.subtract(player.getLocation().toVector()).normalize().multiply(6);
                player.setVelocity(direction);
            }
        }.runTaskLater(Nftilation.getInstance(), 10);
        
        model.getParameters().put(PropertyConstant.PLAYER_FALL_DAMAGE_BERSERK, 1);
        return true;
    }

    @Override
    public List<IDamageHandler> getDamageHandlers() {
        return Collections.singletonList(new BerserkUltimateHandler());
    }
}
