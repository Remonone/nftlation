package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.utils.InventoryUtils;
import remonone.nftilation.utils.VectorUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Guts extends Role {
    @Override
    public Material getMaterial() {
        return Material.DIAMOND_SWORD;
    }

    @Override
    public String getRoleName() {
        return "Guts";
    }

    @Override
    public List<String> getRoleDescription() {
        return Collections.singletonList("Ты знаешь что делать. ;)");
    }

    @Override
    public String getRoleID() {
        return "EM";
    }

    @Override
    public int getRoleIndex() {
        return 40;
    }

    @Override
    protected void setPlayer(Player player, int upgradeLevel) {
        player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(.5F);
        float genericArmor = 20 - (10 * (upgradeLevel - 1));
        float genericArmorToughness = 20 - (10 * (upgradeLevel - 1));
        float genericHealth = 60 - (upgradeLevel - 1) * 20;
        player.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(genericArmor);
        player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(genericArmorToughness);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(genericHealth);
        player.setHealth(genericHealth);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 1));
    }
    
    @Override
    protected ItemStack getHelmet(Player player, int upgradeLevel) {
        return new ItemStack(Material.AIR);
    }
    
    @Override
    protected ItemStack getChestplate(Player player, int upgradeLevel) {
        return new ItemStack(Material.AIR);
    }
    
    @Override
    protected ItemStack getLeggings(Player player, int upgradeLevel) {
        return new ItemStack(Material.AIR);
    }

    @Override
    protected ItemStack getBoots(Player player, int upgradeLevel) {
        return new ItemStack(Material.AIR);
    }
    
    @Override
    protected ItemStack getSword(int upgradeLevel) {
        ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
        weapon.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        ItemMeta meta = weapon.getItemMeta();
        meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.DARK_RED + "Меч Гатса");
        meta.setUnbreakable(true);
        weapon.setItemMeta(meta);
        NBT.modify(weapon, (nbt) -> {nbt.setString("gutsWeaponary", "weapon");});
        return weapon;
    }
    
    @Override
    protected List<ItemStack> getAbilityItems(int level) {
        ItemStack pistol = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta teleportMeta = pistol.getItemMeta();
        teleportMeta.setDisplayName(ChatColor.DARK_PURPLE + "Пушка");
        pistol.setItemMeta(teleportMeta);
        NBT.modify(pistol, (nbt) -> {nbt.setString("gutsShot", "shot");});
        ItemStack collapser = new ItemStack(Material.FERMENTED_SPIDER_EYE);
        ItemMeta meta = collapser.getItemMeta();
        meta.setDisplayName("Берсерк");
        collapser.setItemMeta(meta);
        NBT.modify(collapser, (nbt) -> {nbt.setString("gunsCollapse", "weapon");});
        return Arrays.asList(pistol, collapser);
    }
    
    @EventHandler
    public void onDamage(final EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        if(!(damager instanceof Player)) return;
        Player attacker = (Player) damager;
        Role role = Store.getInstance().getDataInstance().getPlayerRole(attacker.getUniqueId());
        if(!(role instanceof Guts)) return;
        if(!(e.getEntity() instanceof LivingEntity)) return;
        ItemStack stack = attacker.getInventory().getItemInMainHand();
        if(stack == null || stack.getAmount() < 1 || stack.getType().equals(Material.AIR)) return;
        String gutsWeapon = NBT.get(stack, (nbt) -> (String)nbt.getString("gutsWeaponary"));
        if(StringUtils.isEmpty(gutsWeapon)) return;
        LivingEntity damagee = (LivingEntity) e.getEntity();
        Vector attackerPosition = attacker.getLocation().toVector().clone();
        Vector targetPosition = damagee.getLocation().toVector().clone();
        Vector direction = targetPosition.subtract(attackerPosition);
        String team = Store.getInstance().getDataInstance().getPlayerTeam(attacker.getUniqueId());
        float knockbackScale = (GameInstance.getInstance().getPlayerModelFromTeam(team, attacker).getUpgradeLevel() > 2) ? 1.5F : 3F;
        damagee.setVelocity(damagee.getVelocity().add(direction.multiply(knockbackScale)));
    }
    
    @EventHandler
    public void onItemUse(final PlayerInteractEvent e) {
        Player user = e.getPlayer();
        Role role = Store.getInstance().getDataInstance().getPlayerRole(user.getUniqueId());
        if(!(role instanceof Guts)) return;
        ItemStack stack = e.getItem();
        if(stack == null || stack.getAmount() < 1 || stack.getType().equals(Material.AIR)) return;
        String gutsShot = NBT.get(stack, (nbt) -> (String)nbt.getString("gutsShot"));
        if(!StringUtils.isEmpty(gutsShot)) {
            shotArrow(stack, user);
            return;
        }
        String gunsCollapse = NBT.get(stack, (nbt) -> (String)nbt.getString("gunsCollapse"));
        if(!StringUtils.isEmpty(gunsCollapse)) {
            startBerserk(stack, user);
        }
    }

    private void startBerserk(ItemStack stack,  Player user) {
        if(InventoryUtils.isCooldownRemain(stack)) {
            InventoryUtils.notifyAboutCooldown(user, stack);
            return;
        }
        Vector destination = VectorUtils.getBlockPositionOnDirection(user.getWorld(), user.getEyeLocation().toVector(), user.getEyeLocation().getDirection(), 25F);
        if(destination == null) {
            user.sendMessage(ChatColor.RED + "Неправильно выбранная точка!");
            return;
        }
        user.setVelocity(new Vector(0, 1, 0));
        String team = Store.getInstance().getDataInstance().getPlayerTeam(user.getUniqueId());
        int upgradeLevel = GameInstance.getInstance().getPlayerModelFromTeam(team, user).getUpgradeLevel();
        new BukkitRunnable() {
            @Override
            public void run() {
                user.setVelocity(destination.add(user.getEyeLocation().getDirection()).subtract(user.getLocation().toVector().clone().subtract(new Vector(0, -3, 0))));
                user.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
                user.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 10));
            }
        }.runTaskLater(Nftilation.getInstance(), 10);
        new BukkitRunnable() {
            @Override
            public void run() {
                String team = Store.getInstance().getDataInstance().getPlayerTeam(user.getUniqueId());
                if(GameInstance.getInstance().getPlayerModelFromTeam(team, user).getUpgradeLevel() < 3) {
                    TNTPrimed explode = user.getWorld().spawn(user.getLocation(), TNTPrimed.class);
                    explode.setFuseTicks(0);
                    EntityHandleComponent.setEntityOwner(explode, user);
                }
            }
        }.runTaskLater(Nftilation.getInstance(), 15);
        new BukkitRunnable() {
            @Override
            public void run() {
                user.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                user.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(.5f);
                user.getWorld().playSound(user.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1f, .1f);
                if(upgradeLevel < 3) {
                    int amplifier = upgradeLevel < 2 ? 2 : 0;
                    user.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, DataConstants.TICKS_IN_MINUTE, amplifier));
                    user.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DataConstants.TICKS_IN_MINUTE, amplifier));
                }
                
            }
        }.runTaskLater(Nftilation.getInstance(), 25);
        int cooldown =  upgradeLevel * 3;
        InventoryUtils.setCooldownForItem(stack, cooldown);
    }

    private void shotArrow(ItemStack stack, Player user) {
        if(InventoryUtils.isCooldownRemain(stack)) {
            InventoryUtils.notifyAboutCooldown(user, stack);
            return;
        }
        Arrow arrow = user.launchProjectile(Arrow.class);
        arrow.setVelocity(arrow.getVelocity().multiply(3.2));
        String team = Store.getInstance().getDataInstance().getPlayerTeam(user.getUniqueId());
        int upgradeLevel = GameInstance.getInstance().getPlayerModelFromTeam(team, user).getUpgradeLevel();
        long cooldown = 2 + upgradeLevel * 3L;
        InventoryUtils.setCooldownForItem(stack, cooldown);
    }
    
}
