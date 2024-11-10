package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.effects.LineEffect;
import remonone.nftilation.effects.props.LineProps;
import remonone.nftilation.effects.strategies.ParticleColorStrategy;
import remonone.nftilation.game.ingame.objects.TrapCircle;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CyberExpert extends Role {
    
    @Override
    public String getRoleID() {
        return "CE";
    }

    public CyberExpert() {
        super("CE");
        super.registerHandlers(new HashMap<String, IAbilityHandler>() {
            {
                put("jail", new IAbilityHandler() {
                    @Override
                    public boolean executeHandle(PlayerModel model) { return jailTarget(model); }
                    @Override
                    public float getCooldown(PlayerModel model) {
                        return ((Double)getMetaByName(model, MetaConstants.META_CE_JAIL_COOLDOWN)).floatValue();
                    }
                });
                put("shot", new IAbilityHandler() {
                    @Override
                    public boolean executeHandle(PlayerModel model) { return shotArrow(model); }
                    @Override
                    public float getCooldown(PlayerModel model) {
                        return ((Double)getMetaByName(model, MetaConstants.META_CE_SHOT_COOLDOWN)).floatValue();
                    }
                });
                put("teleport", new IAbilityHandler() {
                    @Override
                    public boolean executeHandle(PlayerModel model) { return teleportWithin(model); }
                    @Override
                    public float getCooldown(PlayerModel model) {
                        return ((Double)getMetaByName(model, MetaConstants.META_CE_TELEPORT_COOLDOWN)).floatValue();
                    }
                });
            }
        }, RoleConstant.CYBER_EXPERT_NBT_CONTAINER);
    }

    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> playerParams) {
        ArrayList<ItemStack> items = new ArrayList<>();
        if((Integer)playerParams.getOrDefault(PropertyConstant.PLAYER_LEVEL_PARAM, 1) != 1) {
            ItemStack jail = new ItemStack(Material.IRON_FENCE);
            ItemMeta meta = jail.getItemMeta();
            meta.setDisplayName("Кибер-СИЗО");
            jail.setItemMeta(meta);
            NBT.modify(jail, (nbt) -> {
                nbt.setString(RoleConstant.CYBER_EXPERT_NBT_CONTAINER, "jail");
            });
            items.add(jail);
        }
        ItemStack pistol = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta pistolMeta = pistol.getItemMeta();
        pistolMeta.setDisplayName(ChatColor.DARK_PURPLE + "Пистолет Макарова");
        pistol.setItemMeta(pistolMeta);
        NBT.modify(pistol, (nbt) -> {nbt.setString(RoleConstant.CYBER_EXPERT_NBT_CONTAINER, "shot");});
        items.add(pistol);
        ItemStack teleport = new ItemStack(Material.EYE_OF_ENDER);
        NBT.modify(teleport, (nbt) -> {nbt.setString(RoleConstant.CYBER_EXPERT_NBT_CONTAINER, "teleport");});
        ItemMeta teleportMeta = teleport.getItemMeta();
        teleportMeta.setDisplayName(ChatColor.GOLD + "Забекдорить");
        teleport.setItemMeta(teleportMeta);
        items.add(teleport);
        return items;
    }

    private boolean teleportWithin(PlayerModel model) {
        Player player = model.getReference();
        int level = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        Integer range = (Integer)getMetaInfo(MetaConstants.META_CE_TELEPORT_RANGE, level);
        Location positionToTeleport = BlockUtils.getBlockLookedAt(player, range).add(new Vector(.5F, 0, .5F));
        positionToTeleport.setPitch(player.getLocation().getPitch());
        positionToTeleport.setYaw(player.getLocation().getYaw());
        LineProps props = LineProps.builder()
                .world(player.getWorld())
                .from(player.getEyeLocation().toVector())
                .to(positionToTeleport.toVector())
                .particle(Particle.REDSTONE)
                .step(.1D)
                .particleStrategy(new ParticleColorStrategy(RGBConstants.blue))
                .build();
        new LineEffect().execute(props);
        player.teleport(positionToTeleport);
        return true;
    }

    private boolean jailTarget(PlayerModel model) {
        double range = (Double)getMetaByName(model, MetaConstants.META_CE_JAIL_RANGE);
        Player player = model.getReference();
        Entity selectedEntity = PlayerUtils.getEntityLookedAt(player, range);
        if(selectedEntity == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, .5f, 1f);
            return false;
        }
        if(!(selectedEntity instanceof Player)) {
            player.playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, .5f, 1f);
            return false;
        }
        Player target = (Player)selectedEntity;
        createTrap(target, model);
        return true;
    }

    private boolean shotArrow(PlayerModel user) {
        Player player = user.getReference();
        int level = (Integer)user.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        Arrow arrow = player.launchProjectile(Arrow.class);
        Double strength = (Double)getMetaInfo(MetaConstants.META_CE_SHOT_STRENGTH, level);
        arrow.setVelocity(arrow.getVelocity().multiply(strength));
        return true;
    }

    private void createTrap(Player target, PlayerModel performer) {
        double duration = (Double)getMetaByName(performer, MetaConstants.META_CE_JAIL_DURATION);
        double radius = (Double)getMetaByName(performer, MetaConstants.META_CE_JAIL_RADIUS);
        double knockback = (Double)getMetaByName(performer, MetaConstants.META_CE_JAIL_KNOCKBACK);
        double damage = (Double)getMetaByName(performer, MetaConstants.META_CE_JAIL_DAMAGE);
        TrapCircle.builder()
                .trappee(target)
                .trapper(performer.getReference())
                .duration(duration * DataConstants.TICKS_IN_SECOND)
                .range(radius)
                .knockback(knockback)
                .damage(damage)
                .world(performer.getReference().getWorld())
                .build()
                .initTrap();
    }
}
