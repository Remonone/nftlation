package remonone.nftilation.game.roles;

import de.tr7zw.nbtapi.NBT;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
import remonone.nftilation.constants.PropertyConstant;
import remonone.nftilation.constants.RoleConstant;
import remonone.nftilation.enums.Stage;
import remonone.nftilation.game.DataInstance;
import remonone.nftilation.game.GameInstance;
import remonone.nftilation.game.damage.MonkeyWandDamage;
import remonone.nftilation.game.models.IDamageInvoker;
import remonone.nftilation.game.models.PlayerModel;
import remonone.nftilation.utils.InventoryUtils;
import remonone.nftilation.utils.PlayerUtils;

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
    protected ItemStack getSword(Map<String, Object> params) {
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        ItemStack stack = new ItemStack(Material.STICK);
        stack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        NBT.modify(stack, nbt -> {
            nbt.setString(RoleConstant.MONKEY_NBT_CONTAINER, RoleConstant.MONKEY_NBT_WAND);
        });
        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        String name = upgradeLevel == 3 ? "Monkey King Wand" : "BONK";
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
    }
    
    @Override
    protected ItemStack getHelmet(Map<String, Object> params) {
        return new ItemStack(Material.AIR);
    }

    @Override
    protected ItemStack getChestplate(Map<String, Object> params) {
        return new ItemStack(Material.AIR);
    }

    @Override
    protected ItemStack getLeggings(Map<String, Object> params) {
        return new ItemStack(Material.AIR);
    }

    @Override
    protected ItemStack getBoots(Map<String, Object> params) {
        return new ItemStack(Material.AIR);
    }

    @Override
    protected List<ItemStack> getAbilityItems(Map<String, Object> params){
        ItemStack itemStack = new ItemStack(Material.INK_SACK);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName(RoleConstant.MONKEY_ABILITY_ITEM);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        NBT.modify(itemStack, nbt -> {nbt.setString(RoleConstant.MONKEY_NBT_CONTAINER, RoleConstant.MONKEY_NBT_INVISIBILITY);});
        return Collections.singletonList(itemStack);
    }
    
    @Override
    public void setPlayer(Player player, Map<String, Object> params) {
        int upgradeLevel = (Integer)params.get(PropertyConstant.PLAYER_LEVEL_PARAM);
        player.setHealthScaled(true);
        float healthModifier = upgradeLevel > 2 ? 30 : 40;
        float speedModifier = .05F * upgradeLevel;
        player.setWalkSpeed(DataConstants.PLAYER_SPEED + speedModifier);
        player.setHealthScale(DataConstants.PLAYER_HEALTH - (DataConstants.PLAYER_HEALTH / 100) * healthModifier);
        player.setAllowFlight(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 100, false, false));
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
        PlayerModel model = instance.getPlayerModelFromTeam(team, player);
        if(!PlayerUtils.validateParams(model.getParameters())) return;
        int level = (Integer)model.getParameters().get(PropertyConstant.PLAYER_LEVEL_PARAM);
        int length = 1 + level * 5;
        World world = player.getWorld();
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, length * DataConstants.TICKS_IN_SECOND, 0, false, false));
        world.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, .5f, .8f);
        InventoryUtils.setCooldownForItem(item, 60);
    }

    @Override
    public List<IDamageInvoker> getDamageInvokers() {
        return Collections.singletonList(new MonkeyWandDamage());
    }
}
