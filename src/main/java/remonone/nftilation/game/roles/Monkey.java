package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MetaConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.damage.MonkeyWandDamage;
import remonone.nftilation.game.models.EffectPotion;
import remonone.nftilation.game.models.IDamageInvoker;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.PlayerUtils;

import java.util.*;

public class Monkey extends Role {
    
    public Monkey() {
        super("MN");
        super.registerHandlers(new HashMap<String, IAbilityHandler>() {
            {
                put(RoleConstant.MONKEY_NBT_INVISIBILITY, new IAbilityHandler() {
                    @Override
                    public boolean executeHandle(PlayerModel model) {
                        return useInvisibilityItem(model);
                    }

                    @Override
                    public float getCooldown(PlayerModel model) {
                        return ((Double)getMetaByName(model, MetaConstants.META_MONKEY_INVISIBILITY_COOLDOWN)).floatValue();
                    }
                });
                put(RoleConstant.MONKEY_NBT_THROWER, new IAbilityHandler() {
                    @Override
                    public boolean executeHandle(PlayerModel model) {
                        return onThrowItemUsed(model);
                    }

                    @Override
                    public float getCooldown(PlayerModel model) {
                        return ((Double)getMetaByName(model, MetaConstants.META_MONKEY_THROWER_COOLDOWN)).floatValue();
                    }
                });
            }
        }, RoleConstant.MONKEY_NBT_CONTAINER);
    }
    
    @Override
    public String getRoleID() {
        return "MN";
    }

    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> params){
        List<ItemStack> items = new ArrayList<>();
        ItemStack itemStack = new ItemStack(Material.INK_SACK);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName(RoleConstant.MONKEY_ABILITY_ITEM);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        NBT.modify(itemStack, nbt -> {
            nbt.setString(RoleConstant.MONKEY_NBT_CONTAINER, RoleConstant.MONKEY_NBT_INVISIBILITY);
            nbt.setString(RoleConstant.ROLE, getRoleID());
        });
        items.add(itemStack);
        int level = (int)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        if(level > 1) {
            ItemStack thrower = new ItemStack(Material.BROWN_MUSHROOM);
            ItemMeta throwerMeta = thrower.getItemMeta();
            throwerMeta.setUnbreakable(true);
            throwerMeta.setDisplayName(RoleConstant.MONKEY_ABILITY_THROWER_ITEM);
            thrower.setItemMeta(throwerMeta);
            NBT.modify(thrower, nbt -> {
                nbt.setString(RoleConstant.MONKEY_NBT_CONTAINER, RoleConstant.MONKEY_NBT_THROWER);
                nbt.setString(RoleConstant.ROLE, getRoleID());
            });
            items.add(thrower);
        }
        return items;
    }
    
    @Override
    public void setPlayer(Player player, Map<String, Object> params) {
        super.setPlayer(player, params);
        player.setAllowFlight(true);
    }

    @EventHandler
    public void onPlayerFlight(final PlayerToggleFlightEvent e) {
        Player accessor = e.getPlayer();
        DataInstance data = Store.getInstance().getDataInstance();
        Role role = data.getPlayerRole(accessor.getUniqueId());
        if(!(role instanceof Monkey)) {
            return;
        }
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) {
            return;
        }
        PlayerModel model = PlayerUtils.getModelFromPlayer(accessor);
        Map<String, Object> params = model.getParameters();
        int level = (int)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        int jumpAmount = (Integer)getMetaInfo(MetaConstants.META_MONKEY_JUMP_COUNT, level);
        int count = (int)params.getOrDefault(RoleConstant.MONKEY_JUMP_COUNT, 0);
        if(count >= jumpAmount) {
            accessor.setAllowFlight(false);
        }
        params.put(RoleConstant.MONKEY_JUMP_COUNT, count + 1);
        e.setCancelled(true);
        accessor.setFlying(false);
        double acceleration = (double)getMetaInfo(MetaConstants.META_MONKEY_JUMP_TOSSING, level);
        double velocityUp = (double)getMetaInfo(MetaConstants.META_MONKEY_JUMP_ACCELERATION, level);
        double cooldown = (double)getMetaInfo(MetaConstants.META_MONKEY_JUMP_COOLDOWN, level);
        params.put("cooldown", System.currentTimeMillis() + (long)(cooldown * DataConstants.ONE_SECOND));
        accessor.setVelocity(accessor.getVelocity().multiply(acceleration).setY(velocityUp).add(accessor.getLocation().getDirection().normalize()));
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        Player accessor = e.getPlayer();
        DataInstance data = Store.getInstance().getDataInstance();
        Role role = data.getPlayerRole(accessor.getUniqueId());
        if(!(role instanceof Monkey)) {
            return;
        }
        CheckOnGround(accessor, e.getFrom(), e.getTo());
    }

    private void CheckOnGround(Player accessor, Location from, Location to) {
        PlayerModel model = PlayerUtils.getModelFromPlayer(accessor);
        Map<String, Object> params = model.getParameters();
        if(!(params.containsKey("cooldown") && ((long)params.get("cooldown") < System.currentTimeMillis()))) return;
        if((from.getBlockY()>to.getBlockY())
                && !(accessor.getLocation().add(0, -2, 0)
                .getBlock()
                .getType()
                .equals(Material.AIR))) {
            accessor.setAllowFlight(true);
            params.remove(RoleConstant.MONKEY_JUMP_COUNT);
        }
    }

    public boolean useInvisibilityItem(PlayerModel model) {
        int level = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        int length = (Integer) getMetaInfo(MetaConstants.META_MONKEY_INVISIBILITY_DURATION, level);
        Player player = model.getReference();
        World world = player.getWorld();
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, length * DataConstants.TICKS_IN_SECOND, 0, false, false));
        world.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, .5f, .8f);
        return true;
    }

    public boolean onThrowItemUsed(PlayerModel model) {
        Snowball snowball = model.getReference().launchProjectile(Snowball.class);
        snowball.setMetadata(RoleConstant.MONKEY_GRENADE, new FixedMetadataValue(Nftilation.getInstance(), model));
        snowball.setCustomName(RoleConstant.MONKEY_GRENADE_ENTITY_NAME);
        return true;
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void onSnowballHit(final ProjectileHitEvent e) {
        if(!(e.getEntity() instanceof Snowball)) return;
        Snowball snowball = (Snowball) e.getEntity();
        if(!snowball.hasMetadata(RoleConstant.MONKEY_GRENADE)) return;
        Entity entity = e.getHitEntity();
        if(entity == null) return;
        if(!(entity instanceof Player)) return;
        Player player = (Player) entity;
        PlayerModel model = (PlayerModel) snowball.getMetadata(RoleConstant.MONKEY_GRENADE).get(0).value();
        if(GameInstance.getInstance().checkIfPlayersInSameTeam(player, model.getReference())) return;
        List<EffectPotion> effects = (List<EffectPotion>) getMetaByName(model, MetaConstants.META_MONKEY_THROWER_EFFECTS);
        player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, .7f);
        model.getReference().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);
        for(EffectPotion effect : effects) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect.getEffect()), effect.getDuration(), effect.getStrength(), true, true));
        }

    }

    @Override
    public List<IDamageInvoker> getDamageInvokers() {
        return Collections.singletonList(new MonkeyWandDamage());
    }
}
