package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.effects.SphereEffect;
import remonone.nftilation.effects.props.SphereProps;
import remonone.nftilation.effects.strategies.ParticleRepulsionStrategy;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.AttackPresets;
import remonone.nftilation.utils.PlayerUtils;

import java.util.*;

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
        }}, RoleConstant.SYBIL_ATTACKER_NBT_CONTAINER);
    }

    private boolean shotArrow(PlayerModel model) {
        Arrow arrow = model.getReference().launchProjectile(Arrow.class);
        double scale = (Double) getMetaByName(model, MetaConstants.META_SA_SHOT_STRENGTH);
        
        arrow.setVelocity(arrow.getVelocity().multiply(scale));
        boolean isExplosive = (Boolean) getMetaByName(model, MetaConstants.META_SA_SHOT_EXPLOSIVE);
        arrow.setMetadata(RoleConstant.SYBIL_ATTACKER_EXPLOSIVE, new FixedMetadataValue(Nftilation.getInstance(), isExplosive));
        return true;
    }

    @Override
    public String getRoleID() {
        return "SA";
    }
    
    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> params) {
        List<ItemStack> items = new ArrayList<>();
        ItemStack pistol = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta pistolMeta = pistol.getItemMeta();
        pistolMeta.setDisplayName(ChatColor.DARK_PURPLE + "Пистолет Макарова");
        pistol.setItemMeta(pistolMeta);
        NBT.modify(pistol, (nbt) -> {
            nbt.setString(RoleConstant.SYBIL_ATTACKER_NBT_CONTAINER, RoleConstant.SYBIL_ATTACKER_PISTOL);
            nbt.setString(RoleConstant.ROLE, getRoleID());
        });
        items.add(pistol);
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
            AttackPresets.summonExplosion(location, shooter, range, power);
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
