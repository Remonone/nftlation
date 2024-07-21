package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import remonone.nftilation.Store;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.utils.InventoryUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Monkey extends Role {
    @Override
    public Material getMaterial() {
        return Material.BROWN_MUSHROOM;
    }

    @Override
    public String getRoleName() {
        return "Monkey";
    }

    @Override
    public List<String> getRoleDescription() {
        return Arrays.asList(RoleConstant.MONKEY_DESCRIPTION_1, RoleConstant.MONKEY_DESCRIPTION_2, RoleConstant.MONKEY_DESCRIPTION_3);
    }

    @Override
    public String getRoleID() {
        return "MN";
    }

    @Override
    public int getRoleIndex() {
        return 33;
    }
    
    @Override
    protected ItemStack getSword(int upgradeLevel) {
        ItemStack stack = new ItemStack(Material.STICK);
        switch(upgradeLevel) {
            case 1: 
            case 2:
                stack = new ItemStack(Material.STICK);
                stack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
                NBT.modify(stack, nbt -> {
                    nbt.setInteger("level", 2);
                    nbt.setString("monkey", "stick");
                });
                break;
            case 3:
                stack = new ItemStack(Material.STICK);
                stack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
                NBT.modify(stack, nbt -> {
                    nbt.setInteger("level", 3);
                    nbt.setString("monkey", "stick");
                });
                break;
        }
        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        String name = upgradeLevel == 3 ? "Monkey King Wand" : "BONK";
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
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
    protected List<ItemStack> getAbilityItems(int upgradeLevel){
        ItemStack itemStack = new ItemStack(Material.INK_SACK);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName(RoleConstant.MONKEY_ABILITY_ITEM);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        NBT.modify(itemStack, nbt -> {nbt.setString("monkey", "invisibility");});
        return Collections.singletonList(itemStack);
    }
    
    @Override
    public void setPlayer(Player player, int upgradeLevel) {
        player.setHealthScaled(true);
        float healthModifier = upgradeLevel > 2 ? 30 : 40;
        float speedModifier = .05F * upgradeLevel;
        player.setWalkSpeed(DataConstants.PLAYER_SPEED + speedModifier);
        player.setHealthScale(DataConstants.PLAYER_HEALTH - (DataConstants.PLAYER_HEALTH / 100) * healthModifier);
        player.setAllowFlight(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 100, false, false));
    }
    
    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player)) return;
        if(!(e.getEntity() instanceof Player)) return;
        Player attacker = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if(!(Store.getInstance().getDataInstance().getPlayerRole(attacker.getUniqueId()) instanceof Monkey)) return;
        if(weapon == null || weapon.getAmount() < 1 || !weapon.getType().equals(Material.AIR)) return;
        String isStick = NBT.get(weapon, nbt -> (String) nbt.getString("monkey"));
        if(StringUtils.isBlank(isStick) || !isStick.equals("stick")) return;
        GameInstance instance = GameInstance.getInstance();
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        String team = dataInstance.getPlayerTeam(attacker.getUniqueId());
        GameInstance.PlayerModel model = instance.getPlayerModelFromTeam(team, attacker);
        if(model == null) return;
        int level = model.getUpgradeLevel();
        if(level == 2) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 10*DataConstants.TICKS_IN_SECOND, 1, false, true));
        } else if(level == 3) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 10*DataConstants.TICKS_IN_SECOND, 2, false, true));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5*DataConstants.TICKS_IN_SECOND, 1, false, true));
        }
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
        Map<String, Object> params = Store.getInstance().getDataInstance().getPlayerParams(accessor.getUniqueId());
        e.setCancelled(true);
        accessor.setAllowFlight(false);
        accessor.setFlying(false);
        double acceleration = 2;
        double velocityUp = .5;
        accessor.setVelocity(accessor.getVelocity().multiply(acceleration).setY(velocityUp).add(accessor.getLocation().getDirection().normalize()));
        params.put("cooldown", System.currentTimeMillis() + RoleConstant.DOUBLE_JUMP_COOLDOWN * DataConstants.ONE_SECOND);
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
        Map<String, Object> params = Store.getInstance().getDataInstance().getPlayerParams(accessor.getUniqueId());
        if((from.getBlockY()>to.getBlockY())
                && !(accessor.getLocation().add(0, -2, 0)
                .getBlock()
                .getType()
                .equals(Material.AIR)) && (params.containsKey("cooldown") && (((long)params.get("cooldown")) < System.currentTimeMillis()))) {
            accessor.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getAmount() < 1 || item.getType() == Material.AIR) {
            return;
        }
        if (!(Store.getInstance().getDataInstance().getPlayerRole(player.getUniqueId()) instanceof Monkey)) return;
        String isInvisiblity = NBT.get(item, nbt -> (String) nbt.getString("monkey"));
        if (StringUtils.isEmpty(isInvisiblity) || !isInvisiblity.equals("invisibility")) return;
        long cooldown = NBT.get(item, nbt -> (Long) nbt.getLong("cooldown"));
        if(cooldown > System.currentTimeMillis()) {
            player.playSound(player.getLocation(), Sound.ENTITY_CAT_HURT, 1f, 1f);
            player.sendMessage(ChatColor.RED + MessageConstant.ITEM_COOLDOWN + (int)((cooldown - System.currentTimeMillis()) / 1000));
            return;
        }
        GameInstance instance = GameInstance.getInstance();
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        String team = dataInstance.getPlayerTeam(player.getUniqueId());
        GameInstance.PlayerModel model = instance.getPlayerModelFromTeam(team, player);
        int length = 1 + model.getUpgradeLevel() * 5;
        World world = player.getWorld();
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, length * DataConstants.TICKS_IN_SECOND, 0, false, false));
        world.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, .5f, .8f);
        InventoryUtils.setCooldownForItem(item, 60);
    }
}
