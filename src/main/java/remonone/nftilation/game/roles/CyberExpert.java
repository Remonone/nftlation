package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.effects.CircleEffect;
import remonone.nftilation.effects.props.CircleProps;
import remonone.nftilation.effects.strategies.ParticleRepulsionStrategy;
import remonone.nftilation.game.ingame.objects.TrapCircle;
import remonone.nftilation.game.models.EffectPotion;
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
                put("feather", new IAbilityHandler() {
                    @Override
                    public boolean executeHandle(PlayerModel model) { return increaseMoveSpeed(model); }
                    @Override
                    public float getCooldown(PlayerModel model) {
                        return ((Double)getMetaByName(model, MetaConstants.META_CE_FLOW_COOLDOWN)).floatValue();
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
                nbt.setString(RoleConstant.ROLE, getRoleID());
            });
            items.add(jail);
        }
        ItemStack flow = new ItemStack(Material.FEATHER);
        NBT.modify(flow, (nbt) -> {
            nbt.setString(RoleConstant.CYBER_EXPERT_NBT_CONTAINER, "feather");
            nbt.setString(RoleConstant.ROLE, getRoleID());
        });
        ItemMeta flowMeta = flow.getItemMeta();
        flowMeta.setDisplayName(ChatColor.GRAY + "Режим погони");
        flow.setItemMeta(flowMeta);
        items.add(flow);
        return items;
    }

    @SuppressWarnings("unchecked")
    private boolean increaseMoveSpeed(PlayerModel model) {
        Player player = model.getReference();
        CircleProps props = CircleProps.builder()
                .step(5D)
                .particleStrategy(new ParticleRepulsionStrategy(player.getLocation().toVector().add(new Vector(0, 2F, 0)), 1F))
                .particle(Particle.CLOUD)
                .center(player.getLocation().toVector().add(new Vector(0, 1F, 0)))
                .minAngle(0)
                .maxAngle(360)
                .world(player.getWorld())
                .radius(.9D)
                .build();
        new CircleEffect().execute(props);
        List<EffectPotion> effects = (List<EffectPotion>) getMetaByName(model, MetaConstants.META_CE_FLOW_EFFECTS);
        for(EffectPotion effectPotion : effects) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effectPotion.getEffect()), effectPotion.getDuration(), effectPotion.getStrength(), false, false));
        }
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
