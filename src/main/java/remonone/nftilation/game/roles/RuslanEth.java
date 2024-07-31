package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import net.minecraft.server.v1_12_R1.EntityBlaze;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.components.ItemStatModifierComponent;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.mob.RuslanBlaze;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.BlockUtils;
import remonone.nftilation.utils.EntityList;
import remonone.nftilation.utils.InventoryUtils;
import remonone.nftilation.utils.PlayerUtils;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class RuslanEth extends Role {
    
    private final Random random = new Random(System.currentTimeMillis());
    
    private final Map<UUID, List<LivingEntity>> entitiesList = new HashMap<>();
    
    private final List<PotionEffectType> negativeEffects = Arrays.asList(PotionEffectType.CONFUSION, PotionEffectType.BLINDNESS, PotionEffectType.POISON, PotionEffectType.SLOW, PotionEffectType.SLOW_DIGGING, PotionEffectType.WITHER, PotionEffectType.HARM, PotionEffectType.WEAKNESS, PotionEffectType.HUNGER, PotionEffectType.LEVITATION);
    
    @Override
    public Material getMaterial() {
        return Material.BLAZE_ROD;
    }

    @Override
    public String getRoleName() {
        return "Ruslan.eth";
    }

    @Override
    public List<String> getRoleDescription() {
        return Arrays.asList(RoleConstant.RUSLAN_DESCRIPTION_1, RoleConstant.RUSLAN_DESCRIPTION_2, RoleConstant.RUSLAN_DESCRIPTION_3);
    }

    @Override
    public String getRoleID() {
        return "RE";
    }

    @Override
    public int getRoleIndex() {
        return 29;
    }
    
    
    
    @Override
    public void setPlayer(Player player, Map<String, Object> params) {
        if(!entitiesList.containsKey(player.getUniqueId())) {
            entitiesList.put(player.getUniqueId(), new ArrayList<>());
        }
        player.setHealthScaled(true);
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        float health = 12.0F + 2 * upgradeLevel;
        player.setHealthScale(health);
    }
    
    @Override
    public List<ItemStack> getAbilityItems(Map<String, Object> params) {
        ItemStack snowball = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = snowball.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName(RoleConstant.RUSLAN_ABILITY);
        snowball.setItemMeta(meta);
        NBT.modify(snowball, (nbt) -> {nbt.setString("ruslan", "summon");});
        return Collections.singletonList(snowball);
    }
    
    @Override
    public void killPlayer(Player player) {
        entitiesList.get(player.getUniqueId()).forEach(Entity::remove);
        entitiesList.get(player.getUniqueId()).clear();
        RemoveRuslanActionItems(player);
        if(player.getInventory().contains(Material.SNOW_BALL)) {
            player.getInventory().remove(Material.SNOW_BALL);
        }
        Map<String, Object> params = Store.getInstance().getDataInstance().getPlayerParams(player.getUniqueId());
        int taskId = (int) params.getOrDefault("taskId", -1);
        if(taskId != -1) return;
        getServer().getScheduler().cancelTask(taskId);
        params.remove("taskId");
    }
    
    @EventHandler
    public void onBlazeSummon(PlayerInteractEvent e) {
        ItemStack interactee = e.getItem();
        if(interactee == null || interactee.getType().equals(Material.AIR) || interactee.getAmount() < 1) return;
        Player player = e.getPlayer();
        if(!NBT.get(interactee, (nbt) -> (String)nbt.getString("ruslan")).equals("summon")) return;
        if(InventoryUtils.isCooldownRemain(interactee)) {
            InventoryUtils.notifyAboutCooldown(player, interactee);
            return;
        }
        Snowball snowball = player.launchProjectile(Snowball.class);
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        EntityHandleComponent.setEntityOwner(snowball, player);
        snowball.setMetadata("invokerTeam", new FixedMetadataValue(Nftilation.getInstance(), team));
        InventoryUtils.setCooldownForItem(interactee, RoleConstant.RUSLAN_SPAWN_CLONES_COOLDOWN);
    }
    
    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent e) {
        Player owner = EntityHandleComponent.getEntityOwner(e.getEntity());
        if(owner == null) return;
        
        Role role = Store.getInstance().getDataInstance().getPlayerRole(owner.getUniqueId());
        
        if(!(role instanceof RuslanEth)) return;
        
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
        int upgradeLevel = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        int bodyCount = 2 * upgradeLevel;
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
        List<ItemStack> stack = new ArrayList<>();
        stack.add(getCallbackItem());
        if(upgradeLevel > 2) {
            stack.add(getExplodeItem());
        }
        ItemStack[] stacks = stack.toArray(new ItemStack[0]);
        Store.getInstance().getDataInstance().getPlayerParams(owner.getUniqueId()).put("actions", stacks);
        for(ItemStack stack1 : stacks) {
            owner.getInventory().addItem(stack1);
        }
    }
    
    private ItemStack getExplodeItem() {
        ItemStack itemStack = new ItemStack(Material.FIREBALL);
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.DARK_RED + "БА-БАХ");
        itemStack.setItemMeta(itemMeta);
        ItemStatModifierComponent.markItemAsUncraftable(itemStack);
        ItemStatModifierComponent.markItemAsUnstorable(itemStack);
        ItemStatModifierComponent.markItemAsUndroppable(itemStack);
        NBT.modify(itemStack, (nbt) -> {
            nbt.setString("ruslan_action", "explode");
        });
        return itemStack;
    }
    
    private ItemStack getCallbackItem() {
        ItemStack itemStack = new ItemStack(Material.BLAZE_ROD);
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.BLUE + "Мобилизация");
        itemStack.setItemMeta(itemMeta);
        ItemStatModifierComponent.markItemAsUncraftable(itemStack);
        ItemStatModifierComponent.markItemAsUnstorable(itemStack);
        ItemStatModifierComponent.markItemAsUndroppable(itemStack);
        NBT.modify(itemStack, (nbt) -> {
            nbt.setString("ruslan_action", "recall");
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
            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent((Entity)fireball.getShooter(), livingEntity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 2D);
            if(targetBlaze.getTeam().equals(team)) return;
            getServer().getPluginManager().callEvent(event);
            if(!event.isCancelled()) {
                livingEntity.setHealth(livingEntity.getHealth() - event.getFinalDamage());
                livingEntity.setLastDamageCause(event);
                livingEntity.addPotionEffect(new PotionEffect(getRandomNegativeEffect(), 5 * DataConstants.TICKS_IN_SECOND, 1, false, true));
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onBlazeShoot(final EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Fireball)) return;
        Fireball fireball = (Fireball) e.getDamager();
        if(!(fireball.getShooter() instanceof Blaze)) return;
        Blaze blaze = (Blaze) fireball.getShooter();
        
        if(!(e.getEntity() instanceof LivingEntity)) return;
        LivingEntity livingEntity = (LivingEntity) e.getEntity();
        EntityBlaze entityBlaze = ((CraftBlaze)blaze).getHandle();
        if(!(entityBlaze instanceof RuslanBlaze)) {
            return;
        }
        RuslanBlaze ruslanBlaze = (RuslanBlaze) entityBlaze;
        Player host = (Player) ruslanBlaze.getOwner().getBukkitEntity();
        String team = ruslanBlaze.getTeam();
        if(host == null) {
            return;
        }
        if(livingEntity instanceof Blaze) return;
        if(livingEntity instanceof Player) {
            String teamName = Store.getInstance().getDataInstance().getPlayerTeam(livingEntity.getUniqueId());
            if(StringUtils.isEmpty(teamName) || teamName.equals(team)) {
                e.setCancelled(true);
                return;
            }
        }
        e.setCancelled(true);
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(host, livingEntity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 2D);
        getServer().getPluginManager().callEvent(event);
        if(!event.isCancelled()) {
            livingEntity.setHealth(livingEntity.getHealth() - event.getFinalDamage());
            livingEntity.setLastDamageCause(event);
            livingEntity.addPotionEffect(new PotionEffect(getRandomNegativeEffect(), 5 * DataConstants.TICKS_IN_SECOND, 1, false, true));
        }
    }
    
    private PotionEffectType getRandomNegativeEffect() {
        return negativeEffects.get(random.nextInt(negativeEffects.size()));
    }
    
    @EventHandler
    public void onItemInteract(final PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if(!(Store.getInstance().getDataInstance().getPlayerRole(player.getUniqueId()) instanceof RuslanEth)) {
            return;
        }
        ItemStack stack = e.getItem();
        if (stack == null || stack.getAmount() < 1 || stack.getType() == Material.AIR) {
            return;
        }
        String action = NBT.get(stack, (nbt) -> (String)nbt.getString("ruslan_action"));
        if(StringUtils.isEmpty(action)) return;
        switch(action) {
            case "recall": {
                Location playerLocation = player.getLocation();
                List<LivingEntity> entities = entitiesList.get(player.getUniqueId());
                entities.forEach(entity -> entity.teleport(playerLocation));
                RemoveRuslanActionItems(player);
                break;
            }
            case "explode": {
                List<LivingEntity> blazes = entitiesList.get(player.getUniqueId());
                for(LivingEntity entity : blazes) {
                    if(entity.getHealth() < .5F) {
                        continue;
                    }
                    Location loc = entity.getLocation();
                    entity.remove();
                    TNTPrimed tnt = player.getWorld().spawn(loc, TNTPrimed.class);
                    tnt.setYield(RoleConstant.RUSLAN_CLONE_EXPLOSION_STRENGTH);
                    tnt.setFuseTicks(0);
                    EntityHandleComponent.setEntityOwner(tnt, player);
                    tnt.setIsIncendiary(true);
                    AreaEffectCloud area = player.getWorld().spawn(loc, AreaEffectCloud.class);
                    area.setCustomName(RoleConstant.RUSLAN_AREA_EFFECT_NAME);
                    EntityHandleComponent.setEntityOwner(area, player);
                    area.addCustomEffect(new PotionEffect(getRandomNegativeEffect(), 10 * DataConstants.TICKS_IN_SECOND, 1, false, true), true);
                    area.setRadius(RoleConstant.RUSLAN_NEGATIVE_AREA_RADIUS);
                    area.setDuration(25);
                }
                entitiesList.get(player.getUniqueId()).clear();
                RemoveRuslanActionItems(player);
            }
        }
    }
    
    private void RemoveRuslanActionItems(Player player) {
        Spliterator<ItemStack> itemStackSpliterator = player.getInventory().spliterator();
        while(itemStackSpliterator.tryAdvance(itemStack -> {
            if(itemStack == null || itemStack.getType().equals(Material.AIR) || itemStack.getAmount() < 1) return;
            if(!StringUtils.isEmpty(NBT.get(itemStack, (nbt) -> (String)nbt.getString("ruslan_action")))) {
                player.getInventory().remove(itemStack);
            }
            ItemStack offHand = player.getInventory().getItemInMainHand();
            if(offHand == null || offHand.getType().equals(Material.AIR) || offHand.getAmount() < 1) return;
            if(!StringUtils.isEmpty(NBT.get(offHand, (nbt) -> (String)nbt.getString("ruslan_action")))) {
                player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            }
        }));
    }
}
