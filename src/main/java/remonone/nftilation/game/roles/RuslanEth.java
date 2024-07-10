package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import jdk.nashorn.internal.ir.Block;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.constants.DataConstants;
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
        return Arrays.asList("This is a ruslan.eth");
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
        player.setHealthScale(14.0D);
    }
    
    @Override
    public List<ItemStack> getAbilityItems(int upgradeLevel) {
        ItemStack snowball = new ItemStack(Material.SNOW_BALL);
        return Arrays.asList(snowball);
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
        PlayerData playerData = Store.getInstance().getDataInstance().FindPlayerByName(player.getName());
        GameInstance.PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(playerData.getTeam().getTeamName(), player);
        projectile.setMetadata("invoker", new FixedMetadataValue(Nftilation.getInstance(), model));
        projectile.setMetadata("invokerTeam", new FixedMetadataValue(Nftilation.getInstance(), playerData.getTeam().getTeamName()));
    
    }
    
    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent e) {
        Entity entity = e.getEntity();
        GameInstance.PlayerModel model = (GameInstance.PlayerModel)entity.getMetadata("invoker").get(0).value();
        if(model == null) return;
        String invokerTeam = (String)entity.getMetadata("invokerTeam").get(0).value();
        Player player = model.getReference();
        if(!entitiesList.get(player.getUniqueId()).isEmpty()) {
            entitiesList.get(player.getUniqueId()).forEach(EntityList::removeEntity);
            entitiesList.get(player.getUniqueId()).clear();
        }
        int bodyCount = 2 * model.getUpgradeLevel();
        Location spawnPoint = null;
        if(e.getHitBlock() != null) {
            spawnPoint = e.getHitBlock().getLocation();            
        }
        if(e.getHitEntity() != null) {
            spawnPoint = e.getHitEntity().getLocation();
        }
        if(spawnPoint == null) return;
        for(int i = 0; i < bodyCount; i++) {
            Location nearestEmptyBlock = BlockUtils.getNearestEmptySpace(spawnPoint.getBlock(), 5).getLocation();
            Blaze blaze = player.getWorld().spawn(spawnPoint, Blaze.class);
            blaze.setMetadata("ruslan", new FixedMetadataValue(Nftilation.getInstance(), invokerTeam));
            blaze.setMetadata("host", new FixedMetadataValue(Nftilation.getInstance(), player));
            EntityList.addEntity(blaze);
            entitiesList.get(player.getUniqueId()).add(blaze);
        }
        List<ItemStack> stack = new ArrayList<>();
        stack.add(getCallbackItem());
        if(model.getUpgradeLevel() > 2) {
            stack.add(getExplodeItem());
        }
        ItemStack[] stacks = stack.toArray(new ItemStack[0]);
        Store.getInstance().getDataInstance().getPlayerParams(player.getName()).put("actions", stacks);
        for(ItemStack stack1 : stacks) {
            player.getInventory().addItem(stack1);
        }
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                RemoveRuslanActionItems(player);
                player.getInventory().addItem(new ItemStack(Material.SNOW_BALL));
            }
        };
        task.runTaskLater(Nftilation.getInstance(), 5 * DataConstants.TICKS_IN_SECOND);
        int taskId = task.getTaskId();
        Store.getInstance().getDataInstance().getPlayerParams(player.getName()).put("taskId", taskId);
    }
    
    private ItemStack getExplodeItem() {
        ItemStack itemStack = new ItemStack(Material.FIREBALL);
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.DARK_RED + "CABOOM");
        itemStack.setItemMeta(itemMeta);
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
        PlayerData playerData = Store.getInstance().getDataInstance().FindPlayerByName(target.getName());
        if(playerData == null || playerData.getTeam().getTeamName().equals(team)) {
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
        Player host = (Player)blaze.getMetadata("host").get(0).value();
        PlayerData playerData = Store.getInstance().getDataInstance().FindPlayerByName(player.getName());
        e.setCancelled(true);
        if(playerData == null || playerData.getTeam().getTeamName().equals(team)) {
            return;
        }
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, host, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 2D);
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
            }
            case "explode": {

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
