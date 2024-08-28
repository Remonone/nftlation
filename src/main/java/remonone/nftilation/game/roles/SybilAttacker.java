package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.NameConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.ITeam;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.ColorUtils;
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
    
    @SuppressWarnings("unchecked")
    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> params) {
        List<ItemStack> items = new ArrayList<>();
        ItemStack bow = new ItemStack(Material.BOW);
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        Map<String, Object> abilityNbt = (Map<String, Object>) getMetaInfo(MetaConstants.META_SA_ABILITY_NBT, upgradeLevel);
        List<RoleItemDispenser.EnchantInfo> enchantInfos = (List<RoleItemDispenser.EnchantInfo>) getMetaInfo(MetaConstants.META_SA_ABILITY_ENCHANTS, upgradeLevel);
        NBT.modify(bow, nbt -> {
            for (Map.Entry<String, Object> entry : abilityNbt.entrySet()) {
                nbt.setDouble(entry.getKey(), (Double)entry.getValue());
            }
        });
        for(RoleItemDispenser.EnchantInfo info : enchantInfos) {
            bow.addUnsafeEnchantment(Enchantment.getByName(info.getEnchantment()), info.getLevel());
        }
        ItemMeta bowMeta = bow.getItemMeta();
        String name = (String) Optional.of(getMetaInfo(MetaConstants.META_SA_ABILITY_NAME, upgradeLevel)).orElse(NameConstants.NULL_STRING);
        bowMeta.setDisplayName(name);
        bowMeta.setUnbreakable(true);
        bowMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        bow.setItemMeta(bowMeta);
        items.add(bow);
        ItemStack arrows = new ItemStack(Material.ARROW);
        int arrowAmount = (Integer) Optional.of(getMetaInfo(MetaConstants.META_SA_ABILITY_AMOUNT, upgradeLevel)).orElse(1);
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
        int power = NBT.get(e.getBow(), nbt -> (Integer) nbt.getInteger("power"));
        if(power >= 1) {
            // Make handler for arrow explosion power
            e.getProjectile().setMetadata("arrow-power", new FixedMetadataValue(Nftilation.getInstance(), power));
        }
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

            Firework firework = e.getEntity().getWorld().spawn(location, Firework.class);
            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            fireworkMeta.setPower(10);
            fireworkMeta.addEffect(FireworkEffect.builder().flicker(true).trail(true).withColor(ColorUtils.TranslateToColor(ChatColor.getByChar(team.getTeamColor()))).with(FireworkEffect.Type.BALL_LARGE).build());
            firework.setFireworkMeta(fireworkMeta);
            firework.setMetadata("sybil_attacker", new FixedMetadataValue(Nftilation.getInstance(), 10));
            EntityHandleComponent.setEntityOwner(firework, shooter);
            firework.detonate();
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
