package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftBlaze;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.components.ItemStatModifierComponent;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.constants.*;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.damage.RuslanBlazeDamageInvoker;
import remonone.nftilation.game.mob.RuslanBlaze;
import remonone.nftilation.game.models.IDamageInvoker;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.AttackPresets;
import remonone.nftilation.utils.BlockUtils;
import remonone.nftilation.utils.EntityList;
import remonone.nftilation.utils.PlayerUtils;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class RuslanEth extends Role {
    private final Map<UUID, List<LivingEntity>> entitiesList = new HashMap<>();
    
    public RuslanEth() {
        super("RE");
        super.registerHandlers(new HashMap<String, IAbilityHandler>(){{
            put(RoleConstant.RUSLAN_SUMMON, new IAbilityHandler() {
                @Override
                public boolean executeHandle(PlayerModel model) {
                    return onBlazeSummon(model);
                }
                @Override
                public float getCooldown(PlayerModel model) {
                    return ((Double)getMetaByName(model, MetaConstants.META_RE_SUMMON_COOLDOWN)).floatValue();
                }
            });
            put(RoleConstant.RUSLAN_MOBILIZATION, new IAbilityHandler() {
                @Override
                public boolean executeHandle(PlayerModel model) {
                    return onBlazeReturn(model);
                }
                @Override
                public float getCooldown(PlayerModel model) {
                    return 0;
                }
            });
            put(RoleConstant.RUSLAN_EXPLOSION, new IAbilityHandler() {
                @Override
                public boolean executeHandle(PlayerModel model) {
                    return onBlazeExplode(model);
                }
                @Override
                public float getCooldown(PlayerModel model) {
                    return 0;
                }
            });
        }}, RoleConstant.RUSLAN_NBT_CONTAINER);
    }
    
    @Override
    public String getRoleID() {
        return "RE";
    }
    
    @Override
    public void setPlayer(Player player, Map<String, Object> params) {
        super.setPlayer(player, params);
        if(!entitiesList.containsKey(player.getUniqueId())) {
            entitiesList.put(player.getUniqueId(), new ArrayList<>());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> params) {
        ItemStack summonAbility = new ItemStack(Material.BLAZE_POWDER);
        int upgradeLevel = (int)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        String name = (String)getMetaInfo(MetaConstants.META_RE_SUMMON_NAME, upgradeLevel);
        List<String> description = (List<String>) getMetaInfo(MetaConstants.META_RE_SUMMON_DESCRIPTION, upgradeLevel);
        ItemMeta meta = summonAbility.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName(name);
        meta.setLore(description);
        summonAbility.setItemMeta(meta);
        NBT.modify(summonAbility, (nbt) -> {nbt.setString(RoleConstant.RUSLAN_NBT_CONTAINER, RoleConstant.RUSLAN_SUMMON);});
        return Collections.singletonList(summonAbility);
    }
    
    @Override
    public void killPlayer(Player player) {
        entitiesList.get(player.getUniqueId()).forEach(Entity::remove);
        entitiesList.get(player.getUniqueId()).clear();
        removeRuslanActionItems(player);
        PlayerModel model = PlayerUtils.getModelFromPlayer(player);
        Map<String, Object> params = model.getParameters();
        int taskId = (int) params.getOrDefault("taskId", -1);
        if(taskId != -1) return;
        getServer().getScheduler().cancelTask(taskId);
        params.remove("taskId");
    }
    
    public boolean onBlazeSummon(PlayerModel model) {
        Player player = model.getReference();
        Snowball snowball = player.launchProjectile(Snowball.class);
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        EntityHandleComponent.setEntityOwner(snowball, player);
        float cooldown = ((Double)getMetaByName(model, MetaConstants.META_RE_SUMMON_COOLDOWN)).floatValue();
        new BukkitRunnable() {
            @Override
            public void run() {
                removeRuslanActionItems(player);
            }
        }.runTaskLater(Nftilation.getInstance(), (int)(cooldown * DataConstants.TICKS_IN_SECOND));
        snowball.setMetadata("invokerTeam", new FixedMetadataValue(Nftilation.getInstance(), team));
        return true;
    }
    
    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent e) {
        Player owner = EntityHandleComponent.getEntityOwner(e.getEntity());
        if(owner == null) return;
        
        Role role = Store.getInstance().getDataInstance().getPlayerRole(owner.getUniqueId());
        
        if(!(role instanceof RuslanEth)) return;
        removeRuslanActionItems(owner);
        if(entitiesList.containsKey(owner.getUniqueId()) || !entitiesList.get(owner.getUniqueId()).isEmpty()) {
            entitiesList.get(owner.getUniqueId()).forEach(EntityList::removeEntity);
            entitiesList.get(owner.getUniqueId()).clear();
        }
        Location spawnPoint = null;
        if(e.getHitBlock() != null) {
            spawnPoint = e.getHitBlock().getLocation();            
        }
        if(e.getHitEntity() != null) {
            spawnPoint = e.getHitEntity().getLocation();
        }
        if(spawnPoint == null) return;
        Location nearestEmptyBlock = BlockUtils.getNearestEmptySpace(spawnPoint.getBlock(), 1);
        if(nearestEmptyBlock == null) {
            owner.playSound(owner.getLocation(), Sound.ENTITY_BLAZE_HURT, 1, .8f);
            owner.sendMessage(MessageConstant.CANNOT_SPAWN_BLAZE);
            return;
        }
        String teamName = Store.getInstance().getDataInstance().getPlayerTeam(owner.getUniqueId());
        PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(teamName, owner);
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        int bodyCount = (Integer)getMetaByName(model, MetaConstants.META_RE_SUMMON_AMOUNT);
        nearestEmptyBlock.setX(nearestEmptyBlock.getX() + .5F);
        nearestEmptyBlock.setZ(nearestEmptyBlock.getZ() + .5F);
        for(int i = 0; i < bodyCount; i++) {
            RuslanBlaze blaze = new RuslanBlaze(nearestEmptyBlock, teamName, ((CraftPlayer)owner).getHandle());
            LivingEntity entity = (LivingEntity) blaze.getBukkitEntity();
            ((CraftWorld)nearestEmptyBlock.getWorld()).getHandle().addEntity(blaze, CreatureSpawnEvent.SpawnReason.CUSTOM);
            EntityHandleComponent.setEntityOwner(entity, owner);
            EntityList.addEntity(entity);
            entitiesList.get(owner.getUniqueId()).add(entity);
        }
        owner.getInventory().addItem(getCallbackItem(model));
        if((Boolean)getMetaByName(model, MetaConstants.META_RE_EXPLOSION_AVAILABILITY)) {
            owner.getInventory().addItem(getExplodeItem(model));
        }
    }
    
    @SuppressWarnings("unchecked")
    private ItemStack getExplodeItem(PlayerModel model) {
        ItemStack itemStack = new ItemStack(Material.FIREBALL);
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        String name = (String)getMetaByName(model, MetaConstants.META_RE_EXPLOSION_NAME);
        List<String> description = (List<String>) getMetaByName(model, MetaConstants.META_RE_EXPLOSION_DESCRIPTION);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(description);
        itemStack.setItemMeta(itemMeta);
        ItemStatModifierComponent.markItemAsUncraftable(itemStack);
        ItemStatModifierComponent.markItemAsUnstorable(itemStack);
        ItemStatModifierComponent.markItemAsUndroppable(itemStack);
        NBT.modify(itemStack, (nbt) -> {
            nbt.setString(RoleConstant.RUSLAN_NBT_CONTAINER, RoleConstant.RUSLAN_EXPLOSION);
            nbt.setString(RoleConstant.ROLE, getRoleID());
        });
        return itemStack;
    }
    
    @SuppressWarnings("unchecked")
    private ItemStack getCallbackItem(PlayerModel model) {
        ItemStack itemStack = new ItemStack(Material.BLAZE_ROD);
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        String name = (String)getMetaByName(model, MetaConstants.META_RE_MOBILIZATION_NAME);
        List<String> description = (List<String>) getMetaByName(model, MetaConstants.META_RE_MOBILIZATION_DESCRIPTION);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(description);
        itemStack.setItemMeta(itemMeta);
        ItemStatModifierComponent.markItemAsUncraftable(itemStack);
        ItemStatModifierComponent.markItemAsUnstorable(itemStack);
        ItemStatModifierComponent.markItemAsUndroppable(itemStack);
        NBT.modify(itemStack, (nbt) -> {
            nbt.setString(RoleConstant.RUSLAN_NBT_CONTAINER, RoleConstant.RUSLAN_MOBILIZATION);
            nbt.setString(RoleConstant.ROLE, getRoleID());
        });
        return itemStack;
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onBlazeHit(final ProjectileHitEvent e) {
        if(!(e.getEntity() instanceof Fireball)) return;
        Fireball fireball = (Fireball) e.getEntity();
        if(!(fireball.getShooter() instanceof Blaze)) return;
        if(e.getHitBlock() != null) {
            return;
        }
        Entity entity = e.getHitEntity();
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity livingEntity = (LivingEntity) entity;
        if(livingEntity instanceof Blaze) {
            RuslanBlaze ruslanBlaze = (RuslanBlaze) ((CraftBlaze)fireball.getShooter()).getHandle();
            String team = ruslanBlaze.getTeam();
            RuslanBlaze targetBlaze = (RuslanBlaze) ((CraftBlaze)livingEntity).getHandle();
            if(targetBlaze.getTeam().equals(team)) return;
        }
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent((Entity)fireball.getShooter(), livingEntity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 2D);
        getServer().getPluginManager().callEvent(event);
        if(!event.isCancelled()) {
            livingEntity.setHealth(livingEntity.getHealth() - event.getFinalDamage());
            livingEntity.setLastDamageCause(event);
            livingEntity.addPotionEffect(new PotionEffect(RuslanBlazeDamageInvoker.getRandomNegativeEffect(), 5 * DataConstants.TICKS_IN_SECOND, 1, false, true));
        }
    }
    
    public boolean onBlazeReturn(PlayerModel model) {
        Player player = model.getReference();
        Location playerLocation = player.getLocation();
        List<LivingEntity> entities = entitiesList.get(player.getUniqueId());
        entities.forEach(entity -> entity.teleport(playerLocation));
        removeRuslanActionItems(player);
        return true;
    }
    
    public boolean onBlazeExplode(PlayerModel model) {
        Player player = model.getReference();
        List<LivingEntity> blazes = entitiesList.get(player.getUniqueId());
        for(LivingEntity entity : blazes) {
            if(entity.getHealth() < .5F) {
                continue;
            }
            Location loc = entity.getLocation();
            entity.remove();
            double range = (Double)getMetaByName(model, MetaConstants.META_RE_EXPLOSION_RANGE);
            double damage = (Double)getMetaByName(model, MetaConstants.META_RE_EXPLOSION_DAMAGE);
            float gasesRange = ((Double)getMetaByName(model,  MetaConstants.META_RE_EXPLOSION_GASES_RANGE)).floatValue();
            double gasesDuration = (Double)getMetaByName(model,  MetaConstants.META_RE_EXPLOSION_GASES_DURATION);
            AttackPresets.summonExplosion(loc, player, range, damage, 2, 10, 100, 1f, false);
            AreaEffectCloud area = player.getWorld().spawn(loc, AreaEffectCloud.class);
            area.setCustomName(RoleConstant.RUSLAN_AREA_EFFECT_NAME);
            EntityHandleComponent.setEntityOwner(area, player);
            area.addCustomEffect(new PotionEffect(RuslanBlazeDamageInvoker.getRandomNegativeEffect(), 10 * DataConstants.TICKS_IN_SECOND, 1, false, true), true);
            area.setRadius(gasesRange);
            area.setDuration((int)(gasesDuration * DataConstants.TICKS_IN_SECOND));
        }
        entitiesList.get(player.getUniqueId()).clear();
        removeRuslanActionItems(player);
        return true;
    }
    
    
    @SuppressWarnings("StatementWithEmptyBody")
    private void removeRuslanActionItems(Player player) {
        Spliterator<ItemStack> itemStackSpliterator = player.getInventory().spliterator();
        while(itemStackSpliterator.tryAdvance(itemStack -> {
            if(itemStack == null || itemStack.getType().equals(Material.AIR) || itemStack.getAmount() < 1) return;
            String nbtItem = NBT.get(itemStack, (nbt) -> (String)nbt.getString(RoleConstant.RUSLAN_NBT_CONTAINER));
            if(!StringUtils.isBlank(nbtItem) && !RoleConstant.RUSLAN_SUMMON.equals(nbtItem)) {
                player.getInventory().remove(itemStack);
            }
        }));
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if(offHand == null || offHand.getType().equals(Material.AIR) || offHand.getAmount() < 1) return;
        String nbtItem = NBT.get(offHand, (nbt) -> (String)nbt.getString(RoleConstant.RUSLAN_NBT_CONTAINER));
        if(!StringUtils.isBlank(nbtItem) && !RoleConstant.RUSLAN_SUMMON.equals(nbtItem)) {
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        }
    }

    @Override
    public List<IDamageInvoker> getDamageInvokers() {
        return Collections.singletonList(new RuslanBlazeDamageInvoker());
    }
}
