package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.utils.Logger;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class Cryptomarine extends Role {
    
    private static final Random RANDOM = new Random();
    
    @Override
    public Material getMaterial() {
        return Material.SHIELD;
    }

    @Override
    public String getRoleName() {
        return "Cryptomarine";
    }

    @Override
    public List<String> getRoleDescription() {
        return Arrays.asList("This is a Cryptomarine");
    }

    @Override
    public String getRoleID() {
        return "CM";
    }

    @Override
    public int getRoleIndex() {
        return 24;
    }
    
    @Override
    protected ItemStack getHelmet(Player player, int upgradeLevel){
        ItemStack itemStack = new ItemStack(Material.LEATHER_HELMET);
        switch(upgradeLevel){
            case 1:
                itemStack = new ItemStack(Material.IRON_HELMET);
                break;
            case 2:
                itemStack = new ItemStack(Material.DIAMOND_HELMET);
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                break;
            case 3:
                itemStack = new ItemStack(Material.DIAMOND_HELMET);
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                break;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    @Override
    protected ItemStack getChestplate(Player player, int upgradeLevel){
        ItemStack itemStack = new ItemStack(Material.LEATHER_CHESTPLATE);
        switch(upgradeLevel){
            case 1:
                itemStack = new ItemStack(Material.IRON_CHESTPLATE);
                break;
            case 2:
                itemStack = new ItemStack(Material.DIAMOND_CHESTPLATE);
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                break;
            case 3:
                itemStack = new ItemStack(Material.DIAMOND_CHESTPLATE);
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                break;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    @Override
    protected ItemStack getLeggins(Player player, int upgradeLevel){
        ItemStack itemStack = new ItemStack(Material.LEATHER_LEGGINGS);
        switch(upgradeLevel){
            case 1:
            case 2:
                itemStack = new ItemStack(Material.IRON_LEGGINGS);
                break;
            case 3:
                itemStack = new ItemStack(Material.DIAMOND_LEGGINGS);
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                break;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    @Override
    protected ItemStack getBoots(Player player, int upgradeLevel){
        ItemStack itemStack = new ItemStack(Material.LEATHER_BOOTS);
        switch(upgradeLevel){
            case 1:
            case 2:
                itemStack = new ItemStack(Material.IRON_BOOTS);
                break;
            case 3:
                itemStack = new ItemStack(Material.DIAMOND_BOOTS);
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                break;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    @Override
    protected ItemStack getAxe(int upgradeLevel){
        ItemStack itemStack = new ItemStack(Material.LEATHER_LEGGINGS);
        switch(upgradeLevel){
            case 1:
                itemStack = new ItemStack(Material.IRON_AXE);
                break;
            case 2:
                itemStack = new ItemStack(Material.IRON_AXE);
                itemStack.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                break;
            case 3:
                itemStack = new ItemStack(Material.DIAMOND_AXE);
                itemStack.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                NBT.modify(itemStack, nbt -> {nbt.setString("cryptomarine", "axe");});
                break;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName("Splitter");
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    @Override
    protected List<ItemStack> getAbilityItems(int upgradeLevel){
        ItemStack itemStack = new ItemStack(Material.SHIELD);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName("Humanity defender");
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return Arrays.asList(itemStack);
    }
    
    @Override
    public void setPlayer(Player player, int upgradeLevel) {
        Logger.debug("applying...");
        player.setHealthScaled(true);
        float health = DataConstants.PLAYER_HEALTH + upgradeLevel * 2;
        player.setHealthScale(health);
        float speed = DataConstants.PLAYER_SPEED;
        float modifier = upgradeLevel == 3 ? 10 : 20;
        player.setWalkSpeed(speed - (speed / 100) * modifier);
        if(upgradeLevel > 1) {
            player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100000, 1, false, false));
        }
    }
    
    @Override
    protected void killPlayer(Player player, int upgradeLevel) {
        if(!(Store.getInstance().getDataInstance().getPlayerRole(player.getName()) instanceof Cryptomarine)) return;
        PlayerData data = Store.getInstance().getDataInstance().FindPlayerByName(player.getName());
        GameInstance.PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(data.getTeam().getTeamName(), player);
        if(model.getUpgradeLevel() < 3) return;
        World world = player.getWorld();
        Location location = player.getLocation();
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 1000, false, false));
        TNTPrimed entity = player.getWorld().spawn(location, TNTPrimed.class);
        entity.setYield(10);
        entity.setFuseTicks(0);
        entity.setMetadata("invoker", new MetadataValue() {
            @Override
            public Object value() {return player;}
            @Override
            public int asInt() {return 0;}
            @Override
            public float asFloat() {return 0;}
            @Override
            public double asDouble() {return 0;}
            @Override
            public long asLong() {return 0;}
            @Override
            public short asShort() {return 0;}
            @Override
            public byte asByte() {return 0;}
            @Override
            public boolean asBoolean() {return false;}
            @Override
            public String asString() {return "";}
            @Override
            public Plugin getOwningPlugin() {return Nftilation.getInstance();}
            @Override
            public void invalidate() {}
        });
        entity.setIsIncendiary(true);
        world.createExplosion(location.getX(), location.getY(), location.getZ(), 5, true, false);
    }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player)) return;
        if(!(e.getEntity() instanceof Player)) return;
        if(e.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) return;
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        Player attacker = (Player)e.getDamager();
        Player victim = (Player)e.getEntity();
        if(!(Store.getInstance().getDataInstance().getPlayerRole(attacker.getName()) instanceof Cryptomarine)) return;
        String axe = NBT.get(attacker.getInventory().getItemInMainHand(), nbt -> (String) nbt.getString("cryptomarine"));
        if(StringUtils.isEmpty(axe) || !axe.equals("axe")) return;
        if(RANDOM.nextInt(10) > 7) return;
        World world = attacker.getWorld();
        Location location = e.getEntity().getLocation();
        world.strikeLightningEffect(location);
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(attacker, victim, EntityDamageEvent.DamageCause.LIGHTNING, 5D);
        getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        victim.setHealth(victim.getHealth() - event.getFinalDamage());
        victim.setLastDamageCause(event);
    }
}
