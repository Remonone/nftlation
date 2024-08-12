package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.PlayerUtils;

import java.util.*;

public class SybilAttacker extends Role {
    
    public SybilAttacker() {
        super("SA");
    }
    
    @Override
    public String getRoleID() {
        return "SA";
    }

    @Override
    public void setPlayer(Player player, Map<String, Object> params) {
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        if(upgradeLevel > 1) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, DataConstants.CONSTANT_POTION_DURATION, 1, false, false));
        }
    }
    
    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> params) {
        List<ItemStack> items = new ArrayList<>();
        ItemStack bow = new ItemStack(Material.BOW);
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        switch (upgradeLevel) {
            case 1:
                NBT.modify(bow, nbt -> {
                    nbt.setInteger("arrow-per-side", 1);
                    nbt.setFloat("set-step", 4F);
                });
                bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
                break;
            case 2:
                NBT.modify(bow, nbt -> {
                    nbt.setInteger("arrow-per-side", 2);
                    nbt.setFloat("set-step", 1.5F);
                });
                bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
                bow.addEnchantment(Enchantment.ARROW_DAMAGE, 2);
                break;
            case 3:
                NBT.modify(bow, nbt -> {nbt.setInteger("blowing-arrow", 1);});
                bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
                break;
        }
        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.setDisplayName(RoleConstant.SYBIL_BOW_NAME);
        bowMeta.setUnbreakable(true);
        bowMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        bow.setItemMeta(bowMeta);
        items.add(bow);
        ItemStack arrows = new ItemStack(Material.ARROW);
        int arrowAmount = upgradeLevel > 1 ? 1 : RoleConstant.SYBIL_ARROW_AMOUNT;
        arrows.setAmount(arrowAmount);
        items.add(arrows);
        return items;
    }

    @EventHandler
    public void onArrowShot(EntityShootBowEvent e) {
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        if(!(e.getEntity() instanceof Player)) return;
        Player accessor = (Player) e.getEntity();
        DataInstance data = Store.getInstance().getDataInstance();
        Role role = data.getPlayerRole(accessor.getUniqueId());
        EntityHandleComponent.setEntityOwner(e.getProjectile(), accessor);
        if(!(role instanceof SybilAttacker)) {
            return;
        }
        int amount = NBT.get(e.getBow(), nbt -> (Integer) nbt.getInteger("arrow-per-side"));
        if(amount >= 1) {
            float step = NBT.get(e.getBow(), nbt -> (Float) nbt.getFloat("set-step"));
            for(int i = 0; i < amount; i++) {
                SummonAdditionalArrow(accessor, e.getProjectile(), -step * (i + 1));
                SummonAdditionalArrow(accessor, e.getProjectile(), step * (i + 1));
            }
        }
    }

    @EventHandler
    public void onArrowHit(final ProjectileHitEvent e) {
        if(!(e.getEntity().getShooter() instanceof Player)) return;
        Player shooter = (Player) e.getEntity().getShooter();
        DataInstance data = Store.getInstance().getDataInstance();
        Role role = data.getPlayerRole(shooter.getUniqueId());
        if(!(role instanceof SybilAttacker)) return;
        String team = Store.getInstance().getDataInstance().getPlayerTeam(shooter.getUniqueId());
        PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, shooter);
        if(model == null) return;
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        int upgradeLevel = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
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
            TNTPrimed entity = e.getEntity().getWorld().spawn(location, TNTPrimed.class);
            entity.setMetadata("sybil_attacker", new FixedMetadataValue(Nftilation.getInstance(), 10));
            entity.setFuseTicks(0);
            EntityHandleComponent.setEntityOwner(entity, shooter);
            entity.setIsIncendiary(true);
            e.getEntity().remove();
        }
    }
    
    private void SummonAdditionalArrow(Entity entity, Entity arrow, float deflection) {
        Location position = arrow.getLocation();
        Location newPosition = position.clone();
        Player player = (Player)entity;
        float yaw = position.getYaw();

        double scale = arrow.getVelocity().getZ() / position.getDirection().getZ();
        Vector deflectedDirection = GetRotationVector(yaw+deflection);
        deflectedDirection.setY(-position.getDirection().getY());
        newPosition.setDirection(deflectedDirection);

        Arrow newInstance = entity.getWorld().spawn(newPosition, Arrow.class);
        EntityHandleComponent.setEntityOwner(newInstance, player);
        newInstance.setVelocity(new Vector(scale * deflectedDirection.getX(), scale * deflectedDirection.getY(), scale * deflectedDirection.getZ()));
        newInstance.setShooter(player);
    }

    private Vector GetRotationVector(float rotation) {
        double x = Math.sin(Clamp(rotation)*Math.PI/180);
        double z = Math.cos(Clamp(rotation)*Math.PI/180);
        return new Vector(x, 0, z);
    }

    private float Clamp(float value) {
        return value > 180 ? value - 360 : value < -180 ? value + 360 : value;
    }
}
