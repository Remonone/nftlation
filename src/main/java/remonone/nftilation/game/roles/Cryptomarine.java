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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.Nftilation;
import remonone.nftilation.Store;
import remonone.nftilation.components.EntityHandleComponent;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.PlayerUtils;

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
        return Arrays.asList(RoleConstant.CRYPTOMARINE_DESCRIPTION_1, RoleConstant.CTYPTOMARINE_DESCRIPTION_2, RoleConstant.CTYPTOMARINE_DESCRIPTION_3, RoleConstant.CTYPTOMARINE_DESCRIPTION_4);
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
    protected ItemStack getHelmet(Map<String, Object> params){
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
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
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                break;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    @Override
    protected ItemStack getChestplate(Map<String, Object> params){
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
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
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                break;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    @Override
    protected ItemStack getLeggings(Map<String, Object> params){
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        ItemStack itemStack = new ItemStack(Material.LEATHER_LEGGINGS);
        switch(upgradeLevel){
            case 1:
            case 2:
                itemStack = new ItemStack(Material.IRON_LEGGINGS);
                break;
            case 3:
                itemStack = new ItemStack(Material.DIAMOND_LEGGINGS);
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                break;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    @Override
    protected ItemStack getBoots(Map<String, Object> params){
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        ItemStack itemStack = new ItemStack(Material.LEATHER_BOOTS);
        switch(upgradeLevel){
            case 1:
            case 2:
                itemStack = new ItemStack(Material.IRON_BOOTS);
                break;
            case 3:
                itemStack = new ItemStack(Material.DIAMOND_BOOTS);
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                break;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    @Override
    protected ItemStack getAxe(Map<String, Object> params){
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        ItemStack itemStack = new ItemStack(Material.LEATHER_LEGGINGS);
        switch(upgradeLevel){
            case 1:
                itemStack = new ItemStack(Material.IRON_AXE);
                itemStack.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
                break;
            case 2:
                itemStack = new ItemStack(Material.IRON_AXE);
                itemStack.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                itemStack.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
                break;
            case 3:
                itemStack = new ItemStack(Material.DIAMOND_AXE);
                itemStack.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                itemStack.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
                NBT.modify(itemStack, nbt -> {nbt.setString("cryptomarine", "axe");});
                break;
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(RoleConstant.CRYPTOMARINE_AXE);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    @Override
    protected List<ItemStack> getAbilityItems(Map<String, Object> params){
        ItemStack itemStack = new ItemStack(Material.SHIELD);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName(RoleConstant.CTYPTOMARINE_SHIELD);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return Collections.singletonList(itemStack);
    }
    
    @Override
    public void setPlayer(Player player, Map<String, Object> params) {
        if(!PlayerUtils.validateParams(params)) return;
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        float health = DataConstants.PLAYER_HEALTH + upgradeLevel * 2;
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        float speed = DataConstants.PLAYER_SPEED;
        float modifier = upgradeLevel == 3 ? 10 : 20;
        player.setWalkSpeed(speed - (speed / 100) * modifier);
        if(upgradeLevel > 1) {
            player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, DataConstants.CONSTANT_POTION_DURATION, 1, false, false));
        }
    }
    
    @Override
    protected void killPlayer(Player player) {
        if(!(Store.getInstance().getDataInstance().getPlayerRole(player.getUniqueId()) instanceof Cryptomarine)) return;
        String team = Store.getInstance().getDataInstance().getPlayerTeam(player.getUniqueId());
        PlayerModel model = GameInstance.getInstance().getPlayerModelFromTeam(team, player);
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        int upgradeLevel = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        if(upgradeLevel < 3) return;
        Location location = player.getLocation();
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 1000, false, false));
        TNTPrimed entity = player.getWorld().spawn(location, TNTPrimed.class);
        entity.setMetadata("cryptomarine", new FixedMetadataValue(Nftilation.getInstance(), 10));
        entity.setFuseTicks(0);
        EntityHandleComponent.setEntityOwner(entity, player);
        entity.setIsIncendiary(true);
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player)) return;
        if(!(e.getEntity() instanceof Player)) return;
        if(e.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) return;
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        Player attacker = (Player)e.getDamager();
        Player victim = (Player)e.getEntity();
        if(!(Store.getInstance().getDataInstance().getPlayerRole(attacker.getUniqueId()) instanceof Cryptomarine)) return;
        ItemStack itemStack = attacker.getInventory().getItemInMainHand();
        if(itemStack == null || itemStack.getAmount() < 1 || itemStack.getType().equals(Material.AIR)) return;
        String axe = NBT.get(itemStack, nbt -> (String) nbt.getString("cryptomarine"));
        if(StringUtils.isEmpty(axe) || !axe.equals("axe")) return;
        if(RANDOM.nextFloat() > RoleConstant.CRYPTOMARINE_LIGHTNING_CHANCE) return;
        World world = attacker.getWorld();
        Location location = e.getEntity().getLocation();
        world.strikeLightningEffect(location);
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(attacker, victim, EntityDamageEvent.DamageCause.LIGHTNING, RoleConstant.CRYPTOMARINE_LIGHTNING_DAMAGE);
        getServer().getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        victim.setHealth(victim.getHealth() - event.getFinalDamage());
        victim.setLastDamageCause(event);
    }
}
