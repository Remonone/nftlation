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
import remonone.nftilation.application.models.PlayerData;
import remonone.nftilation.constants.DataConstants;
import remonone.nftilation.constants.MessageConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;

import java.util.Arrays;
import java.util.List;

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
        return Arrays.asList("This is a Monkey");
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
    protected List<ItemStack> getAbilityItems(int upgradeLevel){
        ItemStack itemStack = new ItemStack(Material.INK_SACK);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName("Invisibility");
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        NBT.modify(itemStack, nbt -> {nbt.setString("monkey", "invisibility");});
        return Arrays.asList(itemStack);
    }
    
    @Override
    public void setPlayer(Player player, int upgradeLevel) {
        player.setHealthScaled(true);
        float healthModifier = upgradeLevel > 1 ? 20 : 40;
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
        if(!(Store.getInstance().getDataInstance().getPlayerRole(attacker.getName()) instanceof Monkey)) return;
        String isStick = NBT.get(weapon, nbt -> (String) nbt.getString("monkey"));
        if(StringUtils.isBlank(isStick) || !isStick.equals("stick")) return;
        GameInstance instance = GameInstance.getInstance();
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        PlayerData playerData = dataInstance.FindPlayerByName(attacker.getName());
        GameInstance.PlayerModel model = instance.getPlayerModelFromTeam(playerData.getTeam().getTeamName(), attacker);
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
        Role role = data.getPlayerRole(accessor.getName());
        if(!(role instanceof Monkey)) {
            return;
        }
        if(Store.getInstance().getGameStage().getStage() == Stage.LOBBY) {
            return;
        }
        e.setCancelled(true);
        accessor.setAllowFlight(false);
        accessor.setFlying(false);
        double acceleration = 2;
        double velocityUp = .5;
        accessor.setVelocity(accessor.getVelocity().multiply(acceleration).setY(velocityUp).add(accessor.getLocation().getDirection().normalize()));
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        if(Store.getInstance().getGameStage().getStage() != Stage.IN_GAME) return;
        Player accessor = e.getPlayer();
        DataInstance data = Store.getInstance().getDataInstance();
        Role role = data.getPlayerRole(accessor.getName());
        if(!(role instanceof Monkey)) {
            return;
        }
        CheckOnGround(accessor, e.getFrom(), e.getTo());
    }

    private void CheckOnGround(Player accessor, Location from, Location to) {
        if((from.getBlockY()>to.getBlockY())
                && !(accessor.getLocation().add(0, -2, 0)
                .getBlock()
                .getType()
                .equals(Material.AIR))){
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
        if (!(Store.getInstance().getDataInstance().getPlayerRole(player.getName()) instanceof Monkey)) return;
        String isInvisiblity = NBT.get(item, nbt -> (String) nbt.getString("monkey"));
        if (StringUtils.isEmpty(isInvisiblity) || !isInvisiblity.equals("invisibility")) return;
        long cooldown = NBT.get(item, nbt -> (Long) nbt.getLong("cooldown"));
        if(cooldown > System.currentTimeMillis()) {
            player.playSound(player.getLocation(), Sound.ENTITY_CAT_HURT, 1f, 1f);
            player.sendMessage(ChatColor.RED + MessageConstant.ITEM_COOLDOWN + " Cooldown: " + (int)((cooldown - System.currentTimeMillis()) / 1000));
            return;
        }
        GameInstance instance = GameInstance.getInstance();
        DataInstance dataInstance = Store.getInstance().getDataInstance();
        PlayerData playerData = dataInstance.FindPlayerByName(player.getName());
        GameInstance.PlayerModel model = instance.getPlayerModelFromTeam(playerData.getTeam().getTeamName(), player);
        int length = 1 + model.getUpgradeLevel() * 2;
        World world = player.getWorld();
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, length * DataConstants.TICKS_IN_SECOND, 0, false, false));
        world.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, .5f, .8f);
        NBT.modify(item, nbt -> {nbt.setLong("cooldown", System.currentTimeMillis() + 60 * DataConstants.ONE_SECOND);});
    }
}
