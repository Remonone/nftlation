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
import remonone.nftilation.constants.*;
import remonone.nftilation.effects.CircleEffect;
import remonone.nftilation.effects.props.CircleProps;
import remonone.nftilation.effects.strategies.ParticleRepulsionStrategy;
import remonone.nftilation.game.GameInstance;
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

    @SuppressWarnings("unchecked")
    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> playerParams) {
        ArrayList<ItemStack> items = new ArrayList<>();
        int upgradeLevel = (int)playerParams.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        if((Integer)playerParams.getOrDefault(PropertyConstant.PLAYER_LEVEL_PARAM, 1) != 1) {
            ItemStack jail = new ItemStack(Material.IRON_FENCE);
            String name = (String)getMetaInfo(MetaConstants.META_CE_JAIL_NAME, upgradeLevel);
            List<String> description = (List<String>)getMetaInfo(MetaConstants.META_CE_JAIL_DESCRIPTION, upgradeLevel);
            ItemMeta meta = jail.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(description);
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
        String name = (String)getMetaInfo(MetaConstants.META_CE_FLOW_NAME, upgradeLevel);
        List<String> description = (List<String>)getMetaInfo(MetaConstants.META_CE_FLOW_DESCRIPTION, upgradeLevel);
        ItemMeta flowMeta = flow.getItemMeta();
        flowMeta.setDisplayName(name);
        flowMeta.setLore(description);
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
                .center(player.getLocation().toVector().add(VectorUtils.UP))
                .minAngle(0)
                .maxAngle(360)
                .world(player.getWorld())
                .radius(.9D)
                .offset(new Vector())
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
            NotificationUtils.sendNotification(player, MessageConstant.EMPTY_TARGET, NotificationUtils.NotificationType.FAIL, true);
            return false;
        }
        if(!(selectedEntity instanceof Player)) {
            NotificationUtils.sendNotification(player, MessageConstant.PLAYER_TARGET_REQUIRED, NotificationUtils.NotificationType.FAIL, true);
            return false;
        }
        Player target = (Player)selectedEntity;
        if(GameInstance.getInstance().checkIfPlayersInSameTeam(target, player)){
            NotificationUtils.sendNotification(player, MessageConstant.PLAYER_IN_SAME_TEAM, NotificationUtils.NotificationType.FAIL, true);
            return false;
        }
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
