package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.scheduler.BukkitRunnable;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.components.ItemStatModifierComponent;
import remonone.nftilation.components.OwnerHandleComponent;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.utils.BlockUtils;
import remonone.nftilation.utils.EntityList;

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
    public void setPlayer(Player player, int upgradeLevel) {
        if(!entitiesList.containsKey(player.getUniqueId())) {
            entitiesList.put(player.getUniqueId(), new ArrayList<>());
        }
        player.setHealthScaled(true);
        float health = 12.0F + 2 * upgradeLevel;
        player.setHealthScale(health);
    }
    
    @Override
    public List<ItemStack> getAbilityItems(int upgradeLevel) {
        ItemStack snowball = new ItemStack(Material.SNOW_BALL);
        return Collections.singletonList(snowball);
    }
    
    @Override
    public void killPlayer(Player player, int upgradeLevel) {
        entitiesList.get(player.getUniqueId()).forEach(Entity::remove);
        entitiesList.get(player.getUniqueId()).clear();
    }
    
    @EventHandler
    public void onProjectileLaunch(final ProjectileLaunchEvent e) {
        Projectile projectile = e.getEntity();
        if(!(projectile.getShooter() instanceof Player)) return;
        Player player = (Player) projectile.getShooter();
        if (!(Store.getInstance().getDataInstance().getPlayerRole(player.getName()) instanceof RuslanEth)) return;
        if(projectile.getType() != EntityType.SNOWBALL) return;
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getName());
        OwnerHandleComponent.setEntityOwner(projectile, player);
        projectile.setMetadata("invokerTeam", new FixedMetadataValue(Nftilation.getInstance(), team));
       
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                RemoveRuslanActionItems(player);
                String teamName = Store.getInstance().getDataInstance().getPlayerTeam(player.getName());
                GameInstance.PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(teamName, player);
                giveAbilityItems(player, model.getUpgradeLevel());
            }
        };
        task.runTaskLater(Nftilation.getInstance(), RoleConstant.RUSLAN_SPAWN_CLONES_COOLDOWN * DataConstants.TICKS_IN_SECOND);
        int taskId = task.getTaskId();
        Store.getInstance().getDataInstance().getPlayerParams(player.getName()).put("taskId", taskId);
    }
    
    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent e) {
        Player owner = OwnerHandleComponent.getEntityOwner(e.getEntity());
        if(owner == null) return;
        
        if(!entitiesList.get(owner.getUniqueId()).isEmpty()) {
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
        String teamName = Store.getInstance().getDataInstance().getPlayerTeam(owner.getName());
        GameInstance.PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(teamName, owner);
        int bodyCount = 2 * model.getUpgradeLevel();
        nearestEmptyBlock.setX(nearestEmptyBlock.getX() + .5F);
        nearestEmptyBlock.setZ(nearestEmptyBlock.getZ() + .5F);
        for(int i = 0; i < bodyCount; i++) {
            Blaze blaze = owner.getWorld().spawn(nearestEmptyBlock, Blaze.class);
            blaze.setMetadata("ruslan", new FixedMetadataValue(Nftilation.getInstance(), teamName));
            OwnerHandleComponent.setEntityOwner(blaze, owner);
            EntityList.addEntity(blaze);
            entitiesList.get(owner.getUniqueId()).add(blaze);
        }
        List<ItemStack> stack = new ArrayList<>();
        stack.add(getCallbackItem());
        if(model.getUpgradeLevel() > 2) {
            stack.add(getExplodeItem());
        }
        ItemStack[] stacks = stack.toArray(new ItemStack[0]);
        Store.getInstance().getDataInstance().getPlayerParams(owner.getName()).put("actions", stacks);
        for(ItemStack stack1 : stacks) {
            owner.getInventory().addItem(stack1);
        }
    }
    
    private ItemStack getExplodeItem() {
        ItemStack itemStack = new ItemStack(Material.FIREBALL);
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.DARK_RED + "CABOOM");
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
        itemMeta.setDisplayName(ChatColor.BLUE + "Recall team");
        itemStack.setItemMeta(itemMeta);
        ItemStatModifierComponent.markItemAsUncraftable(itemStack);
        ItemStatModifierComponent.markItemAsUnstorable(itemStack);
        ItemStatModifierComponent.markItemAsUndroppable(itemStack);
        NBT.modify(itemStack, (nbt) -> {
            nbt.setString("ruslan_action", "recall");
        });
        return itemStack;
    }
    
    
    @EventHandler
    public void onEventTarget(final EntityTargetEvent e) {
        if(!(e.getEntity() instanceof Blaze)) return;
        if(!(e.getTarget() instanceof Player)) return;
        Player target = (Player) e.getTarget();
        Blaze blaze = (Blaze) e.getEntity();
        String team = (String)blaze.getMetadata("ruslan").get(0).value();
        String teamName = Store.getInstance().getDataInstance().getPlayerTeam(target.getName());
        if(StringUtils.isEmpty(teamName) || teamName.equals(team)) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlazeShoot(final EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Fireball)) return;
        Fireball fireball = (Fireball) e.getDamager();
        if(!(fireball.getShooter() instanceof Blaze)) return;
        Blaze blaze = (Blaze) fireball.getShooter();
        if(!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        String team = (String)blaze.getMetadata("ruslan").get(0).value();
        Player host = OwnerHandleComponent.getEntityOwner(blaze);
        if(host == null) {
            return;
        }
        String teamName = Store.getInstance().getDataInstance().getPlayerTeam(player.getName());
        e.setCancelled(true);
        if(StringUtils.isEmpty(teamName) || teamName.equals(team)) {
            return;
        }
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(host, player, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 2D);
        getServer().getPluginManager().callEvent(event);
        if(!event.isCancelled()) {
            player.setHealth(player.getHealth() - event.getFinalDamage());
            player.setLastDamageCause(event);
            player.addPotionEffect(new PotionEffect(getRandomNegativeEffect(), 5 * DataConstants.TICKS_IN_SECOND, 1, false, true));
        }
    }
    
    private PotionEffectType getRandomNegativeEffect() {
        return negativeEffects.get(random.nextInt(negativeEffects.size()));
    }
    
    @EventHandler
    public void onItemInteract(final PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if(!(Store.getInstance().getDataInstance().getPlayerRole(player.getName()) instanceof RuslanEth)) {
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
                    tnt.setMetadata("invoker", new FixedMetadataValue(Nftilation.getInstance(), player));
                    tnt.setIsIncendiary(true);
                    AreaEffectCloud area = player.getWorld().spawn(loc, AreaEffectCloud.class);
                    area.setCustomName(RoleConstant.RUSLAN_AREA_EFFECT_NAME);
                    area.setMetadata("invoker", new FixedMetadataValue(Nftilation.getInstance(), player));
                    area.addCustomEffect(new PotionEffect(getRandomNegativeEffect(), 10 * DataConstants.TICKS_IN_SECOND, 1, false, true), true);
                    area.setRadius(RoleConstant.RUSLAN_NEGATIVE_AREA_RADIUS);
                }
            }
        }
    }
    
    private void RemoveRuslanActionItems(Player player) {
        ItemStack[] stacks = (ItemStack[])Store.getInstance().getDataInstance().getPlayerParams(player.getName()).getOrDefault("actions", new ItemStack[0]);
        for(ItemStack stack : stacks) {
            player.getInventory().remove(stack);
        }
        Store.getInstance().getDataInstance().getPlayerParams(player.getName()).remove("actions");
        
    }
}
